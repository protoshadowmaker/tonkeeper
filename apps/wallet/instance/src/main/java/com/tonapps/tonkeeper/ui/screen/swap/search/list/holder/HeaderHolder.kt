package com.tonapps.tonkeeper.ui.screen.swap.search.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.swap.search.list.Item
import com.tonapps.tonkeeperx.R
import uikit.extensions.setPaddingVertical
import uikit.widget.TitleView

class HeaderHolder(parent: ViewGroup) : Holder<Item.Header>(parent, R.layout.view_swap_header) {

    private val titleView = findViewById<TitleView>(R.id.title)

    init {
        titleView.setPaddingVertical(0)
    }

    override fun onBind(item: Item.Header) {
        titleView.text = item.title
    }

}