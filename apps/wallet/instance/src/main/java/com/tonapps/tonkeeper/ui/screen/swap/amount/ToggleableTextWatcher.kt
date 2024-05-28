package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.text.Editable
import android.text.TextWatcher

class ToggleableTextWatcher(
    private val beforeTextChanged: (
        text: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) -> Unit = { _, _, _, _ -> },
    private val onTextChanged: (
        text: CharSequence?,
        start: Int,
        before: Int,
        count: Int
    ) -> Unit = { _, _, _, _ -> },
    private val afterTextChanged: (text: Editable?) -> Unit = {}
) : TextWatcher {

    private var enabled: Boolean = true

    override fun afterTextChanged(s: Editable?) {
        if (enabled) {
            afterTextChanged.invoke(s)
        }
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
        if (enabled) {
            beforeTextChanged.invoke(text, start, count, after)
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        if (enabled) {
            onTextChanged.invoke(text, start, before, count)
        }
    }

    fun ignore(action: () -> Unit) {
        enabled = false
        action()
        enabled = true
    }
}