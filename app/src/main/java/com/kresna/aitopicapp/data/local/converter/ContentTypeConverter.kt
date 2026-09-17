package com.kresna.aitopicapp.data.local.converter

import androidx.room.TypeConverter
import com.kresna.aitopicapp.domain.model.ContentType

class ContentTypeConverter {

    @TypeConverter
    fun fromContentType(contentType: ContentType): String {
        return contentType.name
    }

    @TypeConverter
    fun toContentType(value: String): ContentType {
        return try {
            ContentType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ContentType.MCQ
        }
    }
}
