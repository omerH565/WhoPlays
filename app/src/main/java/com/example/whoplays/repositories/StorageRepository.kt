package com.example.whoplays.repositories

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {

    // גישה לתיקיית השורש ב-Storage
    private val storageRef = FirebaseStorage.getInstance().reference

    // העלאת תמונת פרופיל
    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String? {
        return try {
            val imageRef = storageRef.child("profile_images/$uid.jpg")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // העלאת תמונה אסינכרונית והחזרת ה-URL (הקיים)
    suspend fun uploadCourtImage(imageUri: Uri): String? {
        return try {
            // יצירת שם קובץ רנדומלי וייחודי
            val fileName = UUID.randomUUID().toString()
            val imageRef = storageRef.child("court_images/$fileName")

            // העלאת הקובץ
            imageRef.putFile(imageUri).await()

            // משיכת לינק ההורדה הפומבי
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}