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
    version = 4,
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
    }
}
