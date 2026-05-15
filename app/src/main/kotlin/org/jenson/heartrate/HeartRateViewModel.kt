package org.jenson.heartrate

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HeartRateViewModel(context: Context) : ViewModel() {
    private val measureClient = HealthServices.getClient(context.applicationContext).measureClient

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

    private val heartRateCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
            if (availability is DataTypeAvailability) {
                _availability.value = availability
            }
        }

        override fun onDataReceived(data: DataPointContainer) {
            val heartRateDataPoints = data.getData(DataType.HEART_RATE_BPM)
            if (heartRateDataPoints.isNotEmpty()) {
                val now = System.currentTimeMillis()
                if (!isAmbient || now - lastAmbientUpdateMs >= 10_000L) {
                    _heartRate.value = heartRateDataPoints.last().value.toInt()
                    if (isAmbient) lastAmbientUpdateMs = now
                }
            }
        }
    }

    init {
        checkCapabilitiesThenRegister()
    }

    private fun checkCapabilitiesThenRegister() {
        Futures.addCallback(
            measureClient.getCapabilitiesAsync(),
            object : FutureCallback<androidx.health.services.client.data.MeasureCapabilities> {
                override fun onSuccess(caps: androidx.health.services.client.data.MeasureCapabilities) {
                    if (DataType.HEART_RATE_BPM in caps.supportedDataTypesMeasure) registerCallback()
                }
                override fun onFailure(t: Throwable) { }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun registerCallback() {
        viewModelScope.launch {
            delay(500)
            try {
                measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, heartRateCallback)
            } catch (e: Exception) {
                _availability.value = DataTypeAvailability.UNAVAILABLE
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, heartRateCallback)
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
