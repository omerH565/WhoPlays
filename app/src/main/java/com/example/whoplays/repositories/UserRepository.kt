package com.example.whoplays.repositories

import com.example.whoplays.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun saveUser(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUser(uid: String): User? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateFcmToken(uid: String, token: String): Boolean {
        return try {
            usersCollection.document(uid).update("fcmToken", token).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUsersByIds(uids: List<String>): List<User> {
        if (uids.isEmpty()) return emptyList()
        return try {
            // Firestore limit is usually 10 for 'in' queries, but for participants it should be fine for now
            val snapshot = usersCollection.whereIn("uid", uids).get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
