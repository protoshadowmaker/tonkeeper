package com.tonapps.tonkeeper.ui.screen.swap.search.list.all.holder

import android.view.View
import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.swap.search.list.all.TokenItem
import com.tonapps.tonkeeperx.R
import uikit.extensions.drawable

class TokenSkeletonHolder(parent: ViewGroup) : Holder<TokenItem.TokenSkeleton>(
    parent, R.layout.view_swap_token_skeleton
) {

    private val stubView: View = findViewById(R.id.stub)

    override fun onBind(item: TokenItem.TokenSkeleton) {
        stubView.background = item.position.drawable(context)
    }

}