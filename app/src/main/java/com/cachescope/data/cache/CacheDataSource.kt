package com.cachescope.data.cache

interface CacheDataSource<T> {
    suspend fun get(key: String): T?
    suspend fun put(key: String, value: T)
    suspend fun clear()
}
