package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.net.Uri

data class SwapAmountScreenState(
    val srcTokenState: TokenState,
    val dstTokenState: TokenState,
    val swapInfoState: SwapInfoState? = null,
    val swapActionState: SwapActionState = SwapActionState.ENTER_AMOUNT,
    val sideEffects: Set<SideEffect> = emptySet()
)

data class TokenState(
    val selected: Boolean,
    val displayName: String? = null,
    val symbol: String? = null,
    val address: String? = null,
    val iconUri: Uri? = null,
    val balanceFormat: CharSequence? = null,
    val amount: Float = 0f,
    val amountFormat: CharSequence = "0"
)

data class SwapInfoState(
    val loading: Boolean,
    val swapRate: CharSequence,
    val priceImpact: CharSequence,
    val minimumReceived: CharSequence,
    val providerFee: CharSequence,
    val blockchainFee: String,
    val route: String,
    val provider: String
)

enum class SwapActionState {
    ENTER_AMOUNT, CHOOSE_TOKEN, PROGRESS, CONTINUE
}

enum class SideEffect {
    UPDATE_SRC_TOKEN, UPDATE_DST_TOKEN
}