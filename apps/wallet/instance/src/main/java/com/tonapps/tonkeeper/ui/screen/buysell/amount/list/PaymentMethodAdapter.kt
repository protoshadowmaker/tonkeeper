package com.tonapps.tonkeeper.ui.screen.buysell.amount.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class PaymentMethodAdapter(
    private val onClickMethod: (id: String) -> Unit,
) : BaseListAdapter() {

    init {
        submitList(listOf())
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            PaymentMethodItem.TYPE_PAYMENT_METHOD -> PaymentMethodHolder(parent, onClickMethod)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.isNestedScrollingEnabled = true
    }
}