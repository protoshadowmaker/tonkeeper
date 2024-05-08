package com.tonapps.tonkeeper.ui.screen.swap.search.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.swap.search.list.holder.HeaderHolder
import com.tonapps.tonkeeper.ui.screen.swap.search.list.holder.TokenHolder
import com.tonapps.tonkeeper.ui.screen.swap.search.list.holder.TokenSkeletonHolder
import com.tonapps.uikit.list.BaseListAdapter
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem
import uikit.drawable.DrawableCache

class Adapter(
    private val onClickToken: (contractAddress: String) -> Unit,
) : BaseListAdapter() {

    private val itemsBgCache: DrawableCache = DrawableCache()

    init {
        submitList(emptyList())
    }

    override fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.TYPE_HEADER -> HeaderHolder(parent)
            Item.TYPE_SUGGESTED_LIST -> TODO()
            Item.TYPE_TOKEN -> TokenHolder(parent, itemsBgCache, onClickToken)
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