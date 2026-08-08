package com.example.whoplays.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whoplays.models.Game
import com.example.whoplays.repositories.GameRepository
import com.example.whoplays.repositories.StorageRepository
import kotlinx.coroutines.launch

class CreateGameViewModel : ViewModel() {

    private val gameRepository = GameRepository()
    private val storageRepository = StorageRepository()

    // ניהול מצב טעינה כדי לחסום לחיצות כפולות
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ניהול סטטוס הצלחה
    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    fun createNewGame(
        creatorId: String,
        sportType: String,
        location: String,
        maxPlayers: Int,
        imageUri: Uri?,
        dateTimeMillis: Long
    ) {
        if (sportType.isBlank() || location.isBlank() || maxPlayers < 2) {
            _isSuccess.value = false
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            var imageUrl = ""

            // אם המשתמש בחר תמונה, קודם נעלה אותה
            if (imageUri != null) {
                val uploadedUrl = storageRepository.uploadCourtImage(imageUri)
                if (uploadedUrl != null) {
                    imageUrl = uploadedUrl
                }
            }

            // יצירת האובייקט המלא
            val newGame = Game(
                creatorId = creatorId,
                sportType = sportType,
                locationName = location,
                maxPlayers = maxPlayers,
                currentPlayers = 1, // היוצר הוא השחקן הראשון
                participantIds = listOf(creatorId),
                courtImageUrl = imageUrl,
                dateTime = dateTimeMillis.toString()
            )

            // שמירה במסד הנתונים
            val result = gameRepository.createGame(newGame)
            _isLoading.postValue(false)
            _isSuccess.postValue(result)
        }
    }
}