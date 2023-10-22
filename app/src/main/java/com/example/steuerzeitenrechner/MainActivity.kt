package com.example.steuerzeitenrechner

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.math.atan2
import com.example.steuerzeitenrechner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magneticFieldReading = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        binding.startStopButton.setOnClickListener {
            if (binding.startStopButton.text == getString(R.string.start_gyroscope)) {
                startGyroscopeUpdates()
                binding.startStopButton.text = getString(R.string.stop_gyroscope)
            } else {
                stopGyroscopeUpdates()
                binding.startStopButton.text = getString(R.string.start_gyroscope)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.startStopButton.text == getString(R.string.stop_gyroscope)) {
            startGyroscopeUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopGyroscopeUpdates()
    }

    private fun startGyroscopeUpdates() {
        accelerometer?.let { accelSensor ->
            magneticField?.let { magSensor ->
                sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
                sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun stopGyroscopeUpdates() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> System.arraycopy(it.values, 0, accelerometerReading, 0, it.values.size)
                Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(it.values, 0, magneticFieldReading, 0, it.values.size)
            }

            val rotationMatrix = FloatArray(9)
            val rotationMatrixAdjusted = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerReading, magneticFieldReading)) {
                SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                    rotationMatrixAdjusted);
                SensorManager.getOrientation(rotationMatrixAdjusted, orientationAngles)
                //val azimuth = -atan2(rotationMatrix[3], rotationMatrix[0])
                binding.gyroValues.text = getString(R.string.azimuth_value, Math.toDegrees(orientationAngles[0].toDouble()), Math.toDegrees(orientationAngles[1].toDouble()), Math.toDegrees(orientationAngles[2].toDouble()))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}
