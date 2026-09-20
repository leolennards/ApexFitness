# ApexFitness

ApexFitness is an Android fitness tracking app that I built with Kotlin and Jetpack Compose. It lets a user build workout routines, log sessions, track water and cardio, take on challenges and watch their progress over time. I went for a calm, premium look with a light and a dark theme.

## Features

- Sign up and sign in with email or Google (Firebase Authentication)
- Build and edit workout routines from an exercise library, and schedule them on certain days
- Run a workout session and log sets, reps and weight
- Personal records and stats, with calories worked out from MET values
- Water tracking and cardio logging
- Challenges with progress, plus achievements and levels with a small celebration screen
- Calendar and activity history
- Weekly workout reminders (AlarmManager)
- Light and dark theme, with a short fade when switching
- Rest timer between sets, and a kg or lb setting
- Delete account, which removes all of the user's data

## Built with

- Kotlin
- Jetpack Compose and Material 3
- Firebase Authentication and Cloud Firestore
- Navigation Compose, ViewModel and coroutines
- Manrope font, with a custom theme (colours, type, spacing and motion) in `ui/theme`

## Project layout

- `data/` holds the models, the Firestore repository and the calculations (calories, streaks, challenges)
- `ui/` holds one folder per screen
- `ui/theme/` holds the shared colours, type, components and animations

## Running it

1. Open the project in Android Studio.
2. Create a Firebase project and add an Android app with the package name `com.leolennards.apexfitness`.
3. Turn on Email/Password and Google sign-in under Authentication, and create a Firestore database.
4. Add your debug SHA-1 to the Firebase app (needed for Google sign-in).
5. Download `google-services.json` and put it in the `app` folder. The file `app/google-services.json.example` shows the layout. My own file is not in this repo on purpose.
6. In `gradle.properties`, change the `org.gradle.java.home` line to your own JDK or delete it.
7. Sync Gradle and run the app.

## Privacy policy

The policy page is in `docs/privacy.html` (it can be served with GitHub Pages from the `docs` folder).

## What I learned

This project taught me a lot about structuring a Compose app, keeping state in ViewModels, working with Firestore, and making a consistent design system instead of styling every screen separately.

## Author

Leo Lennards
