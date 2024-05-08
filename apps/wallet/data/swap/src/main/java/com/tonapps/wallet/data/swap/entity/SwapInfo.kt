package com.tonapps.wallet.data.swap.entity

import fi.ston.models.DexReverseSimulateSwap200Response

data class SwapInfo(
    val offerValue: Long,
    val askValue: Long,
    val swapRate: Float,
    val priceImpact: Float,
    val minimumReceived: Long,
    val liquidityFee: Long
)

fun DexReverseSimulateSwap200Response.toSwapInfo(): SwapInfo {
    return SwapInfo(
        offerValue = this.offerUnits.toLong(),
        askValue = this.askUnits.toLong(),
        swapRate = this.swapRate.toFloat(),
        priceImpact = this.priceImpact.toFloat(),
        minimumReceived = this.minAskUnits.toLong(),
        liquidityFee = this.feeUnits.toLong()
    )
}