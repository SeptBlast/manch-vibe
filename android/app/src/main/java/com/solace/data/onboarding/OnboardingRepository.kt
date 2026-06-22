package com.solace.data.onboarding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.solace.ui.onboarding.OnboardingAnswers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// Firestore: users/{uid}.onboardingComplete = true
//            users/{uid} gets all profile fields populated from answers
// ---------------------------------------------------------------------------

@Singleton
class OnboardingRepository @Inject constructor() {

    private val db: FirebaseFirestore = Firebase.firestore

    /** Returns true if the user has already completed onboarding. */
    suspend fun isOnboardingComplete(uid: String): Boolean {
        val doc = db.collection("users").document(uid).get().await()
        return doc.getBoolean("onboardingComplete") == true
    }

    /**
     * Writes all collected onboarding answers to the user's Firestore document.
     * Sets onboardingComplete = true so this screen is never shown again.
     */
    suspend fun saveOnboardingAnswers(uid: String, answers: OnboardingAnswers): Result<Unit> =
        runCatching {
            db.collection("users").document(uid).set(
                hashMapOf(
                    "username"          to answers.username,
                    "moodColor"         to answers.moodColor.name,
                    "moodLabel"         to answers.moodLabel,
                    "bio"               to answers.feelingSentence,
                    "vibes"             to answers.vibes,
                    "minutesPerDay"     to "2 minutes a day",
                    "moodDescriptors"   to answers.moodDescriptors,
                    "feelingNow"        to answers.feelingNow,
                    "feelingLike"       to answers.feelingLike,
                    "strugglingWith"    to answers.strugglingWith,
                    "soughtSupport"     to answers.soughtSupport,
                    "openness"          to answers.openness,
                    "hopeToGain"        to answers.hopeToGain,
                    "feelingSentence"   to answers.feelingSentence,
                    "isVisible"         to true,
                    "onboardingComplete" to true,
                    "createdAt"         to Timestamp.now(),
                    "updatedAt"         to Timestamp.now(),
                )
            ).await()
        }
}
