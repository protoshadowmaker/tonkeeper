package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.os.Bundle
import android.os.Parcelable
import com.tonapps.extensions.requireParcelableCompat
import com.tonapps.wallet.data.swap.entity.SwapRequestEntity
import kotlinx.parcelize.Parcelize
import uikit.base.BaseArgs

data class ConfirmSwapArgs(
    val swapRequest: SwapRequestEntity,
    val initialState: InitialState,
) : BaseArgs() {

    private companion object {
        private const val REQUEST = "request"
        private const val INITIAL_STATE = "initialState"
    }

    constructor(bundle: Bundle) : this(
        swapRequest = bundle.requireParcelableCompat(REQUEST),
        initialState = bundle.requireParcelableCompat(INITIAL_STATE),
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putParcelable(REQUEST, swapRequest)
        putParcelable(INITIAL_STATE, initialState)
    }

    @Parcelize
    data class InitialState(
        val swapRate: CharSequence,
        val priceImpact: CharSequence,
        val minimumReceived: CharSequence,
        val providerFee: CharSequence,
        val blockchainFee: CharSequence,
        val route: CharSequence,
        val provider: CharSequence,
    ) : Parcelable
}