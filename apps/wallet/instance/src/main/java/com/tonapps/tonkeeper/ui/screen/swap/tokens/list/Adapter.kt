package com.tonapps.tonkeeper.ui.screen.swap.tokens.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.swap.tokens.list.holder.HeaderHolder
import com.tonapps.tonkeeper.ui.screen.swap.tokens.list.holder.TokenHolder
import com.tonapps.tonkeeper.ui.screen.swap.tokens.list.holder.TokenSkeletonHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

class Adapter(
    private val onClickToken: () -> Unit,
) : BaseListAdapter() {

    init {
        submitList(listOf(Item.TokenSkeleton))
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.TYPE_HEADER -> HeaderHolder(parent)
            Item.TYPE_SUGGESTED_LIST -> TODO()
            Item.TYPE_TOKEN -> TokenHolder(parent)
            Item.TYPE_TOKEN_SKELETON -> TokenSkeletonHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(false)
        recyclerView.isNestedScrollingEnabled = true
    }

}