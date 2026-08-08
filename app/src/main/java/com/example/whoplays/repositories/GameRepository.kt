package com.example.whoplays.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.example.whoplays.models.Game
import com.example.whoplays.utils.Constants
import kotlinx.coroutines.tasks.await

class GameRepository {

    private val db = FirebaseFirestore.getInstance()
    private val gamesCollection = db.collection(Constants.GAMES_COLLECTION)

    // שליפה פשוטה ללא מחיקה אוטומטית (כדי למנוע תקלות)
    suspend fun getAvailableGames(): List<Game> {
        return try {
            val snapshot = gamesCollection.get().await()
            snapshot.toObjects(Game::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createGame(game: Game): Boolean {
        return try {
            val documentReference = if (game.gameId.isEmpty()) gamesCollection.document() else gamesCollection.document(game.gameId)
            val newGame = game.copy(gameId = documentReference.id)
            documentReference.set(newGame).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun joinGame(gameId: String, userId: String): Boolean {
        return try {
            val gameRef = gamesCollection.document(gameId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(gameRef)
                val currentGame = snapshot.toObject(Game::class.java) ?: return@runTransaction
                if (currentGame.currentPlayers < currentGame.maxPlayers) {
                    val newParticipants = currentGame.participantIds.toMutableList()
                    if (!newParticipants.contains(userId)) {
                        newParticipants.add(userId)
                        transaction.update(gameRef, "participantIds", newParticipants)
                        transaction.update(gameRef, "currentPlayers", newParticipants.size)
                    }
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun leaveGame(gameId: String, userId: String): Boolean {
        return try {
            val gameRef = gamesCollection.document(gameId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(gameRef)
                val currentGame = snapshot.toObject(Game::class.java) ?: return@runTransaction
                val newParticipants = currentGame.participantIds.toMutableList()
                newParticipants.remove(userId)
                if (newParticipants.isEmpty()) {
                    transaction.delete(gameRef)
                } else {
                    transaction.update(gameRef, "participantIds", newParticipants)
                    transaction.update(gameRef, "currentPlayers", newParticipants.size)
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
