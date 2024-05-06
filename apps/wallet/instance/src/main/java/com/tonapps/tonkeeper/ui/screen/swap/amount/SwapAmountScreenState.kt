package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.net.Uri

data class SwapAmountScreenState(
    val srcTokenState: TokenState,
    val dstTokenState: TokenState,
)

data class TokenState(
    val selected: Boolean,
    val displayName: String,
    val symbol: String? = null,
    val iconUri: Uri? = null,
    val balanceFormat: CharSequence? = null,
    val amountFormat: CharSequence? = null,
    val updateAmount: Boolean = false,
)