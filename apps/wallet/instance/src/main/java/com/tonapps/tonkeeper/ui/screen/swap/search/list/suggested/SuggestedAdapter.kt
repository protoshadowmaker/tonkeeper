package com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.holder.TokenHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class SuggestedAdapter(
    private val onClickToken: (contractAddress: String) -> Unit,
) : BaseListAdapter() {

    init {
        submitList(emptyList())
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            SuggestedTokenItem.TYPE_TOKEN -> TokenHolder(parent, onClickToken)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
    }

}