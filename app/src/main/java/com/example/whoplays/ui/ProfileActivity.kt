package com.example.whoplays.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.whoplays.R
import com.example.whoplays.models.User
import com.example.whoplays.repositories.StorageRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private val storageRepository = StorageRepository()
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    private lateinit var ivProfilePic: ImageView
    private lateinit var tvEmail: TextView
    private lateinit var etCity: android.widget.EditText
    private lateinit var chipGroupSports: ChipGroup
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        setupToolbar("My Profile", showBackButton = true)

        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvEmail = findViewById(R.id.tvEmail)
        etCity = findViewById(R.id.etCity)
        chipGroupSports = findViewById(R.id.chipGroupSports)
        progressBar = findViewById(R.id.progressBar)
        val btnChangePic = findViewById<Button>(R.id.btnChangePic)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        loadUserData()

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                ivProfilePic.setImageURI(uri)
            }
        }

        btnChangePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            currentUser = userRepository.getUser(uid)
            progressBar.visibility = View.GONE
            currentUser?.let { user ->
                tvEmail.text = user.email
                etCity.setText(user.city)
                if (user.profileImageUrl.isNotEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(user.profileImageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(ivProfilePic)
                }
                
                for (i in 0 until chipGroupSports.childCount) {
                    val chip = chipGroupSports.getChildAt(i) as Chip
                    if (user.favoriteSports.contains(chip.text.toString())) {
                        chip.isChecked = true
                    }
                }
            }
        }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            var imageUrl = currentUser?.profileImageUrl ?: ""
            
            if (selectedImageUri != null) {
                val uploadedUrl = storageRepository.uploadProfileImage(uid, selectedImageUri!!)
                if (uploadedUrl != null) {
                    imageUrl = uploadedUrl
                }
            }

            val selectedSports = mutableListOf<String>()
            for (i in 0 until chipGroupSports.childCount) {
                val chip = chipGroupSports.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selectedSports.add(chip.text.toString())
                }
            }

            val updatedUser = User(
                uid = uid,
                email = auth.currentUser?.email ?: "",
                firstName = currentUser?.firstName ?: "",
                lastName = currentUser?.lastName ?: "",
                age = currentUser?.age ?: 0,
                profileImageUrl = imageUrl,
                favoriteSports = selectedSports,
                city = etCity.text.toString().trim()
            )

            val success = userRepository.saveUser(updatedUser)
            progressBar.visibility = View.GONE
            if (success) {
                Toast.makeText(this@ProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
                finish()
            } else {
                Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
