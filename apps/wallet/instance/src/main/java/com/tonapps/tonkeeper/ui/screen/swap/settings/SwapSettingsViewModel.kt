package com.tonapps.tonkeeper.ui.screen.swap.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn

class SwapSettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var initJob: Job? = null

    private val _uiStateFlow = MutableEffectFlow<SwapSettingsScreenState>()
    val uiStateFlow = _uiStateFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    ).filterNotNull()

    init {
        initJob = combine(
            settingsRepository.slippageValueFlow,
            settingsRepository.slippageExpertFlow
        ) { slippageValue, slippageExpert ->
            _uiStateFlow.tryEmit(SwapSettingsScreenState(slippageValue, slippageExpert))
            initJob?.cancel()
        }.launchIn(viewModelScope)
    }

    fun onSlippage1Clicked() {
        updateSlippageToDefault(1f)
    }

    fun onSlippage3Clicked() {
        updateSlippageToDefault(3f)
    }

    fun onSlippage5Clicked() {
        updateSlippageToDefault(5f)
    }

    private fun updateSlippageToDefault(value: Float) {
        settingsRepository.slippageExpert = false
        settingsRepository.slippageValue = value
    }
}