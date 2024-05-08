package com.tonapps.tonkeeper.ui.screen.swap.amount

import androidx.lifecycle.ViewModel
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.localization.LocalizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SwapAmountViewModel(
    private val l10n: LocalizationRepository,
    private val settings: SettingsRepository,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private var srcToken: SwapTokenEntity? = SwapTokenEntity.TON
    private var dstToken: SwapTokenEntity? = null
    private var srcAmount: Float = 0f
    private var dstAmount: Float = 0f

    private val _uiStateFlow = MutableStateFlow(
        SwapAmountScreenState(
            srcTokenState = buildTokenState(srcToken),
            dstTokenState = buildTokenState(dstToken)
        )
    )
    val uiStateFlow: StateFlow<SwapAmountScreenState> = _uiStateFlow.asStateFlow()

    fun onSourceChanged(contractAddress: String) {
        srcToken = swapRepository.getCached(contractAddress)
        val state = uiStateFlow.value
        val tokenState = buildTokenState(srcToken)
            .copy(amountFormat = state.srcTokenState.amountFormat)
        submitDataToUi(state.copy(srcTokenState = tokenState))
    }

    fun onSourceValueChanged(amount: Float) {

    }

    fun onDestinationChanged(contractAddress: String) {
        dstToken = swapRepository.getCached(contractAddress)
        val state = uiStateFlow.value
        val tokenState = buildTokenState(dstToken)
            .copy(amountFormat = state.dstTokenState.amountFormat)
        submitDataToUi(state.copy(dstTokenState = tokenState))
    }

    fun onDestinationValueChanged(amount: Float) {

    }

    private fun buildTokenState(token: SwapTokenEntity?): TokenState {
        return if (token == null) {
            TokenState(
                selected = false,
                symbol = l10n.getString(com.tonapps.wallet.localization.R.string.choose_token)
            )
        } else {
            TokenState(
                selected = true,
                displayName = token.displayName,
                symbol = token.symbol,
                address = token.contractAddress,
                iconUri = token.iconUri
            )
        }
    }

    private fun submitDataToUi(state: SwapAmountScreenState) {
        _uiStateFlow.value = state
    }
}