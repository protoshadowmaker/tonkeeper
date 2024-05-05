package com.tonapps.tonkeeper.ui.screen.swap.search.list.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.swap.search.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import uikit.extensions.drawable
import uikit.extensions.gone
import uikit.extensions.invisible
import uikit.extensions.visible
import uikit.widget.FrescoView

class TokenHolder(
    parent: ViewGroup,
    private val onClickListener: (symbol: String) -> Unit
) :
    Holder<Item.Token>(parent, R.layout.view_cell_jetton) {

    private val amountZeroColor = context.textSecondaryColor
    private val amountPositiveColor = context.textPrimaryColor

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val nameView = findViewById<AppCompatTextView>(R.id.rate)
    private val balanceView = findViewById<AppCompatTextView>(R.id.balance)
    private val balanceFiatView = findViewById<AppCompatTextView>(R.id.balance_currency)

    override fun onBind(item: Item.Token) {
        itemView.background = item.position.drawable(context)
        itemView.setOnClickListener {
            onClickListener(item.symbol)
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