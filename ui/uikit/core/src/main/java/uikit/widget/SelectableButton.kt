package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import uikit.drawable.InputDrawable

class SelectableButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : Button(context, attrs, defStyle) {
    private val inputDrawable = InputDrawable(context)

    init {
        background = inputDrawable
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        inputDrawable.active = selected
    }
}