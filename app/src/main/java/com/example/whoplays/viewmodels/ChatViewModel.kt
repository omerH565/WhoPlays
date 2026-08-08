package com.example.whoplays.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.whoplays.models.Message
import com.example.whoplays.models.User
import com.example.whoplays.repositories.ChatRepository
import com.example.whoplays.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _currentUser.postValue(userRepository.getUser(uid))
        }
    }

    fun getMessages(gameId: String): LiveData<List<Message>> {
        return chatRepository.getMessages(gameId).asLiveData()
    }

    fun sendMessage(gameId: String, text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return

        val message = Message(
            senderId = user.uid,
            senderName = "${user.firstName} ${user.lastName}",
            senderProfileImageUrl = user.profileImageUrl,
            text = text
        )

        viewModelScope.launch {
            chatRepository.sendMessage(gameId, message)
            // Here we could trigger a notification logic if we had a backend
        }
    }
}
