package com.cachescope.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cachescope.data.analytics.BenchmarkDao
import com.cachescope.data.analytics.BenchmarkResult
import com.cachescope.data.cache.CacheDao
import com.cachescope.data.cache.CacheEntity

@Database(
    entities = [CacheEntity::class, BenchmarkResult::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
    abstract fun benchmarkDao(): BenchmarkDao
}
