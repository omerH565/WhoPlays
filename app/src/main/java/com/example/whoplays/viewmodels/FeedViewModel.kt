package com.example.whoplays.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whoplays.models.Game
import com.example.whoplays.models.User
import com.example.whoplays.repositories.GameRepository
import com.example.whoplays.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class FeedViewModel : ViewModel() {

    private val gameRepository = GameRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private var allGames = listOf<Game>()
    private var currentUser: User? = null
    private var currentFilterIndex = 0
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    private val _games = MutableLiveData<List<Game>>()
    val games: LiveData<List<Game>> = _games

    init {
        fetchUserProfile()
        fetchGames()
    }

    fun setUserLocation(lat: Double, lon: Double) {
        userLatitude = lat
        userLongitude = lon
        if (currentFilterIndex == 5) {
            applyCurrentFilter()
        }
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            currentUser = userRepository.getUser(uid)
            applyCurrentFilter()
        }
    }

    fun fetchGames() {
        viewModelScope.launch {
            val fromServer = gameRepository.getAvailableGames()
            if (fromServer.isEmpty()) {
                // אם השרת ריק, ניצור נתונים מקומיים ונציג אותם מיד
                allGames = getMockGames()
                _games.postValue(allGames)
                // במקביל ננסה לשמור אותם לשרת לפעם הבאה
                seedDataToFirebase(allGames)
            } else {
                allGames = fromServer
                applyCurrentFilter()
            }
        }
    }

    fun filterGames(categoryIndex: Int) {
        currentFilterIndex = categoryIndex
        if (categoryIndex == 3) { // Fav tab
            fetchUserProfile()
        } else {
            applyCurrentFilter()
        }
    }

    private fun applyCurrentFilter() {
        val filtered = when (currentFilterIndex) {
            1 -> filterByCity(allGames)
            2 -> filterToday(allGames)
            3 -> filterFavorites(allGames)
            4 -> filterJoined(allGames)
            else -> allGames
        }
        _games.postValue(filtered)
    }

    private fun filterByCity(list: List<Game>): List<Game> {
        val city = currentUser?.city ?: return emptyList()
        if (city.isBlank()) return emptyList()
        return list.filter { it.locationName.contains(city, ignoreCase = true) }
    }

    private fun filterToday(list: List<Game>): List<Game> {
        val today = Calendar.getInstance()
        return list.filter {
            val gameDate = Calendar.getInstance().apply { timeInMillis = it.dateTime.toLongOrNull() ?: 0 }
            gameDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    gameDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }

    private fun filterFavorites(list: List<Game>): List<Game> {
        val favorites = currentUser?.favoriteSports ?: emptyList()
        if (favorites.isEmpty()) return emptyList()
        return list.filter { it.sportType in favorites }
    }

    private fun filterJoined(list: List<Game>): List<Game> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return list.filter { it.participantIds.contains(uid) }
    }

    private fun getMockGames(): List<Game> {
        val now = System.currentTimeMillis()
        val day = 86400000L
        val sports = listOf(
            "Football" to "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=500",
            "Basketball" to "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=500",
            "Tennis" to "https://images.unsplash.com/photo-1542144582-1ba00456b5e3?q=80&w=500",
            "Volleyball" to "https://plus.unsplash.com/premium_photo-1708696237508-37eb0c43cab4?q=80&w=500",
            "Padel" to "https://plus.unsplash.com/premium_photo-1708692919998-e3dc853ef8a8?q=80&w=500",
            "Yoga" to "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=500",
            "Running" to "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?q=80&w=500"
        )
        val locations = listOf(
            Triple("Park HaYarkon, Tel Aviv", 32.1034, 34.8059),
            Triple("Sacher Park, Jerusalem", 31.7766, 35.2074),
            Triple("Sportek South, Tel Aviv", 32.0456, 34.7933),
            Triple("Charles Clore Park, Tel Aviv", 32.0625, 34.7594),
            Triple("Carmel Beach, Haifa", 32.8021, 34.9547),
            Triple("National Park, Ramat Gan", 32.0455, 34.8239),
            Triple("Teddy Stadium Court, Jerusalem", 31.7513, 35.1906),
            Triple("La Gweira, Rishon LeZion", 31.9774, 34.7479)
        )

        val dummyGames = mutableListOf<Game>()
        val random = java.util.Random()

        for (i in 0..30) {
            val sport = sports[random.nextInt(sports.size)]
            val location = locations[random.nextInt(locations.size)]
            val maxPlayers = when (sport.first) {
                "Football" -> 10 + random.nextInt(3) * 2
                "Basketball" -> 6 + random.nextInt(3) * 2
                "Tennis" -> 2 + random.nextInt(2) * 2
                "Yoga", "Running" -> 10 + random.nextInt(15)
                else -> 4 + random.nextInt(4)
            }
            
            // We have a fixed pool of 10 dummy users.
            val allUserIds = (1..10).map { "dummy_user_$it" }
            val currentPlayersCount = random.nextInt(minOf(maxPlayers, 10)) + 1
            val participantIds = allUserIds.shuffled().take(currentPlayersCount)
            
            val gameTime = now + (i * day) + (random.nextInt(10) * 3600000L)
            val uniqueId = "dummy_${System.currentTimeMillis()}_$i"
            
            dummyGames.add(
                Game(
                    gameId = uniqueId,
                    creatorId = participantIds.first(),
                    sportType = sport.first,
                    locationName = location.first,
                    currentPlayers = participantIds.size,
                    maxPlayers = maxPlayers,
                    courtImageUrl = sport.second,
                    participantIds = participantIds,
                    dateTime = gameTime.toString(),
                    latitude = location.second,
                    longitude = location.third
                )
            )
        }
        return dummyGames
    }

    fun generateDummyData() {
        viewModelScope.launch {
            seedDataToFirebase(getMockGames())
            fetchGames()
        }
    }

    private fun seedDataToFirebase(games: List<Game>) {
        viewModelScope.launch {
            // Seed Users with real names and varied photos
            val dummyUsers = listOf(
                User("dummy_user_1", "alex@example.com", "Alex", "Rivera", 28, "https://i.pravatar.cc/150?u=alex", city = "Tel Aviv"),
                User("dummy_user_2", "jordan@example.com", "Jordan", "Smith", 24, "https://i.pravatar.cc/150?u=jordan", city = "Jerusalem"),
                User("dummy_user_3", "casey@example.com", "Casey", "Williams", 31, "https://i.pravatar.cc/150?u=casey", city = "Haifa"),
                User("dummy_user_4", "sam@example.com", "Sam", "Taylor", 22, "https://i.pravatar.cc/150?u=sam", city = "Rishon LeZion"),
                User("dummy_user_5", "morgan@example.com", "Morgan", "Brown", 29, "https://i.pravatar.cc/150?u=morgan", city = "Netanya"),
                User("dummy_user_6", "taylor@example.com", "Taylor", "Swift", 26, "https://i.pravatar.cc/150?u=taylor", city = "Tel Aviv"),
                User("dummy_user_7", "chris@example.com", "Chris", "Evans", 33, "https://i.pravatar.cc/150?u=chris", city = "Petah Tikva"),
                User("dummy_user_8", "emma@example.com", "Emma", "Watson", 25, "https://i.pravatar.cc/150?u=emma", city = "Haifa"),
                User("dummy_user_9", "david@example.com", "David", "Beckham", 35, "https://i.pravatar.cc/150?u=david", city = "Tel Aviv"),
                User("dummy_user_10", "serena@example.com", "Serena", "Williams", 30, "https://i.pravatar.cc/150?u=serena", city = "Jerusalem")
            )
            
            dummyUsers.forEach { user ->
                userRepository.saveUser(user.copy(favoriteSports = listOf("Football", "Yoga", "Padel").shuffled().take(2)))
            }

            // Seed Games
            games.forEach { gameRepository.createGame(it) }
        }
    }

    fun joinGame(gameId: String, userId: String) {
        viewModelScope.launch {
            if (gameRepository.joinGame(gameId, userId)) {
                // Send a "Joined" system message to the chat
                val user = currentUser
                if (user != null) {
                    val systemMessage = com.example.whoplays.models.Message(
                        senderId = "system",
                        senderName = "System",
                        text = "${user.firstName} ${user.lastName} has joined the game! 👋",
                        timestamp = System.currentTimeMillis()
                    )
                    com.example.whoplays.repositories.ChatRepository().sendMessage(gameId, systemMessage)
                }
                fetchGames()
            }
        }
    }

    fun leaveGame(gameId: String, userId: String) {
        viewModelScope.launch {
            if (gameRepository.leaveGame(gameId, userId)) fetchGames()
        }
    }

    suspend fun getParticipants(uids: List<String>): List<User> {
        return userRepository.getUsersByIds(uids)
    }
}
