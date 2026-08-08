package com.example.whoplays.models

data class User(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val profileImageUrl: String = "",
    val favoriteSports: List<String> = emptyList(),
    val fcmToken: String = "",
    val city: String = ""
)
