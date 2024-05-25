package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.net.Uri

sealed class ConfirmSwapScreenState {

    data object Init : ConfirmSwapScreenState()

    data object Error : ConfirmSwapScreenState()

    data class Data(
        val processState: ProcessState,
        val srcConfirmTokenState: ConfirmTokenState,
        val dstConfirmTokenState: ConfirmTokenState,
        val confirmSwapInfo: ConfirmSwapInfoState,
        val sideEffects: Set<SideEffect> = emptySet()
    ) : ConfirmSwapScreenState()
}


data class ConfirmTokenState(
    val symbol: CharSequence,
    val iconUri: Uri,
    val walletCurrencyAmountFormat: CharSequence,
    val amountFormat: CharSequence
)

data class ConfirmSwapInfoState(
    val priceImpact: CharSequence,
    val minimumReceived: CharSequence,
    val providerFee: CharSequence,
    val blockchainFee: CharSequence,
    val route: CharSequence,
    val provider: CharSequence
)

enum class ProcessState {
    IDLE, PROCESSING, SUCCESS, ERROR
}

sealed class SideEffect {
    data class ExecuteCommand(val jsCommand: String) : SideEffect()
}