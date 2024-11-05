package com.shubhit.womensafetyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.shubhit.womensafetyapp.databinding.ActivityAuthBinding
import com.shubhit.womensafetyapp.utills.Preferences

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        binding.signInButton.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passET.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpText.setOnClickListener {
            val intent = Intent(this@AuthActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPassText.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun signIn(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        Preferences.userId = userId
                    }
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    // Redirect to main activity or dashboard
                    val intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("FROM_BOTTOM_NAV", false)  // Flag to indicate the source
                    startActivity(intent)
                    finish()
                } else {
                    // Check if the error is because the user is not registered
                    Toast.makeText(
                        this,
                        "Authentication failed: Enter correct Email and Password",
                        Toast.LENGTH_LONG
                    ).show()


                }


            }


    }


}