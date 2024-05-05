package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.net.Uri

data class SwapAmountScreenState(
    val srcTokenName: String,
    val srcTokenIconUri: Uri,
    val srcBalanceFormat: CharSequence? = null,
    val srcAmountFormat: CharSequence,
    val updateSrcAmount: Boolean = false,
    val dstTokenName: String,
    val dstTokenIconUri: Uri? = null,
    val dstBalanceFormat: CharSequence? = null,
    val dstAmountFormat: CharSequence? = null,
    val updateDstAmount: Boolean = false,
)