package com.cachescope.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cachescope.data.analytics.StrategyStats
import com.cachescope.data.cache.CacheType

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = viewModel::clearAll) {
                    Text("Clear data", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Avg latency bar chart
        item {
            Text("Avg latency per strategy", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LatencyBarChart(state.summaries)
        }

        // Stats cards
        item {
            Text("Hit rate breakdown", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.summaries.filter { it.totalFetches > 0 }.forEach { stats ->
                    StatCard(stats)
                }
                if (state.summaries.all { it.totalFetches == 0 }) {
                    Text(
                        "No data yet. Run some benchmarks first!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LatencyBarChart(summaries: List<StrategyStats>) {
    val maxLatency = summaries.maxOfOrNull { it.avgLatencyMs }?.takeIf { it > 0 } ?: 1.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            summaries.forEach { stats ->
                val fraction = (stats.avgLatencyMs / maxLatency).toFloat().coerceIn(0f, 1f)
                val color = Color(stats.type.color)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        stats.type.label,
                        modifier = Modifier.width(72.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction.coerceAtLeast(if (stats.totalFetches > 0) 0.03f else 0f))
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (stats.totalFetches > 0) "${stats.avgLatencyMs.toLong()}ms" else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.width(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(stats: StrategyStats) {
    val color = Color(stats.type.color)
    val hitPct = (stats.hitRate * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stats.type.label, fontWeight = FontWeight.SemiBold, color = color)
                Text(
                    "${stats.totalFetches} fetches · ${stats.cacheHits} hits",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Hit rate circle indicator
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { stats.hitRate },
                    modifier = Modifier.size(48.dp),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f),
                    strokeWidth = 4.dp
                )
                Text(
                    "$hitPct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
