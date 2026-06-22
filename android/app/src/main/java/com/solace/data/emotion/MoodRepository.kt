package com.solace.data.emotion

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// Firestore schema
// moods/{uid}/logs/{logId}
//   emoji: String
//   moodColorKey: String   (MoodColorKey.name)
//   note: String
//   createdAt: Timestamp
// ---------------------------------------------------------------------------

data class MoodLog(
    val id: String = "",
    val emoji: String = "",
    val moodColorKey: String = "",
    val note: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)

@Singleton
class MoodRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore

    private fun logsCol(uid: String) =
        db.collection("moods").document(uid).collection("logs")

    fun getLogs(uid: String, limit: Long = 30): Flow<List<MoodLog>> = callbackFlow {
        val sub = logsCol(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val logs = snapshot?.documents?.map { doc ->
                    MoodLog(
                        id = doc.id,
                        emoji = doc.getString("emoji") ?: "",
                        moodColorKey = doc.getString("moodColorKey") ?: "",
                        note = doc.getString("note") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    )
                } ?: emptyList()
                trySend(logs)
            }
        awaitClose { sub.remove() }
    }

    suspend fun addLog(
        uid: String,
        emoji: String,
        moodColorKey: String,
        note: String,
    ): Result<Unit> = runCatching {
        logsCol(uid).add(
            mapOf(
                "emoji" to emoji,
                "moodColorKey" to moodColorKey,
                "note" to note,
                "createdAt" to Timestamp.now(),
            )
        ).await()
    }
}
