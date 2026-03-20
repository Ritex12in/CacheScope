package com.cachescope.ui.compare

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CompareScreen(viewModel: CompareViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Strategy Race",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "All strategies fire simultaneously. See which responds fastest.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Query input
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = { Text("Search query") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isRacing
        )

        // Race / Reset buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = if (state.raceComplete) viewModel::reset else viewModel::startRace,
                enabled = !state.isRacing && state.query.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                if (state.isRacing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Racing…")
                } else if (state.raceComplete) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Race again")
                } else {
                    Text("Start race")
                }
            }
        }

        // Winner banner
        if (state.raceComplete) {
            val winner = state.entries.minByOrNull { it.outcome?.result?.latencyMs ?: Long.MAX_VALUE }
            winner?.outcome?.let { outcome ->
                val color = Color(winner.type.color)
                Card(
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🏆", fontSize = 28.sp)
                        Column {
                            Text(
                                "${winner.type.label} wins!",
                                fontWeight = FontWeight.Bold,
                                color = color,
                                fontSize = 16.sp
                            )
                            Text(
                                "${outcome.result.latencyMs}ms · ${if (outcome.result.hit) "cache hit" else "network"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Strategy race cards
        state.entries.forEach { entry ->
            RaceEntryCard(entry = entry)
        }
    }
}

@Composable
private fun RaceEntryCard(entry: RaceEntry) {
    val color = Color(entry.type.color)
    val outcome = entry.outcome
    val latency = outcome?.result?.latencyMs

    // Pulse animation while loading
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${entry.type.name}")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_${entry.type.name}"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.rank == 1)
                color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank badge or loading indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (entry.isLoading) color.copy(alpha = pulseAlpha)
                        else color.copy(alpha = if (outcome != null) 0.2f else 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    entry.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = color
                    )
                    entry.rank != null -> Text(
                        "#${entry.rank}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = color
                    )
                    else -> Text(
                        "—",
                        fontSize = 13.sp,
                        color = color.copy(alpha = 0.5f)
                    )
                }
            }

            // Strategy name + hit/miss
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.type.label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (outcome != null) {
                    val hitColor = if (outcome.result.hit) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Text(
                        if (outcome.result.hit) "cache hit" else "cache miss → network",
                        fontSize = 12.sp,
                        color = hitColor
                    )
                } else if (entry.isLoading) {
                    Text(
                        "fetching…",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "waiting",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Latency
            if (latency != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$latency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = color
                    )
                    Text(
                        "ms",
                        fontSize = 11.sp,
                        color = color.copy(alpha = 0.7f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // Relative speed bar (shown once race completes)
        if (outcome != null && latency != null) {
            val maxLatency = 2000L
            val fraction = (latency.toFloat() / maxLatency).coerceIn(0.02f, 1f)
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = color,
                trackColor = color.copy(alpha = 0.1f)
            )
        }
    }
}
