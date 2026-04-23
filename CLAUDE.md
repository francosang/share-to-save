# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This App Does

Android note-taking app. Users share content (text, images) from other apps → app intercepts via `ACTION_SEND` intent → opens AddEdit screen pre-populated with shared data → user saves as note.

## Build & Run

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Install on connected device
./gradlew :androidApp:installDebug

# Build release APK
./gradlew :androidApp:assembleRelease

# Run unit tests
./gradlew :androidApp:test

# Run instrumented tests
./gradlew :androidApp:connectedAndroidTest

# Run a single test class
./gradlew :androidApp:test --tests "com.jfranco.sharetosave.SomeTest"
```

Do NOT read Gradle cache files, `.aar`/`.jar` files, or compiled artifact internals to figure out library APIs — ask the user instead.

## Architecture

Single Android module (`androidApp`). No multiplatform — the root `build.gradle.kts` has unused KMP plugin stubs.

**MVI pattern via Orbit:**
- Each screen has: `State` (Parcelable), `Event` (sealed), `SideEffect` (sealed), `ViewModel` (ContainerHost)
- ViewModels use `intent { reduce { } }` for state and `postSideEffect()` for one-shot navigation/UI actions
- Screens collect side effects with `collectSideEffect { }` from orbit-compose

**Share flow:**
1. `MainActivity` receives `ACTION_SEND` intent in `onCreate`/`onNewIntent`
2. Passes to `MainViewModel` → emits `MainViewSideEffect.OnDataShared`
3. `MainActivity` collects side effect → navigates to `AddEditScreenDestination` with args (text + image URI)
4. `AddEditNoteViewModel` reads args from `SavedStateHandle` via compose-destinations

**Navigation:** compose-destinations (Ramsosta). Destinations are KSP-generated. `NavGraphs.root` in `MainActivity`. Args passed as `@Parcelize` data classes (`AddEditScreenDestinationArgs`).

**Persistence:**
- `NoteStore` interface → `NoteStoreImpl` backed by Room (`AppDatabase`, `NoteDao`, `NoteEntity`)
- `FileStorageHelper` copies shared image URIs to `filesDir/shared_images/` and saves the absolute path in Room
- Room schema exported to `androidApp/schemas/`

**DI:** Hilt. `DatabaseModule` provides `AppDatabase` + `NoteDao` as singletons. `StoreModuleBinds` binds `NoteStore` interface to `NoteStoreImpl`.

**Key domain type:** `Note` — `@Parcelize` data class with `id: Long?` (null = unsaved), `title`, `content`, `image` (file path string), `color` (Int from `Note.noteColors`).

## Stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material3 |
| Navigation | compose-destinations |
| MVI | Orbit (orbit-core, orbit-viewmodel, orbit-compose) |
| DI | Hilt |
| DB | Room |
| Images | Coil |
| Code gen | KSP |

Min SDK 26, Target/Compile SDK 35, Java 17, Kotlin 2.2.
