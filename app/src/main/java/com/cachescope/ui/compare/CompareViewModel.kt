package com.cachescope.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cachescope.data.cache.CacheType
import com.cachescope.domain.BenchmarkOutcome
import com.cachescope.domain.BenchmarkRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RaceEntry(
    val type: CacheType,
    val outcome: BenchmarkOutcome?,
    val isLoading: Boolean = false,
    val rank: Int? = null         // 1 = fastest
)

data class CompareUiState(
    val query: String = "kotlin",
    val isRacing: Boolean = false,
    val entries: List<RaceEntry> = CacheType.entries.map { RaceEntry(it, null) },
    val raceComplete: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val runner: BenchmarkRunner
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun startRace() {
        val query = _uiState.value.query.trim()
        if (query.isBlank() || _uiState.value.isRacing) return

        // Reset all entries to loading state
        _uiState.value = _uiState.value.copy(
            isRacing = true,
            raceComplete = false,
            error = null,
            entries = CacheType.entries.map { RaceEntry(it, null, isLoading = true) }
        )

        viewModelScope.launch {
            // Launch all strategies concurrently using async
            val deferreds = CacheType.entries.map { type ->
                type to async {
                    runCatching { runner.run(query, type) }
                }
            }

            // Collect results as each completes and update UI progressively
            val completedOutcomes = mutableListOf<Pair<CacheType, BenchmarkOutcome>>()

            deferreds.forEach { (type, deferred) ->
                val result = deferred.await()
                result.onSuccess { outcome ->
                    completedOutcomes.add(type to outcome)
                    // Update this entry immediately so UI feels live
                    updateEntry(type, outcome)
                }.onFailure { e ->
                    updateEntryError(type, e.message)
                }
            }

            // Assign ranks by latency
            val ranked = completedOutcomes.sortedBy { it.second.result.latencyMs }
            val rankMap = ranked.mapIndexed { idx, (type, _) -> type to idx + 1 }.toMap()

            _uiState.value = _uiState.value.copy(
                isRacing = false,
                raceComplete = true,
                entries = _uiState.value.entries.map { entry ->
                    entry.copy(rank = rankMap[entry.type], isLoading = false)
                }
            )
        }
    }

    fun reset() {
        _uiState.value = CompareUiState(query = _uiState.value.query)
    }

    private fun updateEntry(type: CacheType, outcome: BenchmarkOutcome) {
        _uiState.value = _uiState.value.copy(
            entries = _uiState.value.entries.map { entry ->
                if (entry.type == type)
                    entry.copy(outcome = outcome, isLoading = false)
                else entry
            }
        )
    }

    private fun updateEntryError(type: CacheType, message: String?) {
        _uiState.value = _uiState.value.copy(
            entries = _uiState.value.entries.map { entry ->
                if (entry.type == type)
                    entry.copy(isLoading = false)
                else entry
            }
        )
    }
}
