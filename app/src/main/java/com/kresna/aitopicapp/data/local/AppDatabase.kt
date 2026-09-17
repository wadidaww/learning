package com.kresna.aitopicapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kresna.aitopicapp.data.local.converter.ContentTypeConverter
import com.kresna.aitopicapp.data.local.converter.StringListConverter
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.data.local.dao.TopicDao
import com.kresna.aitopicapp.data.local.entity.ContentEntity
import com.kresna.aitopicapp.data.local.entity.TopicEntity

@Database(
    entities = [TopicEntity::class, ContentEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(ContentTypeConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun contentDao(): ContentDao
}
