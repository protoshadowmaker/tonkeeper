package com.tonapps.tonkeeper.ui.screen.swap.search.list.all.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.swap.search.list.all.TokenItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.color.buttonTertiaryBackgroundColor
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import uikit.drawable.DrawableCache
import uikit.extensions.cacheKey
import uikit.extensions.drawable
import uikit.extensions.gone
import uikit.extensions.invisible
import uikit.extensions.visible
import uikit.widget.FrescoView

class TokenHolder(
    parent: ViewGroup,
    private val bgCache: DrawableCache = DrawableCache(),
    private val onClickListener: (contractAddress: String) -> Unit
) : Holder<TokenItem.Token>(parent, R.layout.view_cell_jetton) {

    private val amountZeroColor = context.textSecondaryColor
    private val amountPositiveColor = context.textPrimaryColor

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val nameView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)

    override fun onBind(item: TokenItem.Token) {
        val bgColor = if (item.selected) {
            context.buttonTertiaryBackgroundColor
        } else {
            context.backgroundContentColor
        }
        itemView.background = bgCache.getOrCreate(item.position.cacheKey(bgColor)) {
            item.position.drawable(context, bgColor)
        }
        itemView.setOnClickListener {
            onClickListener(item.contractAddress)
        }
        iconView.setImageURI(item.iconUri, this)
        titleView.text = item.symbol
        balanceView.text = if (item.hiddenBalance) {
            HIDDEN_BALANCE
        } else {
            item.balanceFormat
        }
        nameView.text = item.name

        if (item.balance > 0f) {
            balanceView.setTextColor(amountPositiveColor)
        } else {
            balanceView.setTextColor(amountZeroColor)
        }
        if (item.testnet) {
            balanceFiatView.gone()
        } else {
            if (item.balance > 0f) {
                balanceFiatView.visible()
            } else {
                balanceFiatView.invisible()
            }
            if (item.hiddenBalance) {
                balanceFiatView.text = HIDDEN_BALANCE
            } else {
                balanceFiatView.text = item.fiatFormat
            }
        }
    }
}