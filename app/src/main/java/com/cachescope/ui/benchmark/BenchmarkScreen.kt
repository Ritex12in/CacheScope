package com.cachescope.ui.benchmark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cachescope.data.cache.CacheType
import com.cachescope.domain.BenchmarkOutcome

@Composable
fun BenchmarkScreen(viewModel: BenchmarkViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Cache Benchmark",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Query input
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search query") },
            placeholder = { Text("e.g. android") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        // Strategy selector
        Text("Cache strategy", style = MaterialTheme.typography.labelLarge)
        StrategySelector(
            selected = state.selectedStrategy,
            onSelect = viewModel::onStrategySelect
        )

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = viewModel::runBenchmark,
                enabled = !state.isLoading && state.query.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Run")
                }
            }
            OutlinedButton(
                onClick = viewModel::clearAllCaches,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear caches")
            }
        }

        // Last result card
        AnimatedVisibility(
            visible = state.lastOutcome != null,
            enter = fadeIn(), exit = fadeOut()
        ) {
            state.lastOutcome?.let { ResultCard(it) }
        }

        // Error
        state.error?.let { err ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    err,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // History
        if (state.history.isNotEmpty()) {
            Text("Recent fetches", style = MaterialTheme.typography.labelLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.history) { outcome ->
                    HistoryRow(outcome)
                }
            }
        }
    }
}

@Composable
private fun StrategySelector(selected: CacheType, onSelect: (CacheType) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        CacheType.entries.forEach { type ->
            val isSelected = type == selected
            val bgColor = Color(type.color)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) bgColor else bgColor.copy(alpha = 0.12f))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = bgColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    type.label,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else bgColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ResultCard(outcome: BenchmarkOutcome) {
    val result = outcome.result
    val strategyType = CacheType.entries.first { it.name == result.strategy }
    val color = Color(strategyType.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Latency badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${result.latencyMs}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = color
                    )
                    Text("ms", fontSize = 11.sp, color = color)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    strategyType.label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hitColor = if (result.hit) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(hitColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (result.hit) "CACHE HIT" else "CACHE MISS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = hitColor
                        )
                    }
                }
                Text(
                    "Query: \"${result.query}\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(outcome: BenchmarkOutcome) {
    val result = outcome.result
    val strategyType = CacheType.entries.first { it.name == result.strategy }
    val color = Color(strategyType.color)
    val hitColor = if (result.hit) Color(0xFF4CAF50) else Color(0xFFF44336)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Text(strategyType.label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                "\"${result.query}\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (result.hit) "HIT" else "MISS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = hitColor
            )
            Text(
                "${result.latencyMs}ms",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
