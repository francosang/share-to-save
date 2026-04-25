# SPEC

## §G GOAL

Android note-taking app — intercept OS share intents (text, image, audio, video, any file) → auto-save note → tag picker + reminder panel inline; manual CRUD + tag/filter from drawer; WorkManager reminders survive reboots.

---

## §C CONSTRAINTS

- Min SDK 26, Target/Compile SDK 35, Java 17, Kotlin 2.2.10
- Jetpack Compose BOM 2024.06.00, Material3 1.3.2
- Orbit MVI 10.0.0 (orbit-core, orbit-viewmodel, orbit-compose)
- Hilt 2.57.1 + hilt-navigation-compose 1.2.0
- Room 2.7.2 (room-ktx, room-runtime, room-compiler via KSP)
- Compose Destinations 2.2.0 (KSP-generated nav graph)
- Coil 2.7.0 (async image/file thumbnail loading)
- WorkManager 2.9.x — reminder scheduling, survives reboots
- KSP 2.2.10-2.0.2
- ⊥ network. ⊥ multiplatform.
- Shared files → `filesDir/shared_files/{type}/`, absolute path stored in Room.
- Permissions: `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS` (Android 13+), `USE_EXACT_ALARM` (Android 12+).

---

## §I INTERFACES

### Intent filters (AndroidManifest.xml)

|Action|Data type|Purpose|
|---|---|---|
|`ACTION_MAIN` + `LAUNCHER`|—|app launch|
|`ACTION_CREATE_NOTE`|—|custom note creation|
|`ACTION_SEND`|`text/*`, `image/*`, `audio/*`, `video/*`, `*/*`|single-item share|
|`ACTION_SEND_MULTIPLE`|`*/*`|multi-item share|

MainActivity: exported, `singleTask`.
BootReceiver: `RECEIVE_BOOT_COMPLETED` → reschedule ∀ active reminders.

### Navigation (Compose Destinations)

|Destination|Start|Args|
|---|---|---|
|`NotesListScreen`|yes|—|
|`AddEditScreen`|no|`AddEditScreenDestinationArgs(note: Note?, text: String?, fileUri: Uri?, mimeType: String?, fromShare: Boolean)`|

Drawer (modal, ⊥ destination) ∈ `NotesListScreen`:
- All Notes
- Tags (list, tap → filter notes)
- Reminders (∀ active reminders)

### NoteStore

```kotlin
suspend fun getNotes(): List<Note>
fun observeNotes(): Flow<List<Note>>
suspend fun save(note: Note): Note
suspend fun getNote(id: Long): Note?
suspend fun deleteNote(id: Long)
suspend fun setNoteTags(noteId: Long, tagIds: List<Long>)
fun observeNotesByTag(tagId: Long): Flow<List<Note>>
```

### TagStore

```kotlin
fun observeTags(): Flow<List<Tag>>
suspend fun save(tag: Tag): Tag
suspend fun delete(id: Long)
suspend fun getTags(): List<Tag>
suspend fun getTagsForNote(noteId: Long): List<Tag>
```

### ReminderStore

```kotlin
fun observeReminders(): Flow<List<Reminder>>
suspend fun save(reminder: Reminder): Reminder
suspend fun delete(id: Long)
suspend fun getRemindersForNote(noteId: Long): List<Reminder>
suspend fun getAllActive(): List<Reminder>
```

### NotificationScheduler

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

### NoteTagCrossRef (Room junction)

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

MainViewModel — share intents
- Events: `OnDataShared(text: String?, fileUri: Uri?, mimeType: String?)`
- SideEffects: `OnDataShared(text: String?, fileUri: Uri?, mimeType: String?)`

NotesViewModel — list + drawer
- State: `NotesState(notes, noteOrder, isOrderSectionVisible, recentDeletedNote, tags, activeTagFilter: Long?, isDrawerOpen, reminders)`
- Events: Order, DeleteNote, RestoreNote, ToggleOrderSection, AddEditNoteScreen, ToggleDrawer, SelectTag(id?), DeleteReminder
- SideEffects: NavigateToAddEditNoteScreen, ShowSnackbar

AddEditNoteViewModel — create/edit/share
- State: `AddEditNoteState(title, content, attachmentPath, attachmentMimeType, color, noteId, isNoteSaved, saveEnabled, isFromShare, tags, selectedTagIds, reminders, isTagPanelExpanded, isReminderPanelExpanded)`
- Events: ChangeTitleFocus, ChangeContentFocus, ChangeColor, SaveNote, ToggleTag(tagId), AddReminder(type, customTime?), RemoveReminder(id), ToggleTagPanel, ToggleReminderPanel
- SideEffects: `NavigateBackWithResult(note: Note)`

TagsViewModel — drawer tag CRUD
- State: `TagsState(tags: List<Tag>)`
- Events: AddTag(name, color), DeleteTag(id), EditTag(tag)

RemindersViewModel — drawer reminders
- State: `RemindersState(reminders: List<ReminderWithNote>)`
- Events: DeleteReminder(id)

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

- `DatabaseModule` — `AppDatabase` + `NoteDao`, `TagDao`, `ReminderDao` (Singleton)
- `StoreModuleBinds` — `NoteStoreImpl`→`NoteStore`, `TagStoreImpl`→`TagStore`, `ReminderStoreImpl`→`ReminderStore` (Singleton)
- `CoroutinesModule` — `@DefaultDispatcher`, `@IoDispatcher`, `@MainDispatcher`, `@MainImmediateDispatcher`
- `NotificationModule` — `NotificationScheduler` impl (WorkManager-backed, Singleton)

---

## §V INVARIANTS

V1: `Note.id == null` → unsaved. `noteDao.insert()` returns id > 0.
V2: `MainActivity` ! handle `onNewIntent()` ∀ repeat `ACTION_SEND` (singleTask mode).
V3: Share flow: extract `EXTRA_TEXT` + `EXTRA_STREAM` + `EXTRA_MIME_TYPES`/`type` → `MainViewModel` → emit `OnDataShared` → navigate `AddEditScreen(fromShare=true)`.
V4: `NotesViewModel` ! apply `noteOrder.comparator()` before state reduce.
V5: Shared file URI ! copy via `FileStorageHelper.saveSharedFileInternal()` before saving note. `Note.attachmentPath` = absolute path. ⊥ raw content URI.
V6: State changes only via `intent { reduce { } }`. Side effects only via `postSideEffect()`. ⊥ business state in Composables.
V7: `AddEditNoteViewModel` ! pre-populate state from nav args at init: existing `note` → title/content/color/tags; `sharedText` → content; `sharedFileUri` → `FileStorageHelper` copy.
V8: ∀ Room/IO ops use `@IoDispatcher`. ⊥ DB calls on Main thread.
V9: `NoteTextFieldState` ! survive config change via `SavedStateHandle`.
V10: `saveEnabled` = title | content non-empty (manual create). Irrelevant when `fromShare=true` — auto-save ∀.
V11: Auto-save on share: `fromShare=true` → `AddEditNoteViewModel` saves note immediately after file copy at init, before user interaction. `noteId` populated from insert id.
V12: Tag changes auto-save: `fromShare=true` → toggle tag → `noteStore.setNoteTags()` immediately. ⊥ explicit save button.
V13: Reminder changes auto-save: add/remove → `reminderStore.save()`/`delete()` + `notificationScheduler.schedule()`/`cancel()` immediately.
V14: `AddEditScreen(fromShare=true)` → `isTagPanelExpanded=true`.
V15: `ReminderWorker` ! clean up past-due `ReminderEntity` from DB after posting notification.
V16: `BootReceiver` on `BOOT_COMPLETED` → `notificationScheduler.rescheduleAll()` with ∀ active reminders from `reminderStore.getAllActive()`.
V17: `ContentType` derived at display time. ⊥ stored in Room. `NoteItemUI` derives from `attachmentMimeType` + URL regex on `content`.
V18: `Note.attachmentMimeType` ! reflect actual MIME type of stored file. ! set from intent `type` or detected before saving.
V19: WorkManager reminder workers tagged `"reminder_{reminderId}"` — cancel by tag. ⊥ duplicate schedules.
V20: `POST_NOTIFICATIONS` ! request at runtime before first reminder schedule (Android 13+).
V21: UI ⊥ construct `Note` with system-set fields (`created`, `edited`, `id`). `AddEditNoteEvent.SaveNote` carries `NoteInput` (UI-owned fields only). `NoteStore.save(input, existingId?)` sets timestamps + id.
V22: `fromShare` auto-save Note ! read all UI-owned fields (`color`, `title`, `content`) from current `state` at save time. ⊥ hardcoded defaults (e.g. `color=0`).
V23: `collectSideEffect` lambda ! ⊥ call suspending `showSnackbar()` directly. Must launch separate coroutine (`rememberCoroutineScope().launch{}`) for snackbar so collector stays non-blocking; ⊥ nav/other side effects queue behind visible snackbar.

---

## §T TASKS

id|status|task|cites
T1|x|remove SQLDelight residue from `build.gradle.kts` + `libs.versions.toml`|-
T2|x|migrate `Note.image: String?` → `attachmentPath: String?` + `attachmentMimeType: String?`; Room migration version bump|V5,V18
T3|x|extend `FileStorageHelper` → any MIME type, pick subdir by type|V5,V18
T4|x|update `AddEditScreenDestinationArgs`: replace `image: Uri?` → `fileUri: Uri?` + `mimeType: String?` + `fromShare: Boolean`|V3,V7
T5|x|update share extraction in `MainActivity`/`MainViewModel`: extract `EXTRA_STREAM`, `EXTRA_TEXT`, intent `type`|V3,§I.intent
T6|x|impl auto-save in `AddEditNoteViewModel.init{}` when `fromShare=true`: copy file → insert note → store id|V11
T7|x|apply `noteOrder.comparator()` in `NotesViewModel` before state reduce|V4
T8|x|fix `AddEditNoteViewModel` init: seed title/content/color from existing `note` arg|V7
T9|x|add `TagEntity`, `TagDao`, `NoteTagCrossRef`, Room migration|§I.TagDao
T10|x|impl `TagStore` + `TagStoreImpl`|§I.TagStore
T11|x|add `TagsViewModel` for drawer tag CRUD|§I.ViewModels
T12|x|add tag picker panel in `AddEditScreen` (expandable, auto-expanded when `fromShare=true`)|V12,V14
T13|x|wire tag toggle → immediate `noteStore.setNoteTags()` when `fromShare=true`|V12
T14|x|add `ReminderEntity`, `ReminderDao`, Room migration|§I.ReminderDao
T15|x|impl `ReminderStore` + `ReminderStoreImpl`|§I.ReminderStore
T16|.|add `ReminderWorker` (WorkManager): post notification + delete past reminder from DB|V15
T17|.|impl `NotificationScheduler` (WorkManager-backed): schedule/cancel/rescheduleAll|V13,V19
T18|.|add `BootReceiver`: `BOOT_COMPLETED` → `rescheduleAll()`|V16
T19|.|add reminder panel in `AddEditScreen`: expandable, options DAY/WEEK/MONTH/YEAR/CUSTOM|V13
T20|x|add `RemindersViewModel` + reminders section in drawer|§I.ViewModels
T21|.|add drawer to `NotesListScreen`: All Notes / Tags / Reminders sections|§I.Navigation
T22|.|impl `ContentType` derivation helper; show icons in `NoteItemUI`|V17
T23|.|add tag filter to `NotesViewModel` + `noteStore.observeNotesByTag()`|§I.NoteStore
T24|.|request `POST_NOTIFICATIONS` before first reminder schedule|V20
T25|x|verify `onNewIntent` wired to `MainViewModel` for repeat shares|V2,V3
T26|.|verify `NoteItemUI` renders attachment (image via Coil, video thumbnail, audio icon, file icon)|V17
T27|.|move inline Snackbar trigger → `NotesSideEffect.ShowSnackbar` if not done|V6
T28|.|add unit tests: `NoteStoreImpl`, `TagStoreImpl`, `ReminderStoreImpl`, sort comparators, `FileStorageHelper`, `ContentType` derivation|V8
T29|.|remove or wire unused `ActionHandler` base class|-
T30|.|intro `NoteInput(title, content, color, attachmentPath, attachmentMimeType, tagIds)` DTO; replace `Note` arg in `AddEditNoteEvent.SaveNote`; `NoteStore.save(input, existingId?)` sets `created`/`edited`/`id`|V21
T31|.|fix color bug: replace hardcoded `color=0` in `fromShare` auto-save with `state.color`|V22,B1
T32|.|extend domain: introduce `NoteAttachment(path: String, mimeType: String?)` + `Note.attachments: List<NoteAttachment>`; deprecate `attachmentPath`/`attachmentMimeType` single fields|V5,V18
T33|.|Room: add `NoteAttachmentEntity`, `NoteAttachmentDao`; migration; update `NoteStoreImpl` to persist + load attachment list|§I.NoteDao
T34|.|update `AddEditScreenDestinationArgs`: `fileUri: Uri?`+`mimeType: String?` → `fileUris: List<Uri>`, `mimeTypes: List<String>`|V3,V7
T35|.|update share extraction (`MainActivity`/`MainViewModel`): handle both `ACTION_SEND` single + `ACTION_SEND_MULTIPLE` → list of URIs|V3,§I.intent
T36|.|update `FileStorageHelper`: add `saveSharedFilesInternal(uris: List<Uri>, mimeTypes: List<String?>)` → `List<String>`|V5
T37|.|update `AddEditNoteViewModel` init: copy + save list of files; `Note.attachments` populated from result|V11,V22
T38|.|update `AddEditScreen` UI: attachment gallery (horizontal scroll, thumbnails); add/remove individual attachments|V17
T39|.|UX audit + polish: empty state in `NotesListScreen`, skeleton/loading indicators, enter/exit transitions, color picker a11y labels; tag picker empty state: show "+ New tag" affordance (chip or link to drawer) when `tags.isEmpty()`|V6
T40|x|fix `NotesScreen.collectSideEffect`: move `showSnackbar()` into `rememberCoroutineScope().launch{}` so nav side effects dispatch independently while snackbar visible|V23,B2

---

## §B BUGS

id|date|cause|fix
B1|2026-04-25|`fromShare` auto-save constructs `Note(color=0)` (hardcoded, line 110 AddEditNoteViewModel) → ignores state.color → user color pick lost|V22
B2|2026-04-25|`NotesScreen.collectSideEffect` suspends directly on `showSnackbar()` (line 74) → collector blocked while snackbar visible: (1) snackbar appears stuck, (2) FAB click queued/dropped, (3) buffered `NavigateToAddEditNoteScreen` drains after snackbar dismiss → undo opens AddEdit|V23
