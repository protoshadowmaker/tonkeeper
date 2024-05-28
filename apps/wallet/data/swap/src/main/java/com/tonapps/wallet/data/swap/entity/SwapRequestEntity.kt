package com.tonapps.wallet.data.swap.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapRequestEntity(
    val walletAddress: String,
    val fromTokenAddress: String,
    val toTokenAddress: String,
    val srcAmount: Long,
    val dstAmount: Long,
    val minAmount: Long,
    val reverse: Boolean,
    val slippageTolerance: Float
) : Parcelable