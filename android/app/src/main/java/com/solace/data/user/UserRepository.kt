package com.solace.data.user

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.solace.ui.config.MoodColorKey
import com.solace.ui.config.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// Firestore schema
// ---------------------------------------------------------------------------
// users/{uid}
//   username: String
//   avatarUrl: String?
//   moodColor: String          (MoodColorKey name)
//   moodLabel: String
//   bio: String
//   vibes: List<String>
//   minutesPerDay: String
//   isVisible: Boolean
//   createdAt: Timestamp
//   updatedAt: Timestamp
// ---------------------------------------------------------------------------

private data class UserDocument(
    val username: String = "",
    val avatarUrl: String? = null,
    val moodColor: String = MoodColorKey.CALM_RELAXED.name,
    val moodLabel: String = "",
    val bio: String = "",
    val vibes: List<String> = emptyList(),
    val minutesPerDay: String = "2 minutes a day",
    val isVisible: Boolean = true,
)

private fun UserDocument.toProfile(uid: String) = UserProfile(
    userId = uid,
    username = username,
    avatarUrl = avatarUrl,
    moodColor = runCatching { MoodColorKey.valueOf(moodColor) }.getOrDefault(MoodColorKey.CALM_RELAXED),
    moodLabel = moodLabel,
    bio = bio,
    vibes = vibes,
    minutesPerDay = minutesPerDay,
)

private fun UserProfile.toDocument() = UserDocument(
    username = username,
    avatarUrl = avatarUrl,
    moodColor = moodColor.name,
    moodLabel = moodLabel,
    bio = bio,
    vibes = vibes,
    minutesPerDay = minutesPerDay,
)

@Singleton
class UserRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage
    private val usersCol get() = db.collection("users")

    /** Live stream of all visible profiles for the home feed. */
    fun getProfiles(): Flow<List<UserProfile>> = callbackFlow {
        val sub = usersCol
            .whereEqualTo("isVisible", true)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val profiles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<UserDocument>()?.toProfile(doc.id)
                } ?: emptyList()
                trySend(profiles)
            }
        awaitClose { sub.remove() }
    }

    /** Fetch a single profile once. */
    suspend fun getProfile(uid: String): UserProfile? {
        val doc = usersCol.document(uid).get().await()
        return doc.toObject<UserDocument>()?.toProfile(uid)
    }

    /** Create or overwrite the current user's profile. */
    suspend fun saveProfile(uid: String, profile: UserProfile): Result<Unit> = runCatching {
        val data = profile.toDocument()
        usersCol.document(uid).set(
            hashMapOf(
                "username" to data.username,
                "avatarUrl" to data.avatarUrl,
                "moodColor" to data.moodColor,
                "moodLabel" to data.moodLabel,
                "bio" to data.bio,
                "vibes" to data.vibes,
                "minutesPerDay" to data.minutesPerDay,
                "isVisible" to true,
                "updatedAt" to com.google.firebase.Timestamp.now(),
            )
        ).await()
    }

    /** Upload avatar image; returns the public download URL. */
    suspend fun uploadAvatar(uid: String, localUri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(localUri).await()
        ref.downloadUrl.await().toString()
    }

    /** Live stream of profiles that liked [uid], newest first. */
    fun getLikes(uid: String): Flow<List<UserProfile>> = callbackFlow {
        val sub = db.collection("likes").document(uid).collection("received")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val fromIds = snapshot?.documents?.mapNotNull {
                    it.getString("fromUserId")
                } ?: emptyList()

                if (fromIds.isEmpty()) { trySend(emptyList()); return@addSnapshotListener }

                // Batch fetch — Firestore `in` supports up to 30 IDs per query
                fromIds.chunked(30).forEach { chunk ->
                    usersCol.whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { docs ->
                            // Preserve received order
                            val byId = docs.documents.associate { d ->
                                d.id to d.toObject<UserDocument>()?.toProfile(d.id)
                            }
                            val profiles = fromIds.mapNotNull { byId[it] }
                            trySend(profiles)
                        }
                }
            }
        awaitClose { sub.remove() }
    }

    /** Send a like from [fromUid] to [toUid]. */
    suspend fun sendLike(fromUid: String, toUid: String): Result<Unit> = runCatching {
        db.collection("likes").document(toUid).collection("received").document(fromUid)
            .set(mapOf("fromUserId" to fromUid, "createdAt" to com.google.firebase.Timestamp.now()))
            .await()
    }
}
