package com.cachescope.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cachescope.data.analytics.AnalyticsEngine
import com.cachescope.data.analytics.BenchmarkResult
import com.cachescope.data.analytics.StrategyStats
import com.cachescope.data.cache.CacheType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val summaries: List<StrategyStats> = emptyList(),
    val recentResults: List<BenchmarkResult> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analytics: AnalyticsEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState(isLoading = true))
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        analytics.observeAll()
            .onEach { results ->
                val summaries = analytics.allSummaries()
                _uiState.value = AnalyticsUiState(
                    summaries = summaries,
                    recentResults = results.take(30),
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun clearAll() = viewModelScope.launch {
        analytics.clearAll()
    }
}
