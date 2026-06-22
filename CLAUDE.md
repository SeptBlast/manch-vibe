# Solace — Codebase Guide for Claude Code

## What this project is

Solace is a mental-health / social-emotional wellness app for Android (Kotlin + Jetpack Compose + Material 3) and iOS (Swift + SwiftUI). Both platforms share a single conceptual **remote UI config model** so the backend can control product behaviour (sections, labels, experiments, theme) without releasing new app builds.

**Backend:** Firebase (Auth, Firestore, Storage, Remote Config, Analytics, Crashlytics).

## Directory layout

```
vibe/
├── android/app/src/main/java/com/solace/ui/
│   ├── theme/           Color.kt  Type.kt  Theme.kt
│   ├── config/          UiConfigModels.kt  ConfigurableSection.kt
│   ├── screens/         LoginScreen.kt  HomeScreen.kt
│   └── components/      PrimaryButton.kt  SelectionChip.kt
│                        ProfileCard.kt  SectionHeader.kt
│                        EmojiMoodSelector.kt
├── ios/Solace/
│   ├── Theme/           AppTheme.swift  DesignTokens.swift
│   ├── Config/          UiConfigModels.swift  ConfigurableSectionView.swift
│   ├── Screens/         LoginView.swift  HomeView.swift
│   └── Components/      PrimaryButton.swift  SelectionChip.swift
│                        ProfileCardView.swift  SectionHeaderView.swift
│                        EmojiMoodSelectorView.swift
└── product_designs/     Figma screenshots (source of truth for design)
```

## Core architecture rules

### Config-driven rendering
Every visible screen section is driven by `SolaceUiConfig` (decoded from backend JSON). Never hardcode copy, section visibility, or tab lists — always read from config models.

- Android: `UiConfigModels.kt` → `ConfigurableSection.kt` → screen composables
- iOS: `UiConfigModels.swift` → `ConfigurableSectionView.swift` → screen views

### Authentication
**Allowed providers only:** `EMAIL`, `GOOGLE`, `APPLE`.  
**Never add:** phone number, OTP, SMS, or mobile number fields anywhere in onboarding or login flows. This is a hard product constraint.

Provider list is controlled by `LoginScreenConfig.providers` from backend — if `APPLE` is removed from the list, the Apple button disappears automatically.

### Design tokens
- Android: `Color.kt`, `Type.kt`, `Theme.kt` — all Material 3 tokens
- iOS: `DesignTokens.swift`, `AppTheme.swift`
- The two sets must stay in sync. Primary brand colour: `#4ECDC4` (teal). Background dark: `#120B3C`.

## Shared remote-config JSON schema

```json
{
  "schemaVersion": 1,
  "themeVariant": "DEFAULT",
  "experimentFlags": { "enableChat": true, "enableJournal": true, "enableLikes": true },
  "loginScreen": {
    "visible": true,
    "tagline": "Discover yourself. We'll help guide you.",
    "providers": ["EMAIL", "GOOGLE", "APPLE"],
    "emailCtaLabel": "Continue with Email",
    "googleCtaLabel": "Continue with Google",
    "appleCtaLabel": "Continue with Apple"
  },
  "homeScreen": {
    "sections": [
      { "id": "profile_feed", "type": "PROFILE_FEED", "visible": true, "order": 0, "title": "Discover" },
      { "id": "likes_grid",   "type": "LIKES_GRID",   "visible": true, "order": 1, "title": "Likes you" }
    ],
    "bottomNav": [
      { "id": "home",    "label": "Home",    "icon": "home",    "visible": true },
      { "id": "journal", "label": "Journal", "icon": "book",    "visible": true },
      { "id": "emotion", "label": "Emotion", "icon": "emoji",   "visible": true },
      { "id": "likes",   "label": "Likes",   "icon": "heart",   "visible": true },
      { "id": "chat",    "label": "Chat",    "icon": "chat",    "visible": true }
    ]
  }
}
```

## Firebase setup checklist

### One-time project setup
1. Create a Firebase project at console.firebase.google.com
2. Enable **Authentication** → Sign-in providers: Email/Password, Google, Apple
3. Enable **Firestore** (production mode) → deploy rules with `firebase deploy --only firestore:rules,firestore:indexes`
4. Enable **Storage** → deploy rules with `firebase deploy --only storage`
5. Enable **Remote Config** → import `remoteconfig.template.json` via `firebase deploy --only remoteconfig`
6. Enable **Analytics** and **Crashlytics**

### Android
- Download `google-services.json` from Firebase Console → place in `android/app/`
- In `AuthViewModel.kt` replace `YOUR_WEB_CLIENT_ID_HERE` with the OAuth Web Client ID from Firebase Console → Authentication → Google provider

### iOS
- Download `GoogleService-Info.plist` from Firebase Console → add to Xcode target (not just folder)
- In `AuthRepository.swift` replace `YOUR_CLIENT_ID_HERE` with the `CLIENT_ID` value from `GoogleService-Info.plist`
- Add Firebase packages via Xcode → File → Add Package Dependencies using `ios/Package.swift` as reference

## Firebase data architecture

```
Firestore collections:
  users/{uid}                      public profile (feed, profile card)
  journals/{uid}/entries/{id}      private journal entries
  likes/{toUid}/received/{fromUid} who liked whom
  chats/{chatId}                   conversation header (participants, lastMessage)
  chats/{chatId}/messages/{id}     individual messages

Firebase Storage:
  avatars/{uid}.jpg                profile images (max 5 MB, owner-write)

Remote Config:
  ui_config_json                   full SolaceUiConfig JSON string
```

### Data layer pattern (both platforms)
```
UI Screen / View
  └── ViewModel  (collects/publishes state)
        └── Repository  (wraps Firebase SDK)
              └── Firebase SDK  (Auth, Firestore, Storage, RemoteConfig)
```

- **Repositories** are `@Singleton` (Android Hilt) / `static let shared` (iOS) — never instantiate more than once
- **ViewModels** hold `StateFlow` (Android) / `@Published` (iOS) — UI observes these only
- **Never call Firebase SDK directly from a screen or composable**

## Adding a new section type

1. Add the new value to `HomeSectionType` enum in both `UiConfigModels.kt` and `UiConfigModels.swift`.
2. Add a renderer case in `ConfigurableSection.kt` (Android `when` branch) and `ConfigurableSectionView.swift` (Swift `switch` case).
3. The `HomeScreen` / `HomeView` iterates `visibleSections` automatically — no other changes needed.

## Adding a new tab

Add a `BottomNavTabConfig` entry to `HomeScreenConfig.bottomNav` defaults on both platforms, add the `case` in `iconForTab()` (Android) / `systemImage(for:)` (iOS), and add a `case` in the tab body `when`/`switch`.

## Mood colour palette

Controlled by `DefaultMoodPalette` / `defaultMoodPalette`. Eight options: Calm & Relaxed, Energetic & Passionate, Happy & Optimistic, North Reflective, Peaceful & Grounded, Confused & Overwhelmed, Neutral & Numb, Warm & Hopeful. The key-to-colour mapping lives in one place per platform — don't scatter hex values.

## TODOs scattered in code (contract points)

- `LoginScreen.kt` / `LoginView.swift` — connect to AuthViewModel / Coordinator
- `ConfigurableSection.kt` / `ConfigurableSectionView.swift` — connect JournalPreview and EmotionPrompt sections to their ViewModels
- `HomeScreen.kt` / `HomeView.swift` — inject real ViewModels per tab
- `UiConfigModels.kt/.swift` — fetch config from remote endpoint on app start; cache for offline

## Coding conventions

- **No phone/OTP fields** — enforced by product spec, not by lint. Reject any PR that adds them.
- Keep Android and iOS models **conceptually aligned** (same field names, same enum cases) even though the syntax differs.
- Composable/View previews are required on every new screen and reusable component.
- Design tokens are the only acceptable source for colours, spacing, and radius values. No inline hex literals outside `Color.kt` / `DesignTokens.swift`.
- Remote config fields must have sensible defaults so the app renders correctly even when the network call fails.
