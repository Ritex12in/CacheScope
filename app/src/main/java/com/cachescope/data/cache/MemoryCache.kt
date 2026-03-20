package com.cachescope.data.cache

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache<T> @Inject constructor() : CacheDataSource<T> {

    private val cache = mutableMapOf<String, T>()

    override suspend fun get(key: String): T? = cache[key]

    override suspend fun put(key: String, value: T) {
        cache[key] = value
    }

    override suspend fun clear() = cache.clear()

    val size: Int get() = cache.size
}
