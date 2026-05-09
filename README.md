# BudgetSense

Android budgeting app (Kotlin, Jetpack Compose, Material 3) with **Firebase Authentication (Google)** and **Cloud Firestore** for the user profile, plus **Room** for on-device transactions, budgets, bills, and savings goals so the core flows work offline.

## What's new (2.0.3)

- **Home — Savings:** per-goal circular progress rings (saved total in the center + % of target); cleaner “Goal progress” block without redundant month breakdown on cards.
- **Insights:** deposit-based savings tips (e.g. this month vs last, strongest month, largest deposit; honors Hide balance).
- **Navigation:** pill bar scrolls horizontally on narrow devices so Account is always reachable.
- **UI:** ongoing light-theme polish across main screens and charts (see About in the app for the full bullet list shown to users).

## Clarification: Auth vs Firestore

- **Google sign-in** is handled by **Firebase Authentication**, not by Firestore.
- **Firestore** stores the `users/{uid}` document (profile fields synced with Room).
- **Room** is the primary store for transactions and related entities on the device (your list called for offline SQLite/Room; syncing those documents to Firestore is a logical next step).

## Open in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended).
2. **File → Open** this folder (`BudgetSense`). Let Gradle sync; if the wrapper is missing, use **File → Settings → Build, Execution, Deployment → Build Tools → Gradle** and run sync so Android Studio generates `gradlew` / wrapper as needed.
3. The Android application id is **`com.amdevstudio.budgetsense`** (matches `app/google-services.json` for project **budgetsense-930c0**).
4. If you use a different Firebase project, replace **`app/google-services.json`** with the file from the console for the same package name.
5. In Firebase Console, enable **Authentication → Google**, and add your **SHA-1** / **SHA-256** for debug and release keystores.
6. Copy the **Web client ID** (Project settings → Your apps → OAuth 2.0 Web client) into `app/src/main/res/values/strings.xml` as `default_web_client_id` (required for `requestIdToken`).
7. Create a Firestore database and apply the rules in `firestore.rules` (restricts each `users/{uid}` subtree to the signed-in user).

### Example Firestore rules (starter)

Profile doc and subcollections used by this app:
- `users/{uid}`
- `users/{uid}/transactions/{txId}`
- `users/{uid}/bill_reminders/{billId}`
- `users/{uid}/savings_goals/{goalId}`
- `users/{uid}/savings_contributions/{contributionId}`

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /transactions/{txId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /bill_reminders/{billId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /savings_goals/{goalId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /savings_contributions/{contributionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

## UI direction

Visual direction (soft cards, pastel surfaces, playful accents) aligns with a modern “animated app UI” mood boards such as [this Pinterest pin](https://pin.it/23DFJ2va5). App seed colors live in `ui/theme/Color.kt`; Material 3 dynamic color is off by default so the brand reads consistently.

## What is implemented vs roadmap

| Area | Status |
|------|--------|
| Google sign-in, profile doc in Firestore, Room profile | In place |
| Transactions in Room + sync to `users/{uid}/transactions/{id}` (pull on login, push on save/delete) | In place |
| Dashboard, budget caps, bills, savings, rule-based insights | In place |
| Charts/reports PDF/CSV, WorkManager notifications, PIN/biometrics | Not built yet — natural extensions on this base |

## Module layout

- `data/local` — Room entities, DAOs, database.
- `data/repository` — Auth, profile (Room + Firestore), transactions, budgets, bills, savings.
- `domain` — Categories, time ranges, money formatting, insight rules.
- `ui` — Compose theme, navigation, screens.
