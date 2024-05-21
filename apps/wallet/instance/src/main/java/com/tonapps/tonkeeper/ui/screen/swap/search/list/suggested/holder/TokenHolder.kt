package com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.SuggestedTokenItem
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class TokenHolder(
    parent: ViewGroup,
    private val onClickListener: (contractAddress: String) -> Unit
) : Holder<SuggestedTokenItem.Token>(parent, R.layout.view_cell_jetton_compact) {

    private val iconView = findViewById<FrescoView>(R.id.tokenIcon)
    private val titleView = findViewById<AppCompatTextView>(R.id.tokenSymbol)

    override fun onBind(item: SuggestedTokenItem.Token) {
        itemView.setOnClickListener {
            onClickListener(item.contractAddress)
        }
        iconView.setImageURI(item.iconUri, this)
        titleView.text = item.symbol
    }
}