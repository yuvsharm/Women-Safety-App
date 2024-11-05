package com.shubhit.womensafetyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shubhit.womensafetyapp.databinding.ActivitySplashBinding
import com.shubhit.womensafetyapp.utills.LocationUtills
import com.shubhit.womensafetyapp.utills.Preferences

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    lateinit var binding: ActivitySplashBinding

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Preferences.setup(this)

        // Initialize sensor manager and shake detector
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector {
            checkSmsPermissionAndSendHelpMessage()
        }

        // Check and request permissions
        checkAndRequestPermissions()

        val userId = Preferences.userId
        if (userId != null) {
            binding.getStartedButton.text = "Get Started"
        }

        binding.getStartedButton.setOnClickListener {
            if (userId != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            initializeAppFeatures()
        }
    }

    private fun initializeAppFeatures() {
        LocationUtills.getLastKnownLocation(this) { location ->
            location?.let {
                val address = LocationUtills.getAddressFromLocation(this, it)
                Preferences.addressObject = address
            }
        }
    }

    private fun checkSmsPermissionAndSendHelpMessage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            sendHelpMessage()
        } else {
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendHelpMessage() {
        LocationUtills.getLastKnownLocation(this) { location ->
            if (location != null) {
                val message =
                    "I need help! My location is: https://maps.google.com/?q=${location.latitude},${location.longitude}"

                val emergencyContacts = Preferences.emergencyContacts!!
                val smsManager = SmsManager.getDefault()

                for (contact in emergencyContacts) {
                    smsManager.sendTextMessage(contact.number, null, message, null, null)
                }
                Toast.makeText(this, "Help message sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    initializeAppFeatures()
                } else {
                    Toast.makeText(
                        this,
                        "All permissions are required for the app to function correctly",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
