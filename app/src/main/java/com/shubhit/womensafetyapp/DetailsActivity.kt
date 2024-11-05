package com.shubhit.womensafetyapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.shubhit.womensafetyapp.databinding.ActivityDetailsBinding
import com.shubhit.womensafetyapp.utills.LocationUtills
import com.shubhit.womensafetyapp.utills.Preferences

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fromBottomNav = intent.getBooleanExtra("FROM_BOTTOM_NAV", false)


        // Get phone number from previous activity
        val phoneNumber = Preferences.phoneNumber
        binding.etPhoneNumber.setText(phoneNumber)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchLocationAndUpdateUI()
        }




        binding.etPhoneNumber.setText(Preferences.phoneNumber ?: "")
        binding.etName.setText(Preferences.name ?: "")
        binding.etAlternatePhoneNumber.setText(Preferences.AlternatePhoneNumber ?: "")
        binding.spinnerGender.setText(Preferences.gender ?: "")
        binding.etAge.setText(Preferences.age ?: "")
        binding.etAddress.setText(Preferences.address ?: "")
        binding.etState.setText(Preferences.state ?: "")
        binding.spinnerDistrict.setText(Preferences.district ?: "")


        binding.btnSubmit.setOnClickListener {
            Preferences.phoneNumber = binding.etPhoneNumber.text.toString()
            Preferences.name = binding.etName.text.toString()
            Preferences.AlternatePhoneNumber = binding.etAlternatePhoneNumber.text.toString()
            Preferences.gender = binding.spinnerGender.text.toString()
            Preferences.age = binding.etAge.text.toString()
            Preferences.address = binding.etAddress.text.toString()
            Preferences.state = binding.etState.text.toString()
            Preferences.district = binding.spinnerDistrict.text.toString()


            if (fromBottomNav) {
                finish()
            } else {
                val intent = Intent(this, AddContactActivity::class.java)
                startActivity(intent)
                finish()
            }



            Toast.makeText(this, "Details Saved", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchLocationAndUpdateUI() {
        LocationUtills.getLastKnownLocation(this) { location ->
            location?.let {
                val address = LocationUtills.getAddressFromLocation(this, it)
                address?.let { addr ->
                    binding.etAddress.setText(addr.getAddressLine(0))
                    binding.etState.setText(addr.adminArea)
                    binding.spinnerDistrict.setText(addr.locality)
                }
            } ?: run {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndUpdateUI()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}