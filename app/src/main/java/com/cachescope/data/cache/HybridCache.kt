package com.cachescope.data.cache

class HybridCache<T>(
    private val memory: CacheDataSource<T>,
    private val persistent: CacheDataSource<T>
) : CacheDataSource<T> {

    override suspend fun get(key: String): T? {
        // L1: memory hit
        memory.get(key)?.let { return it }

        // L2: persistent hit — warm up memory
        persistent.get(key)?.let {
            memory.put(key, it)
            return it
        }

        return null
    }

    override suspend fun put(key: String, value: T) {
        memory.put(key, value)
        persistent.put(key, value)
    }

    override suspend fun clear() {
        memory.clear()
        persistent.clear()
    }
}
