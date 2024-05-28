package com.tonapps.tonkeeper.ui.screen.buysell.amount

import com.tonapps.tonkeeper.ui.screen.buysell.amount.list.PaymentMethodItem

data class BuySellAmountScreenState(
    val decimals: Int = 0,
    val rate: CharSequence = "0",
    val minAmount: CharSequence = "",
    val paymentMethods: List<PaymentMethodItem> = emptyList(),
)