package com.cachescope.data.cache

import javax.inject.Inject
import javax.inject.Singleton

enum class CacheType(val label: String, val color: Long) {
    MEMORY("Memory",  0xFF4CAF50),  // green
    DISK("Disk",      0xFF2196F3),  // blue
    ROOM("Room DB",   0xFF9C27B0),  // purple
    HYBRID("Hybrid",  0xFFFF9800),  // amber
    NETWORK("Network",0xFFF44336)   // red
}

@Singleton
class CacheFactory @Inject constructor(
    private val memory: MemoryCache<String>,
    private val disk: DiskCache,
    private val room: RoomCache
) {
    fun get(type: CacheType): CacheDataSource<String> = when (type) {
        CacheType.MEMORY  -> memory
        CacheType.DISK    -> disk
        CacheType.ROOM    -> room
        CacheType.HYBRID  -> HybridCache(memory, room)
        CacheType.NETWORK -> throw IllegalStateException("Use UserRepository for network calls")
    }
}
