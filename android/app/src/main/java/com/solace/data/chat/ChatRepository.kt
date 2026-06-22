package com.solace.data.chat

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
// chats/{chatId}                   chatId = sorted([uid1, uid2]).join("_")
//   participants: List<String>
//   lastMessage: String
//   lastSenderId: String
//   lastMessageAt: Timestamp
//
// chats/{chatId}/messages/{msgId}
//   text: String
//   senderId: String
//   createdAt: Timestamp
// ---------------------------------------------------------------------------

data class ChatPreview(
    val chatId: String = "",
    val otherUserId: String = "",
    val lastMessage: String = "",
    val lastMessageAt: Timestamp = Timestamp.now(),
)

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)

@Singleton
class ChatRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val chatsCol get() = db.collection("chats")

    private fun chatId(uid1: String, uid2: String) =
        listOf(uid1, uid2).sorted().joinToString("_")

    /** Live stream of all chat previews for [uid]. */
    fun getChatPreviews(uid: String): Flow<List<ChatPreview>> = callbackFlow {
        val sub = chatsCol
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val previews = snapshot?.documents?.mapNotNull { doc ->
                    val participants = doc.get("participants") as? List<*> ?: return@mapNotNull null
                    val other = participants.firstOrNull { it != uid } as? String ?: return@mapNotNull null
                    ChatPreview(
                        chatId = doc.id,
                        otherUserId = other,
                        lastMessage = doc.getString("lastMessage") ?: "",
                        lastMessageAt = doc.getTimestamp("lastMessageAt") ?: Timestamp.now(),
                    )
                } ?: emptyList()
                trySend(previews)
            }
        awaitClose { sub.remove() }
    }

    /** Live stream of messages in a conversation. */
    fun getMessages(uid1: String, uid2: String): Flow<List<ChatMessage>> = callbackFlow {
        val cid = chatId(uid1, uid2)
        val sub = chatsCol.document(cid).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val messages = snapshot?.documents?.map { doc ->
                    ChatMessage(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    )
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { sub.remove() }
    }

    suspend fun sendMessage(
        senderUid: String,
        recipientUid: String,
        text: String,
    ): Result<Unit> = runCatching {
        val cid = chatId(senderUid, recipientUid)
        val now = Timestamp.now()
        val chatDoc = chatsCol.document(cid)

        // Upsert chat thread header
        chatDoc.set(
            mapOf(
                "participants" to listOf(senderUid, recipientUid),
                "lastMessage" to text,
                "lastSenderId" to senderUid,
                "lastMessageAt" to now,
            ),
            com.google.firebase.firestore.SetOptions.merge(),
        ).await()

        // Append message
        chatDoc.collection("messages").add(
            mapOf(
                "text" to text,
                "senderId" to senderUid,
                "createdAt" to now,
            )
        ).await()
    }
}
