package com.kresna.aitopicapp.di

import android.content.Context
import androidx.room.Room
import com.kresna.aitopicapp.data.local.AppDatabase
import com.kresna.aitopicapp.data.local.dao.ContentDao
import com.kresna.aitopicapp.data.local.dao.TopicDao
import com.kresna.aitopicapp.data.repository.ContentRepositoryImpl
import com.kresna.aitopicapp.data.repository.TopicRepositoryImpl
import com.kresna.aitopicapp.domain.repository.ContentRepository
import com.kresna.aitopicapp.domain.repository.TopicRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aitopicapp.db"
        )
            .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTopicDao(database: AppDatabase): TopicDao {
        return database.topicDao()
    }

    @Provides
    fun provideContentDao(database: AppDatabase): ContentDao {
        return database.contentDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTopicRepository(impl: TopicRepositoryImpl): TopicRepository

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
