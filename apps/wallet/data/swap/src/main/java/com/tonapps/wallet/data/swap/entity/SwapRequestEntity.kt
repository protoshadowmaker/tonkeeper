package com.tonapps.wallet.data.swap.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapRequestEntity(
    val walletAddress: String,
    val fromToken: SwapTokenEntity,
    val toToken: SwapTokenEntity,
    val amount: Long,
    val amountToken: SwapTokenEntity
) : Parcelable