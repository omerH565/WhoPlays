package com.example.whoplays.models

data class Game(
    val gameId: String = "",
    val creatorId: String = "",
    val sportType: String = "",
    val locationName: String = "",
    val currentPlayers: Int = 0,
    val maxPlayers: Int = 0,
    val courtImageUrl: String = "",
    val participantIds: List<String> = emptyList(),
    val dateTime: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
