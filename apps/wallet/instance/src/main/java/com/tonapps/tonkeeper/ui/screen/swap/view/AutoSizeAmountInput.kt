package com.tonapps.tonkeeper.ui.screen.swap.view

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter

class AutoSizeAmountInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyle), TextWatcher {

    var doAfterValueChanged: ((Double) -> Unit)? = null

    private var decimalCount = 9

    private var originalTextSize: Float = textSize
    private val separator = CurrencyFormatter.monetaryDecimalSeparator

    init {
        filters = arrayOf(InputFilter.LengthFilter(21))
        addTextChangedListener(this)
        gravity = Gravity.END
    }

    private fun resizeText() {
        val stepSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 0.5f, resources.displayMetrics
        )
        val minTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 8f, resources.displayMetrics
        )
        var currentTextSize = originalTextSize
        applyTextSize(currentTextSize)
        while (paint.measureText(editableText.toString()) > measuredWidth && currentTextSize > minTextSize) {
            currentTextSize -= stepSize
            applyTextSize(currentTextSize)
        }
    }

    private fun applyTextSize(size: Float) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    override fun afterTextChanged(editable: Editable) {
        val indexDot = editable.indexOf(".")
        val usingDot = indexDot != -1
        val indexComa = editable.indexOf(",")
        val usingComa = indexComa != -1
        if (usingDot && separator != ".") {
            editable.replace(indexDot, indexDot + 1, separator)
            return
        } else if (usingComa && separator != ",") {
            editable.replace(indexComa, indexComa + 1, separator)
            return
        } else if (editable.contains(" ")) {
            editable.replace(editable.indexOf(" "), editable.indexOf(" ") + 1, "")
            return
        } else if (indexDot == 0 || indexComa == 0) {
            editable.insert(0, "0")
            return
        } else if (editable.length > 1 && editable.startsWith("0") && !editable.startsWith("0${separator}")) {
            editable.delete(0, 1)
            return
        } else if (editable.length == 1 && editable.equals(separator)) {
            editable.insert(0, "0")
            return
        }

        val dividerCount = editable.count { it == separator[0] }
        if (dividerCount > 1) {
            val lastDividerIndex = editable.lastIndexOf(separator[0])
            editable.delete(lastDividerIndex, lastDividerIndex + 1)
            return
        }
        resizeText()
        if (checkDecimalCount(editable)) {
            updateValue(editable)
        }
    }

    private fun updateValue(editable: Editable) {
        val text = Coin.prepareValue(editable.toString())
        doAfterValueChanged?.invoke(text.toDoubleOrNull() ?: 0.0)
    }

    fun setDecimalCount(count: Int) {
        decimalCount = count
        text?.let { checkDecimalCount(it) }
    }

    private fun checkDecimalCount(editable: Editable): Boolean {
        val index = editable.indexOf(separator)
        if (index != -1) {
            val decimalPart = editable.substring(index + 1)
            if (decimalPart.length > decimalCount) {
                editable.delete(index + decimalCount + 1, editable.length)
                return false
            }
        }
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        resizeText()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw) {
            resizeText()
            post { requestLayout() }
        }
    }
}