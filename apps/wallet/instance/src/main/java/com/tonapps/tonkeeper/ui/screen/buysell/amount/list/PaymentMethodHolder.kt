package com.tonapps.tonkeeper.ui.screen.buysell.amount.list

import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.list.BaseListHolder
import uikit.extensions.drawable
import uikit.widget.RadioButtonView

class PaymentMethodHolder(
    parent: ViewGroup,
    private val onClickListener: (id: String) -> Unit
) : BaseListHolder<PaymentMethodItem.PaymentMethod>(parent, R.layout.view_cell_payment_method) {

    private val radioButton = findViewById<RadioButtonView>(R.id.radioButton)
    private val imageView = findViewById<ImageView>(R.id.icon)
    private val textView = findViewById<AppCompatTextView>(R.id.title)

    override fun onBind(item: PaymentMethodItem.PaymentMethod) {
        itemView.background = item.position.drawable(context, context.backgroundContentColor)
        itemView.setOnClickListener {
            onClickListener(item.id)
        }
        radioButton.checked = item.selected
        imageView.setImageResource(item.iconResId)
        textView.text = item.title
    }
}