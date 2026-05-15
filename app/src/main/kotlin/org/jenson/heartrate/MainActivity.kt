package org.jenson.heartrate

import android.Manifest
import android.content.Intent
import android.content.LocusId
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    private var isAmbient by mutableStateOf(false)

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            isAmbient = true
        }
        override fun onExitAmbient() {
            isAmbient = false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions map — sensor starts automatically via ViewModel */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        setLocusContext(LocusId("hr_session"), null)
        lifecycle.addObserver(AmbientLifecycleObserver(this, ambientCallback))
        startForegroundService(Intent(this, HeartRateService::class.java))
        requestSensorPermission()
        setContent {
            MaterialTheme {
                HeartRateApp(
                    isAmbient = isAmbient,
                    onQuit = {
                        stopService(Intent(this@MainActivity, HeartRateService::class.java))
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Safety net: onExitAmbient may not fire reliably on all Wear OS versions.
        // onResume guarantees the Activity is fully interactive.
        isAmbient = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, HeartRateService::class.java))
    }

    private fun requestSensorPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }
        val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestPermissionLauncher.launch(arrayOf(permission))
        }
    }
}

@Composable
fun HeartRateApp(isAmbient: Boolean, onQuit: () -> Unit) {
    val viewModel: HeartRateViewModel = viewModel(
        factory = HeartRateViewModel.factory(LocalContext.current)
    )

    LaunchedEffect(isAmbient) {
        viewModel.setAmbientMode(isAmbient)
    }

    val heartRate by viewModel.heartRate.collectAsState()
    val availability by viewModel.availability.collectAsState()

    HeartRateScreen(
        heartRate = heartRate,
        availability = availability?.name,
        isAmbient = isAmbient,
        onQuit = onQuit,
        modifier = Modifier.fillMaxSize(),
    )
}
