package com.wheretogo.presentation.composable.effect

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber
import kotlin.math.sqrt

@Composable
fun ShakeEffect(
    thresholdG: Float = 2.7f,
    debounceMs: Long = 500L,
    onShake: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accel = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var lastShakeTime by remember { mutableStateOf(0L) }

    DisposableEffect(Unit) {
        if (accel == null) {
            Timber.tag("Shake").w("Accelerometer not available")
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
                    val ax = event.values[0]
                    val ay = event.values[1]
                    val az = event.values[2]
                    val gX = ax / SensorManager.GRAVITY_EARTH
                    val gY = ay / SensorManager.GRAVITY_EARTH
                    val gZ = az / SensorManager.GRAVITY_EARTH
                    val gForce = sqrt(gX*gX + gY*gY + gZ*gZ)
                    if (gForce > thresholdG) {
                        val now = System.currentTimeMillis()
                        if (now - lastShakeTime > debounceMs) {
                            lastShakeTime = now
                            onShake()
                        }
                    }
                }
            }
            sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }
}