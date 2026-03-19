package com.peekr.core.di

import android.content.Context
import androidx.room.Room
import com.peekr.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // تشفير الداتابيز
        val passphrase = SQLiteDatabase.getBytes("peekr_secure_key".toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "peekr_database"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun providePostDao(db: AppDatabase) = db.postDao()
    @Provides fun provideArchiveDao(db: AppDatabase) = db.archiveDao()
    @Provides fun provideCategoryDao(db: AppDatabase) = db.categoryDao()
    @Provides fun provideAccountDao(db: AppDatabase) = db.accountDao()
    @Provides fun provideApiKeyDao(db: AppDatabase) = db.apiKeyDao()
    @Provides fun provideToolDao(db: AppDatabase) = db.toolDao()
    @Provides fun provideLogDao(db: AppDatabase) = db.logDao()
}
