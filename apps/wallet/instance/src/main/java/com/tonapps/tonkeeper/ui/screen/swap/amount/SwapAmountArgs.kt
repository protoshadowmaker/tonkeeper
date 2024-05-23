package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.os.Bundle
import com.tonapps.extensions.requireString
import uikit.base.BaseArgs

data class SwapAmountArgs(
    val fromToken: String,
    val toToken: String?,
) : BaseArgs() {

    private companion object {
        private const val FROM_TOKEN = "from"
        private const val TO_TOKEN = "to"
    }

    constructor(bundle: Bundle) : this(
        fromToken = bundle.requireString(FROM_TOKEN),
        toToken = bundle.getString(TO_TOKEN)
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(FROM_TOKEN, fromToken)
        toToken?.let {
            putString(TO_TOKEN, it)
        }
    }
}