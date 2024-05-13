package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.os.Bundle
import com.tonapps.extensions.requireString
import uikit.base.BaseArgs

data class ConfirmSwapArgs(
    val walletAddress: String,
    val fromToken: String,
    val toToken: String,
    val amount: Long,
    val amountToken: String
) : BaseArgs() {

    private companion object {
        private const val WALLET_ADDRESS = "wallet"
        private const val FROM_TOKEN = "from"
        private const val TO_TOKEN = "to"
        private const val AMOUNT = "amount"
        private const val AMOUNT_TOKEN = "amountToken"
    }

    constructor(bundle: Bundle) : this(
        walletAddress = bundle.requireString(WALLET_ADDRESS),
        fromToken = bundle.requireString(FROM_TOKEN),
        toToken = bundle.requireString(TO_TOKEN),
        amount = bundle.getLong(AMOUNT),
        amountToken = bundle.requireString(AMOUNT_TOKEN),
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(WALLET_ADDRESS, walletAddress)
        putString(FROM_TOKEN, fromToken)
        putString(TO_TOKEN, toToken)
        putLong(AMOUNT, amount)
        putString(AMOUNT_TOKEN, amountToken)
    }
}