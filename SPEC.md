# SPEC.md — share-to-save

## §G Goal

Android note-taking app. Intercept OS share intents (text, image, audio, video, any file) → auto-save note immediately → show tag picker + reminder panel inline. Secondary: create/edit/delete/sort/filter notes manually. Tags managed from drawer. Reminders scheduled via WorkManager, survive reboots.

---

## §C Constraints

- Min SDK 26, Target/Compile SDK 35, Java 17, Kotlin 2.2.10
- Jetpack Compose BOM 2024.06.00, Material3 1.3.2
- Orbit MVI 10.0.0 (orbit-core, orbit-viewmodel, orbit-compose)
- Hilt 2.57.1 + hilt-navigation-compose 1.2.0
- Room 2.7.2 (room-ktx, room-runtime, room-compiler via KSP)
- Compose Destinations 2.2.0 (KSP-generated nav graph)
- Coil 2.7.0 (async image/file thumbnail loading)
- WorkManager 2.9.x — reminder scheduling (persists across reboots natively)
- KSP 2.2.10-2.0.2
- No network. No multiplatform.
- All shared files stored in `filesDir/shared_files/{type}/` as absolute path string in Room.
- Permissions: `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS` (Android 13+), `USE_EXACT_ALARM` (Android 12+).

---

## §I Interfaces

### Intent filters (AndroidManifest.xml)

| Action | Data type | Purpose |
|---|---|---|
| `ACTION_MAIN` + `LAUNCHER` | — | App launch |
| `ACTION_CREATE_NOTE` | — | Custom note creation |
| `ACTION_SEND` | `text/*`, `image/*`, `audio/*`, `video/*`, `*/*` | Single-item share |
| `ACTION_SEND_MULTIPLE` | `*/*` | Multi-item share |

MainActivity: exported, `singleTask`.  
BroadcastReceiver: `BootReceiver` — `RECEIVE_BOOT_COMPLETED` → reschedule all active reminders.

### Navigation (Compose Destinations)

| Destination | Start | Args |
|---|---|---|
| `NotesListScreen` | yes | — |
| `AddEditScreen` | no | `AddEditScreenDestinationArgs(note: Note?, text: String?, fileUri: Uri?, mimeType: String?, fromShare: Boolean)` |

Drawer (modal, not a destination) embedded in `NotesListScreen`:
- All Notes section
- Tags section (list of tags, tap to filter notes)
- Reminders section (list of all active reminders)

### NoteStore interface

```kotlin
suspend fun getNotes(): List<Note>
fun observeNotes(): Flow<List<Note>>
suspend fun save(note: Note): Note
suspend fun getNote(id: Long): Note?
suspend fun deleteNote(id: Long)
suspend fun setNoteTags(noteId: Long, tagIds: List<Long>)
fun observeNotesByTag(tagId: Long): Flow<List<Note>>
```

### TagStore interface

```kotlin
fun observeTags(): Flow<List<Tag>>
suspend fun save(tag: Tag): Tag
suspend fun delete(id: Long)
suspend fun getTags(): List<Tag>
suspend fun getTagsForNote(noteId: Long): List<Tag>
```

### ReminderStore interface

```kotlin
fun observeReminders(): Flow<List<Reminder>>
suspend fun save(reminder: Reminder): Reminder
suspend fun delete(id: Long)
suspend fun getRemindersForNote(noteId: Long): List<Reminder>
suspend fun getAllActive(): List<Reminder>
```

### NotificationScheduler interface

```kotlin
interface NotificationScheduler {
  fun schedule(reminder: Reminder, note: Note)
  fun cancel(reminderId: Long)
  fun rescheduleAll(reminders: List<Pair<Reminder, Note>>)
}
// impl: WorkManager OneTimeWorkRequest, tag = "reminder_{id}"
// Data: reminderId, noteId, noteTitle, triggerEpochMillis
// Worker: ReminderWorker — posts notification, cleans up past reminders from DB
```

### NoteDao (Room)

```kotlin
suspend fun selectAll(): List<NoteEntity>
fun observeAll(): Flow<List<NoteEntity>>
fun observeByTag(tagId: Long): Flow<List<NoteEntity>>
suspend fun selectNote(id: Long): NoteEntity?
suspend fun insert(note: NoteEntity): Long   // REPLACE conflict
suspend fun delete(id: Long)
```

### TagDao (Room)

```kotlin
fun observeAll(): Flow<List<TagEntity>>
suspend fun selectAll(): List<TagEntity>
suspend fun insert(tag: TagEntity): Long
suspend fun delete(id: Long)
suspend fun getTagsForNote(noteId: Long): List<TagEntity>
```

### NoteTagCrossRef (Room junction table)

```kotlin
@Entity(primaryKeys = ["noteId", "tagId"])
data class NoteTagCrossRef(val noteId: Long, val tagId: Long)
```

### ReminderDao (Room)

```kotlin
fun observeAll(): Flow<List<ReminderEntity>>
suspend fun insert(reminder: ReminderEntity): Long
suspend fun delete(id: Long)
suspend fun getForNote(noteId: Long): List<ReminderEntity>
suspend fun getAllActive(): List<ReminderEntity>
```

### FileStorageHelper

```kotlin
fun saveSharedFileInternal(context: Context, uri: Uri, mimeType: String?): String?
// detects subdir from mimeType: image/ → images/, video/ → videos/, audio/ → audio/, else → files/
// copies → filesDir/shared_files/{subdir}/{UUID}.{ext}
// returns absolute path or null on error
```

### ViewModels (Orbit MVI)

**MainViewModel** — handles share intents  
Events: `OnDataShared(text: String?, fileUri: Uri?, mimeType: String?)`  
SideEffects: `OnDataShared(text: String?, fileUri: Uri?, mimeType: String?)`

**NotesViewModel** — list screen + drawer  
State: `NotesState(notes, noteOrder, isOrderSectionVisible, recentDeletedNote, tags, activeTagFilter: Long?, isDrawerOpen, reminders)`  
Events: Order, DeleteNote, RestoreNote, ToggleOrderSection, AddEditNoteScreen, ToggleDrawer, SelectTag(id?), DeleteReminder  
SideEffects: NavigateToAddEditNoteScreen, ShowSnackbar

**AddEditNoteViewModel** — create/edit/share screen  
State: `AddEditNoteState(title, content, attachmentPath, attachmentMimeType, color, noteId, isNoteSaved, saveEnabled, isFromShare, tags, selectedTagIds, reminders, isTagPanelExpanded, isReminderPanelExpanded)`  
Events: ChangeTitleFocus, ChangeContentFocus, ChangeColor, SaveNote, ToggleTag(tagId), AddReminder(type, customTime?), RemoveReminder(id), ToggleTagPanel, ToggleReminderPanel  
SideEffects: `NavigateBackWithResult(note: Note)`

**TagsViewModel** — drawer tag management  
State: `TagsState(tags: List<Tag>)`  
Events: AddTag(name, color), DeleteTag(id), EditTag(tag)

**RemindersViewModel** — drawer reminders overview  
State: `RemindersState(reminders: List<ReminderWithNote>)`  
Events: DeleteReminder(id)

### Domain models

```kotlin
@Parcelize
data class Note(
  val id: Long?,
  val title: String?,
  val content: String?,
  val attachmentPath: String?,       // absolute file path, replaces old `image`
  val attachmentMimeType: String?,   // e.g. "image/jpeg", "audio/mp4", "video/mp4"
  val created: LocalDateTime,
  val edited: LocalDateTime?,
  val color: Int,
  val tagIds: List<Long> = emptyList()
)

@Parcelize
data class Tag(val id: Long?, val name: String, val color: Int)

@Parcelize
data class Reminder(
  val id: Long?,
  val noteId: Long,
  val triggerAt: LocalDateTime,
  val type: ReminderType
)

enum class ReminderType { DAY, WEEK, MONTH, YEAR, CUSTOM }

// Derived at display time — not stored
enum class ContentType { TEXT, LINK, IMAGE, VIDEO, AUDIO, FILE }
// ContentType derived: mimeType "image/*"→IMAGE, "video/*"→VIDEO, "audio/*"→AUDIO,
//   null+content has URL→LINK, null+no attachment→TEXT, else→FILE
```

Note.noteColors: RedOrange, LightGreen, Violet, BabyBlue, RedPink.

### DI modules

- `DatabaseModule` — provides `AppDatabase` + `NoteDao`, `TagDao`, `ReminderDao` (Singleton)
- `StoreModuleBinds` — binds `NoteStoreImpl`→`NoteStore`, `TagStoreImpl`→`TagStore`, `ReminderStoreImpl`→`ReminderStore` (Singleton)
- `CoroutinesModule` — `@DefaultDispatcher`, `@IoDispatcher`, `@MainDispatcher`, `@MainImmediateDispatcher`
- `NotificationModule` — provides `NotificationScheduler` impl (WorkManager-backed, Singleton)

---

## §V Invariants

V1. `Note.id == null` → unsaved. After `noteDao.insert()`, returned id > 0.  
V2. `MainActivity` must handle `onNewIntent()` for repeat `ACTION_SEND` when activity already foreground (`singleTask` mode).  
V3. Share flow: extract `EXTRA_TEXT` + `EXTRA_STREAM` + `EXTRA_MIME_TYPES`/`type` → pass to `MainViewModel` → emit `OnDataShared` side effect → navigate to `AddEditScreen(fromShare=true)`.  
V4. `NotesViewModel` must apply `noteOrder.comparator()` to note list before reducing state.  
V5. Shared file URI must be copied to internal storage via `FileStorageHelper.saveSharedFileInternal()` before saving note; `Note.attachmentPath` stores absolute path, never raw content URI.  
V6. State changes only via `intent { reduce { } }`. Side effects only via `postSideEffect()`. Composables hold no business state.  
V7. `AddEditNoteViewModel` must pre-populate state from nav args at init: existing `note` seeds title/content/color/tags; `sharedText` seeds content; `sharedFileUri` triggers `FileStorageHelper` copy.  
V8. All Room/IO ops use `@IoDispatcher`. No DB calls on Main thread.  
V9. `NoteTextFieldState` must survive config change via `SavedStateHandle`.  
V10. `saveEnabled` = title or content non-empty (for manual create). Irrelevant when `fromShare=true` — auto-save always fires.  
V11. **Auto-save on share**: when `fromShare=true`, `AddEditNoteViewModel` saves note immediately after file copy at init, before user interaction. `noteId` populated from returned insert id.  
V12. **Tag changes auto-save**: when `fromShare=true`, toggling tag immediately calls `noteStore.setNoteTags()`. No explicit save button needed.  
V13. **Reminder changes auto-save**: adding/removing reminder calls `reminderStore.save()`/`delete()` + `notificationScheduler.schedule()`/`cancel()` immediately.  
V14. `AddEditScreen(fromShare=true)` expands tag panel by default (`isTagPanelExpanded=true`).  
V15. `ReminderWorker` cleans up past-due `ReminderEntity` from DB after posting notification.  
V16. `BootReceiver` on `BOOT_COMPLETED` calls `notificationScheduler.rescheduleAll()` with all active reminders from `reminderStore.getAllActive()`.  
V17. `ContentType` is derived at display time — never stored in Room. `NoteItemUI` derives it from `attachmentMimeType` + URL regex on `content`.  
V18. `Note.attachmentMimeType` reflects the actual MIME type of the stored file. Must be set from intent `type` or detected before saving.  
V19. WorkManager reminder workers tagged `"reminder_{reminderId}"` — cancel by tag to avoid duplicate schedules.  
V20. `POST_NOTIFICATIONS` permission requested at runtime before scheduling first reminder (Android 13+).  
V21. UI must never construct `Note` domain objects with system-set fields (`created`, `edited`, `id`). `AddEditNoteEvent.SaveNote` carries `NoteInput` (UI-owned fields only); `NoteStore.save(input, existingId?)` sets timestamps and id.

---

## §T Tasks

| id | status | task | cites |
|---|---|---|---|
| T1 | . | Remove SQLDelight residue from `build.gradle.kts` + `libs.versions.toml` | — |
| T2 | . | Migrate `Note.image: String?` → `attachmentPath: String?` + `attachmentMimeType: String?`; Room migration version bump | V5,V18 |
| T3 | . | Extend `FileStorageHelper` to handle any MIME type (image/video/audio/file), pick subdir by type | V5,V18 |
| T4 | . | Update `AddEditScreenDestinationArgs`: replace `image: Uri?` with `fileUri: Uri?` + `mimeType: String?` + `fromShare: Boolean` | V3,V7 |
| T5 | . | Update share extraction in `MainActivity`/`MainViewModel`: extract `EXTRA_STREAM`, `EXTRA_TEXT`, intent `type` | V3,I.intent |
| T6 | . | Implement auto-save in `AddEditNoteViewModel.init{}` when `fromShare=true`: copy file → insert note → store id | V11 |
| T7 | . | Apply `noteOrder.comparator()` in `NotesViewModel` before state reduce | V4 |
| T8 | . | Fix `AddEditNoteViewModel` init: seed title/content/color from existing `note` arg | V7 |
| T9 | . | Add `TagEntity`, `TagDao`, `NoteTagCrossRef`, Room migration | I.TagDao |
| T10 | . | Implement `TagStore` + `TagStoreImpl` | I.TagStore |
| T11 | . | Add `TagsViewModel` for drawer tag CRUD | I.ViewModels |
| T12 | . | Add tag picker panel in `AddEditScreen` (expandable, auto-expanded when `fromShare=true`) | V12,V14 |
| T13 | . | Wire tag toggle to immediate `noteStore.setNoteTags()` when `fromShare=true` | V12 |
| T14 | . | Add `ReminderEntity`, `ReminderDao`, Room migration | I.ReminderDao |
| T15 | . | Implement `ReminderStore` + `ReminderStoreImpl` | I.ReminderStore |
| T16 | . | Add `ReminderWorker` (WorkManager): post notification + delete past reminder from DB | V15 |
| T17 | . | Implement `NotificationScheduler` (WorkManager-backed): schedule/cancel/rescheduleAll | V13,V19 |
| T18 | . | Add `BootReceiver`: `BOOT_COMPLETED` → `rescheduleAll()` | V16 |
| T19 | . | Add reminder panel in `AddEditScreen`: expandable, options DAY/WEEK/MONTH/YEAR/CUSTOM | V13 |
| T20 | . | Add `RemindersViewModel` + reminders section in drawer | I.ViewModels |
| T21 | . | Add drawer to `NotesListScreen`: All Notes / Tags / Reminders sections | I.Navigation |
| T22 | . | Implement `ContentType` derivation helper; show icons in `NoteItemUI` | V17 |
| T23 | . | Add tag filter to `NotesViewModel` + `noteStore.observeNotesByTag()` | I.NoteStore |
| T24 | . | Request `POST_NOTIFICATIONS` permission before first reminder schedule | V20 |
| T25 | . | Verify `onNewIntent` wired to `MainViewModel` for repeat shares | V2,V3 |
| T26 | . | Verify `NoteItemUI` renders attachment (image via Coil, video thumbnail, audio icon, file icon) | V17 |
| T27 | . | Move inline Snackbar trigger to `NotesSideEffect.ShowSnackbar` if not already done | V6 |
| T28 | . | Add unit tests: `NoteStoreImpl`, `TagStoreImpl`, `ReminderStoreImpl`, sort comparators, `FileStorageHelper`, `ContentType` derivation | V8 |
| T29 | . | Remove or wire unused `ActionHandler` base class | — |
| T30 | . | Introduce `NoteInput(title, content, color, attachmentPath, attachmentMimeType, tagIds)` DTO; replace `Note` arg in `AddEditNoteEvent.SaveNote`; `NoteStore.save` sets `created`/`edited`/`id` | V21 |

---

## §B Bugs

| id | date | cause | fix |
|---|---|---|---|
