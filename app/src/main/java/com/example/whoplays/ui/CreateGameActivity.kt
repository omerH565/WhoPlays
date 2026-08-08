package com.example.whoplays.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.whoplays.R
import com.example.whoplays.viewmodels.CreateGameViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateGameActivity : BaseActivity() {

    private val createGameViewModel: CreateGameViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        
        setupToolbar("Create New Game", showBackButton = true)

        val currentUserId = auth.currentUser?.uid ?: return

        val etSportType = findViewById<EditText>(R.id.etSportType)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etMaxPlayers = findViewById<EditText>(R.id.etMaxPlayers)
        val ivSelectedImage = findViewById<ImageView>(R.id.ivSelectedImage)
        val btnPickImage = findViewById<Button>(R.id.btnPickImage)
        val btnCreateGame = findViewById<Button>(R.id.btnCreateGame)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        
        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
        val tvSelectedDateTime = findViewById<TextView>(R.id.tvSelectedDateTime)

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                ivSelectedImage.setImageURI(uri)
            }
        }

        val requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Camera Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "We need camera access.", Toast.LENGTH_LONG).show()
            }
        }

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnPickImage.setOnLongClickListener {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            true
        }

        btnPickDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateTimeUI(tvSelectedDateTime)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPickTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTimeUI(tvSelectedDateTime)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        btnCreateGame.setOnClickListener {
            val sportType = etSportType.text.toString()
            val location = etLocation.text.toString()
            val maxPlayersStr = etMaxPlayers.text.toString()

            if (sportType.isEmpty() || location.isEmpty() || maxPlayersStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val maxPlayers = maxPlayersStr.toIntOrNull() ?: 2

            createGameViewModel.createNewGame(
                creatorId = currentUserId,
                sportType = sportType,
                location = location,
                maxPlayers = maxPlayers,
                imageUri = selectedImageUri,
                dateTimeMillis = calendar.timeInMillis
            )
        }

        createGameViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnCreateGame.isEnabled = !isLoading
            btnPickImage.isEnabled = !isLoading
        }

        createGameViewModel.isSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Game Created Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to create game.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDateTimeUI(textView: TextView) {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        textView.text = "Selected: ${format.format(calendar.time)}"
    }
}
