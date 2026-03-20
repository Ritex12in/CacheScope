package com.cachescope.ui.benchmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cachescope.data.cache.CacheFactory
import com.cachescope.data.cache.CacheType
import com.cachescope.domain.BenchmarkOutcome
import com.cachescope.domain.BenchmarkRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BenchmarkUiState(
    val query: String = "android",
    val selectedStrategy: CacheType = CacheType.MEMORY,
    val isLoading: Boolean = false,
    val lastOutcome: BenchmarkOutcome? = null,
    val history: List<BenchmarkOutcome> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BenchmarkViewModel @Inject constructor(
    private val runner: BenchmarkRunner,
    private val cacheFactory: CacheFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun onStrategySelect(type: CacheType) {
        _uiState.value = _uiState.value.copy(selectedStrategy = type)
    }

    fun runBenchmark() {
        val state = _uiState.value
        if (state.query.isBlank() || state.isLoading) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val outcome = runner.run(state.query.trim(), state.selectedStrategy)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastOutcome = outcome,
                    history = listOf(outcome) + _uiState.value.history.take(19)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun clearAllCaches() {
        viewModelScope.launch {
            CacheType.entries
                .filter { it != CacheType.NETWORK }
                .forEach { type ->
                    runCatching { cacheFactory.get(type).clear() }
                }
            _uiState.value = _uiState.value.copy(
                history = emptyList(),
                lastOutcome = null
            )
        }
    }
}
