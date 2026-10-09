package com.kresna.aitopicapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kresna.aitopicapp.data.local.converter.ContentTypeConverter
import com.kresna.aitopicapp.data.local.converter.StringListConverter
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.data.local.dao.TopicDao
import com.kresna.aitopicapp.data.local.entity.ContentEntity
import com.kresna.aitopicapp.data.local.entity.TopicEntity

@Database(
    entities = [TopicEntity::class, ContentEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(ContentTypeConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun contentDao(): ContentDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE generated_content ADD COLUMN codeSnippet TEXT")
                db.execSQL("ALTER TABLE generated_content ADD COLUMN codeLanguage TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE topics ADD COLUMN defaultContentType TEXT")
            }
        }

        /** Adds MCQ answers, bookmarks, topic difficulty and a topicId index; removes orphaned content. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE generated_content ADD COLUMN userAnswerIndex INTEGER")
                db.execSQL("ALTER TABLE generated_content ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN difficulty TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_generated_content_topicId` ON `generated_content` (`topicId`)")
                db.execSQL("DELETE FROM generated_content WHERE topicId NOT IN (SELECT id FROM topics)")
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }
}
