package com.tonapps.tonkeeper.ui.screen.buysell.amount.list

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

sealed class PaymentMethodItem(type: Int) : BaseListItem(type) {
    companion object {
        const val TYPE_PAYMENT_METHOD = 0
    }

    data class PaymentMethod(
        val position: ListCell.Position,
        val id: String,
        val selected: Boolean,
        val iconResId: Int,
        val title: String,
    ) : PaymentMethodItem(TYPE_PAYMENT_METHOD)
}