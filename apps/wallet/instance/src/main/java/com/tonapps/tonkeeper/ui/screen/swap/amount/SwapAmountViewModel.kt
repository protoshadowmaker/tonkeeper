package com.tonapps.tonkeeper.ui.screen.swap.amount

import androidx.lifecycle.ViewModel
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.localization.LocalizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SwapAmountViewModel(
    private val l10n: LocalizationRepository,
    private val settings: SettingsRepository
) : ViewModel() {
    private val _uiStateFlow = MutableStateFlow(
        SwapAmountScreenState(
            srcTokenName = SwapTokenEntity.TON.displayName,
            srcTokenIconUri = SwapTokenEntity.TON.iconUri,
            srcAmountFormat = "",
            dstTokenName = l10n.getString(com.tonapps.wallet.localization.R.string.choose_token)
        )
    )
    val uiStateFlow: StateFlow<SwapAmountScreenState> = _uiStateFlow.asStateFlow()
}