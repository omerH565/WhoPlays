package com.example.whoplays.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.whoplays.R
import com.example.whoplays.viewmodels.AuthResult
import com.example.whoplays.viewmodels.AuthViewModel

class RegisterActivity : BaseActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_register)
        setupToolbar("Register", showBackButton = true)

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        authViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            etFirstName.isEnabled = !isLoading
            etLastName.isEnabled = !isLoading
            etAge.isEnabled = !isLoading
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
        }

        authViewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthResult.Success -> {
                    val intent = Intent(this, OnboardingActivity::class.java).apply {
                        putExtra("EXTRA_FIRST_NAME", etFirstName.text.toString().trim())
                        putExtra("EXTRA_LAST_NAME", etLastName.text.toString().trim())
                        putExtra("EXTRA_AGE", etAge.text.toString().trim())
                    }
                    startActivity(intent)
                    finishAffinity()
                }
                is AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    authViewModel.resetAuthResult()
                }
                null -> {}
            }
        }

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val age = etAge.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.register(email, pass)
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}
