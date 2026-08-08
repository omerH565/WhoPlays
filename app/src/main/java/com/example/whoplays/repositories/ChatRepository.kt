package com.example.whoplays.repositories

import com.example.whoplays.models.Message
import com.example.whoplays.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getMessages(gameId: String): Flow<List<Message>> = callbackFlow {
        val subscription = db.collection(Constants.GAMES_COLLECTION)
            .document(gameId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(gameId: String, message: Message): Boolean {
        return try {
            val messageRef = db.collection(Constants.GAMES_COLLECTION)
                .document(gameId)
                .collection("messages")
                .document()
            
            messageRef.set(message.copy(messageId = messageRef.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
