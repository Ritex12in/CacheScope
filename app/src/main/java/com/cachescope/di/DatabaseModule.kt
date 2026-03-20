package com.cachescope.di

import android.content.Context
import androidx.room.Room
import com.cachescope.data.analytics.BenchmarkDao
import com.cachescope.data.cache.CacheDao
import com.cachescope.data.db.AppDatabase
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
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cachescope_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()

    @Provides
    fun provideBenchmarkDao(db: AppDatabase): BenchmarkDao = db.benchmarkDao()
}
