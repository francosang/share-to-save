package com.jfranco.sharetosave.persistence.implementation

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jfranco.sharetosave.persistence.implementation.converter.LocalDateTimeConverter
import com.jfranco.sharetosave.persistence.implementation.dao.NoteDao
import com.jfranco.sharetosave.persistence.implementation.dao.TagDao
import com.jfranco.sharetosave.persistence.entity.NoteEntity
import com.jfranco.sharetosave.persistence.entity.NoteTagCrossRef
import com.jfranco.sharetosave.persistence.entity.TagEntity

const val DB_VERSION_1 = 1
const val DB_VERSION_2 = 2
const val DB_VERSION_3 = 3

// Rename image → attachment_path, add attachment_mime_type.
// Full table rebuild required: SQLite < 3.25 (min SDK 26) has no RENAME COLUMN.
val MIGRATION_1_2 = object : Migration(DB_VERSION_1, DB_VERSION_2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE note_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT,
                content TEXT,
                attachment_path TEXT,
                attachment_mime_type TEXT,
                created TEXT NOT NULL,
                edited TEXT,
                color INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO note_new (id, title, content, attachment_path, attachment_mime_type, created, edited, color)
            SELECT id, title, content, image, NULL, created, edited, color FROM note
        """.trimIndent())
        db.execSQL("DROP TABLE note")
        db.execSQL("ALTER TABLE note_new RENAME TO note")
    }
}

val MIGRATION_2_3 = object : Migration(DB_VERSION_2, DB_VERSION_3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE tag (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                color INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE notes_tags (
                note_id INTEGER NOT NULL,
                tag_id INTEGER NOT NULL,
                PRIMARY KEY(note_id, tag_id),
                FOREIGN KEY(note_id) REFERENCES note(id) ON DELETE CASCADE,
                FOREIGN KEY(tag_id) REFERENCES tag(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX index_notes_tags_tag_id ON notes_tags(tag_id)")
    }
}

@Database(
    entities = [
        NoteEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
    ],
    version = DB_VERSION_3
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
    abstract fun getTagDao(): TagDao
}
