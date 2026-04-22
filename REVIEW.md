# Code Review & Development Plan

## What These Changes Do

The branch introduces the core app functionality — a **notes app with share-target support** — built on top of the previously established navigation skeleton (Voyager was removed, now using Compose Destinations + Hilt).

### Layer by layer

**Persistence (Room)**
- Migrated from SQLDelight to Room. `AppDatabase` v1 with a single `note` table.
- `NoteDao` supports full CRUD + reactive `Flow<List<NoteEntity>>` observation.
- `NoteStoreImpl` bridges the DAO to the domain `NoteStore` interface. Insert and update both go through `OnConflictStrategy.REPLACE` — Room uses `id=0` as "new record" (autoGenerate), which works but is a subtle sentinel-value contract.
- `LocalDateTimeConverter` stores `LocalDateTime` as ISO-8601 strings.

**DI**
- `DatabaseModule` — provides `AppDatabase` and `NoteDao` as singletons.
- `StoreModuleBinds` — binds `NoteStoreImpl → NoteStore`.
- `CoroutinesModule` — exposes named `@IoDispatcher`, `@MainDispatcher`, etc. (currently unused by the store).

**Domain model**
- `Note` data class with `@Parcelize` for navigation, 5 preset colors as companions.

**Notes list feature**
- Orbit MVI: `NotesViewModel` observes DB, handles delete-with-undo-restore and sort order toggling.
- `NotesScreen`: staggered 2-column grid, FAB to create, per-item share (text via Android Sharesheet) and delete with Snackbar undo.

**Add/Edit feature**
- `AddEditNoteViewModel` saves a note and navigates back.
- `AddEditNoteScreen`: animated color picker, title/content fields with placeholder hints, FAB that only appears when at least one field has content.

**Share-to-Save feature (work-in-progress)**
- `ShareHandlerActivity` registered in the manifest for `SEND`/`SEND_MULTIPLE` on `text/*` and `*/*`.
- It captures an image URI, stores it in `SharedDataRepository` (a singleton `StateFlow`), then launches `MainActivity`.
- `MainViewModel` exposes the URI; `MainActivity` currently shows a stub `AlertDialog` when a URI arrives.

---

## Bugs & Issues to Fix

**1. Add/Edit screen doesn't load existing note data**
When editing a note (`note != null`), the `AddEditNoteViewModel` is never told about it. `title`, `content`, and `color` all start empty/default. The note object is passed as a nav arg but only used to read `note?.id` at save time. The ViewModel needs an init block to pre-populate the state from the passed note.

**2. Sort order is tracked but never applied**
`NotesState.noteOrder` is toggled correctly, but `NotesScreen` renders `state.notes` as-is. The sorted list is never computed. The sorting logic needs to be applied — either in the ViewModel's `observeNotes` collector or before rendering.

**3. Share image flow is incomplete**
`ShareHandlerActivity` only handles `EXTRA_STREAM` (image URIs). The `text/*` intent filter is registered in the manifest but `EXTRA_TEXT` content is never extracted. The `MainActivity` side shows a placeholder `AlertDialog` instead of opening `AddEditScreen` with content pre-filled.

**4. SQLDelight still in build.gradle.kts**
The plugin (`alias(libs.plugins.sqldelight)`), driver dependency (`sqldelight.android.driver`), and `sqldelight {}` config block are all still present even though Room has replaced it. This will cause a build conflict or dead weight.

**5. `onNewIntent` in MainActivity is commented out**
If `MainActivity` is already in the foreground when a share comes in, `FLAG_ACTIVITY_SINGLE_TOP` causes `onNewIntent` to fire — but that handler is commented out. This means the second share will not be processed.

**6. Snackbar shown inline instead of via side effect** (`NotesScreen.kt:179`)
There's a `// TODO: move to an effect` comment — the Snackbar is launched directly in the composable scope rather than through an Orbit `NotesSideEffect`, which breaks the MVI contract and makes it harder to test.

**7. `NoteStoreImpl` doesn't use `@IoDispatcher`**
DB calls aren't explicitly dispatched to `Dispatchers.IO`. Room itself may handle this internally for suspend functions, but `observeNotes()` returns a Flow that emits on a background thread — it's worth being explicit, especially given that `CoroutinesModule` was set up specifically for this.

---

## Development Plan

### Priority 1 — Fix blockers

1. **Remove SQLDelight** — strip the plugin, driver dependency, and `sqldelight {}` block from `build.gradle.kts` and `libs.versions.toml` (also clean up the leftover `voyager` version entry).

2. **Fix AddEditScreen pre-population** — in `AddEditNoteViewModel`, add an init block (or handle it via an event) that seeds `title`, `content`, and `color` from the passed-in `Note` when `note.id != null`.

3. **Apply sort order** — in `NotesViewModel`, after collecting from `observeNotes()`, apply the `state.noteOrder` comparator to the list before reducing.

### Priority 2 — Complete the share flow

4. **Handle `EXTRA_TEXT` in `ShareHandlerActivity`** — extract the text content (and optionally URL) and pass it through `SharedDataRepository` alongside or instead of image URI.

5. **Wire shared content to `AddEditScreen`** — in `MainActivity` (or via `MainViewModel` Orbit side effect), when `imageToDisplay` / `sharedText` becomes non-null, navigate to `AddEditScreenDestination` with pre-filled content and clear the repository value after consuming it.

6. **Restore `onNewIntent` in `MainActivity`** — uncomment and delegate to `MainViewModel` so repeat shares work correctly.

### Priority 3 — Quality / architecture

7. **Move Snackbar to a side effect** — add a `NotesSideEffect.ShowSnackbar` case and handle it in `collectSideEffect` in `NotesScreen`.

8. **Explicit IO dispatcher in `NoteStoreImpl`** — inject `@IoDispatcher` and wrap Flow emissions with `flowOn(ioDispatcher)`.

9. **Image display in `NoteItemUI` and `AddEditScreen`** — the `image` field exists in the model and entity, and `ShareHandlerActivity` captures image URIs, but nothing renders them yet. Wire up `AsyncImage` (Coil) to display images in both the list card and the edit screen.
