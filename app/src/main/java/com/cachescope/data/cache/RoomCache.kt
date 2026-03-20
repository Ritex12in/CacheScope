package com.cachescope.data.cache

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "cache_table")
data class CacheEntity(
    @PrimaryKey val key: String,
    val json: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache_table WHERE key = :key")
    suspend fun get(key: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: CacheEntity)

    @Query("DELETE FROM cache_table")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cache_table")
    suspend fun count(): Int
}

@Singleton
class RoomCache @Inject constructor(
    private val dao: CacheDao
) : CacheDataSource<String> {

    override suspend fun get(key: String): String? = dao.get(key)?.json

    override suspend fun put(key: String, value: String) {
        dao.put(CacheEntity(key = key, json = value))
    }

    override suspend fun clear() = dao.clearAll()

    suspend fun count(): Int = dao.count()
}
