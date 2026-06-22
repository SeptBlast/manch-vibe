# Solace — ProGuard / R8 rules

# Kotlin
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.** { volatile <fields>; }

# Hilt / Dagger
-keepclasseswithmembers class * { @dagger.* <methods>; }
-keepclasseswithmembers class * { @javax.inject.* <methods>; }

# Firebase Auth
-keepattributes Signature
-keepattributes *Annotation*

# Firebase Firestore — keep data model classes used with toObject<T>()
-keep class com.solace.** { *; }
-keepclassmembers class com.solace.** { *; }

# Gson — keep serialised model fields
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Compose compiler generates synthetic classes
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
