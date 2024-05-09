package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.net.Uri

data class SwapAmountScreenState(
    val srcTokenState: TokenState,
    val dstTokenState: TokenState,
    val swapInfoState: SwapInfoState? = null
)

data class TokenState(
    val selected: Boolean,
    val displayName: String? = null,
    val symbol: String? = null,
    val address: String? = null,
    val iconUri: Uri? = null,
    val balanceFormat: CharSequence? = null,
    val amount: Float = 0f,
    val amountFormat: CharSequence = "0",
    val updateAmount: Boolean = false,
)

data class SwapInfoState(
    val priceImpact: String,
    val minimumReceived: String,
    val providerFee: String,
    val blockchainFee: String,
    val route: String,
    val provider: String
)