package com.cachescope.data.analytics

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.cachescope.data.cache.CacheType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "benchmark_results")
data class BenchmarkResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val strategy: String,          // CacheType.name
    val latencyMs: Long,
    val hit: Boolean,              // true = cache hit, false = miss → went to network
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface BenchmarkDao {
    @Insert
    suspend fun insert(result: BenchmarkResult)

    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<BenchmarkResult>>

    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<BenchmarkResult>

    @Query("SELECT AVG(latencyMs) FROM benchmark_results WHERE strategy = :strategy AND hit = 1")
    suspend fun avgLatency(strategy: String): Double?

    @Query("SELECT COUNT(*) FROM benchmark_results WHERE strategy = :strategy AND hit = 1")
    suspend fun hitCount(strategy: String): Int

    @Query("SELECT COUNT(*) FROM benchmark_results WHERE strategy = :strategy")
    suspend fun totalCount(strategy: String): Int

    @Query("DELETE FROM benchmark_results")
    suspend fun clearAll()
}

@Singleton
class AnalyticsEngine @Inject constructor(
    private val dao: BenchmarkDao
) {
    suspend fun log(result: BenchmarkResult) = dao.insert(result)

    fun observeAll(): Flow<List<BenchmarkResult>> = dao.observeAll()

    suspend fun summaryFor(type: CacheType): StrategyStats {
        val key = type.name
        val total = dao.totalCount(key)
        val hits  = dao.hitCount(key)
        val avg   = dao.avgLatency(key) ?: 0.0
        return StrategyStats(
            type = type,
            totalFetches = total,
            cacheHits = hits,
            avgLatencyMs = avg,
            hitRate = if (total > 0) hits.toFloat() / total else 0f
        )
    }

    suspend fun allSummaries(): List<StrategyStats> =
        CacheType.entries.map { summaryFor(it) }

    suspend fun clearAll() = dao.clearAll()
}

data class StrategyStats(
    val type: CacheType,
    val totalFetches: Int,
    val cacheHits: Int,
    val avgLatencyMs: Double,
    val hitRate: Float
)
