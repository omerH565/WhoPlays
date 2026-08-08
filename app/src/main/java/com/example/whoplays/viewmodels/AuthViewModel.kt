package com.example.whoplays.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whoplays.repositories.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authResult = MutableLiveData<AuthResult?>()
    val authResult: LiveData<AuthResult?> = _authResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun checkCurrentUser(): Boolean {
        return authRepository.currentUser != null
    }

    fun login(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authRepository.login(email, pass)
                _authResult.postValue(AuthResult.Success)
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.localizedMessage ?: "Login failed"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun register(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authRepository.register(email, pass)
                _authResult.postValue(AuthResult.Success)
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.localizedMessage ?: "Registration failed"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun resetAuthResult() {
        _authResult.value = null
    }
}
