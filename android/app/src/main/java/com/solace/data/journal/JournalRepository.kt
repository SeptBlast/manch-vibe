package com.solace.data.journal

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
// ---------------------------------------------------------------------------
// journals/{uid}/entries/{entryId}
//   emoji: String
//   text: String
//   moodColor: String
//   createdAt: Timestamp
// ---------------------------------------------------------------------------

data class JournalEntry(
    val id: String = "",
    val emoji: String = "",
    val text: String = "",
    val moodColor: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)

@Singleton
class JournalRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore

    private fun entriesCol(uid: String) =
        db.collection("journals").document(uid).collection("entries")

    /** Live stream of a user's journal entries, newest first. */
    fun getEntries(uid: String): Flow<List<JournalEntry>> = callbackFlow {
        val sub = entriesCol(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val entries = snapshot?.documents?.map { doc ->
                    JournalEntry(
                        id = doc.id,
                        emoji = doc.getString("emoji") ?: "",
                        text = doc.getString("text") ?: "",
                        moodColor = doc.getString("moodColor") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    )
                } ?: emptyList()
                trySend(entries)
            }
        awaitClose { sub.remove() }
    }

    suspend fun addEntry(uid: String, emoji: String, text: String, moodColor: String): Result<String> =
        runCatching {
            val ref = entriesCol(uid).document()
            ref.set(
                mapOf(
                    "emoji" to emoji,
                    "text" to text,
                    "moodColor" to moodColor,
                    "createdAt" to Timestamp.now(),
                )
            ).await()
            ref.id
        }

    suspend fun deleteEntry(uid: String, entryId: String): Result<Unit> = runCatching {
        entriesCol(uid).document(entryId).delete().await()
    }
}
