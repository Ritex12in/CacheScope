package com.cachescope.domain

import com.cachescope.data.analytics.AnalyticsEngine
import com.cachescope.data.analytics.BenchmarkResult
import com.cachescope.data.cache.CacheFactory
import com.cachescope.data.cache.CacheType
import com.cachescope.data.repository.UserRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class BenchmarkOutcome(
    val result: BenchmarkResult,
    val data: String?       // serialized payload, null on miss
)

@Singleton
class BenchmarkRunner @Inject constructor(
    private val cacheFactory: CacheFactory,
    private val userRepository: UserRepository,
    private val analytics: AnalyticsEngine,
    private val json: Json
) {

    suspend fun run(query: String, strategy: CacheType): BenchmarkOutcome {
        if (strategy == CacheType.NETWORK) {
            return runNetwork(query)
        }

        val cache = cacheFactory.get(strategy)

        val start = System.currentTimeMillis()
        val cached = cache.get(query)
        val latency = System.currentTimeMillis() - start

        if (cached != null) {
            val result = BenchmarkResult(
                strategy = strategy.name,
                latencyMs = latency,
                hit = true,
                query = query
            )
            analytics.log(result)
            return BenchmarkOutcome(result, cached)
        }

        val networkOutcome = runNetwork(query)
        networkOutcome.data?.let { cache.put(query, it) }

        val totalLatency = System.currentTimeMillis() - start
        val missResult = BenchmarkResult(
            strategy = strategy.name,
            latencyMs = totalLatency,
            hit = false,
            query = query
        )
        analytics.log(missResult)
        return BenchmarkOutcome(missResult, networkOutcome.data)
    }

    private suspend fun runNetwork(query: String): BenchmarkOutcome {
        val start = System.currentTimeMillis()
        val response = userRepository.searchUsers(query)
        val latency = System.currentTimeMillis() - start

        val payload = response.getOrNull()?.let { json.encodeToString(it) }
        val result = BenchmarkResult(
            strategy = CacheType.NETWORK.name,
            latencyMs = latency,
            hit = false,
            query = query
        )
        analytics.log(result)
        return BenchmarkOutcome(result, payload)
    }

    suspend fun raceAll(query: String): List<BenchmarkOutcome> {
        return CacheType.entries.map { run(query, it) }
            .sortedBy { it.result.latencyMs }
    }
}
