package org.jenson.heartrate

import android.content.Context
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.ExerciseCapabilities
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseInfo
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HeartRateViewModel(context: Context) : ViewModel() {
    private val exerciseClient = HealthServices.getClient(context.applicationContext).exerciseClient

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> = _heartRate

    private val _availability = MutableStateFlow<DataTypeAvailability?>(null)
    val availability: StateFlow<DataTypeAvailability?> = _availability

    @Volatile private var isAmbient = false
    private var lastAmbientUpdateMs = 0L

    fun setAmbientMode(ambient: Boolean) {
        isAmbient = ambient
        if (!ambient) lastAmbientUpdateMs = 0L
    }

    private val exerciseCallback = object : ExerciseUpdateCallback {
        override fun onRegistered() { }

        override fun onRegistrationFailed(throwable: Throwable) {
            _availability.value = DataTypeAvailability.UNAVAILABLE
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val heartRateDataPoints = update.latestMetrics.getData(DataType.HEART_RATE_BPM)
            if (heartRateDataPoints.isNotEmpty()) {
                val now = System.currentTimeMillis()
                if (!isAmbient || now - lastAmbientUpdateMs >= 10_000L) {
                    _heartRate.value = heartRateDataPoints.last().value.toInt()
                    if (isAmbient) lastAmbientUpdateMs = now
                }
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) { }

        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            if (availability is DataTypeAvailability) {
                _availability.value = availability
            }
        }
    }

    init {
        checkCapabilitiesThenStart()
    }

    private fun checkCapabilitiesThenStart() {
        Futures.addCallback(
            exerciseClient.getCapabilitiesAsync(),
            object : FutureCallback<ExerciseCapabilities> {
                override fun onSuccess(caps: ExerciseCapabilities) {
                    val typeCaps = caps.typeToCapabilities[ExerciseType.WORKOUT]
                    if (typeCaps != null && DataType.HEART_RATE_BPM in typeCaps.supportedDataTypes) {
                        guardExistingThenStart()
                    } else {
                        _availability.value = DataTypeAvailability.UNAVAILABLE
                    }
                }
                override fun onFailure(t: Throwable) {
                    _availability.value = DataTypeAvailability.UNAVAILABLE
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    /** Only one exercise may run device-wide; handle a stale or foreign session before starting. */
    private fun guardExistingThenStart() {
        Futures.addCallback(
            exerciseClient.getCurrentExerciseInfoAsync(),
            object : FutureCallback<ExerciseInfo> {
                override fun onSuccess(info: ExerciseInfo) {
                    when (info.exerciseTrackedStatus) {
                        ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS ->
                            // Another app (e.g. a running workout) holds the sensor; don't fight it.
                            _availability.value = DataTypeAvailability.UNAVAILABLE
                        ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS ->
                            // Orphan from a prior run/crash — end it, then start fresh.
                            Futures.addCallback(
                                exerciseClient.endExerciseAsync(),
                                object : FutureCallback<Void> {
                                    override fun onSuccess(result: Void?) = startExercise()
                                    override fun onFailure(t: Throwable) = startExercise()
                                },
                                MoreExecutors.directExecutor()
                            )
                        else -> startExercise()
                    }
                }
                override fun onFailure(t: Throwable) {
                    // Couldn't read current state; attempt a normal start anyway.
                    startExercise()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun startExercise() {
        exerciseClient.setUpdateCallback(exerciseCallback)
        val config = ExerciseConfig.builder(ExerciseType.WORKOUT)
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .setIsAutoPauseAndResumeEnabled(false)
            .setIsGpsEnabled(false)
            .build()
        Futures.addCallback(
            exerciseClient.startExerciseAsync(config),
            object : FutureCallback<Void> {
                override fun onSuccess(result: Void?) { }
                override fun onFailure(t: Throwable) {
                    _availability.value = DataTypeAvailability.UNAVAILABLE
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onCleared() {
        super.onCleared()
        exerciseClient.endExerciseAsync()
    }

    companion object {
        fun factory(context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HeartRateViewModel(context) as T
            }
        }
    }
}
