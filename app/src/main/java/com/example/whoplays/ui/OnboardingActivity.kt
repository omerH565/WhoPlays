package com.example.whoplays.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.whoplays.R
import com.example.whoplays.models.User
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class OnboardingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        setupToolbar("Preferences")

        val chipGroupSports = findViewById<ChipGroup>(R.id.chipGroupSports)
        val btnFinish = findViewById<Button>(R.id.btnFinishOnboarding)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnFinish.setOnClickListener {
            val selectedSports = mutableListOf<String>()
            for (i in 0 until chipGroupSports.childCount) {
                val chip = chipGroupSports.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selectedSports.add(chip.text.toString())
                }
            }

            if (selectedSports.isEmpty()) {
                Toast.makeText(this, "Please select at least one sport", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveUserPreferences(selectedSports, progressBar)
        }
    }

    private fun saveUserPreferences(sports: List<String>, progressBar: ProgressBar) {
        val currentUser = auth.currentUser ?: return
        progressBar.visibility = View.VISIBLE

        val firstName = intent.getStringExtra("EXTRA_FIRST_NAME") ?: ""
        val lastName = intent.getStringExtra("EXTRA_LAST_NAME") ?: ""
        val ageStr = intent.getStringExtra("EXTRA_AGE") ?: "0"

        val user = User(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
            firstName = firstName,
            lastName = lastName,
            age = ageStr.toIntOrNull() ?: 0,
            favoriteSports = sports
        )

        lifecycleScope.launch {
            val success = userRepository.saveUser(user)
            progressBar.visibility = View.GONE
            if (success) {
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@OnboardingActivity, "Failed to save preferences", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
