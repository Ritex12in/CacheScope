package com.cachescope.di

import android.content.Context
import com.cachescope.data.cache.CacheFactory
import com.cachescope.data.cache.DiskCache
import com.cachescope.data.cache.MemoryCache
import com.cachescope.data.cache.RoomCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun provideMemoryCache(): MemoryCache<String> = MemoryCache()

    @Provides
    @Singleton
    fun provideDiskCache(@ApplicationContext context: Context): DiskCache =
        DiskCache(File(context.cacheDir, "disk_cache"))

    @Provides
    @Singleton
    fun provideCacheFactory(
        memory: MemoryCache<String>,
        disk: DiskCache,
        room: RoomCache
    ): CacheFactory = CacheFactory(memory, disk, room)
}
