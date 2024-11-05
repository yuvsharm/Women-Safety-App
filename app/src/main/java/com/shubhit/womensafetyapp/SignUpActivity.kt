package com.shubhit.womensafetyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.shubhit.womensafetyapp.databinding.ActivitySignUpBinding
import com.shubhit.womensafetyapp.utills.Preferences

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passET.text.toString().trim()
            val confirmPassword = binding.confirmPassET.text.toString().trim()
            val mobileNumber = binding.phoneEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                signUp(email, password, mobileNumber)
            } else {
                Toast.makeText(this, "Please enter valid information", Toast.LENGTH_SHORT).show()
            }
        }
        binding.signInText.setOnClickListener {
            val intent = Intent(this@SignUpActivity, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUp(email: String, password: String, mobileNumber: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        Preferences.userId = userId
                    }
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    Preferences.phoneNumber=mobileNumber
                    val intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("FROM_BOTTOM_NAV", false)  // Flag to indicate the source
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this,
                        "Account creation failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}