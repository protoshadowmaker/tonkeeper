package com.tonapps.tonkeeper.ui.screen.swap.watcher

import android.text.Editable
import android.text.TextWatcher
import com.tonapps.icu.CurrencyFormatter
import java.math.BigDecimal

class DecimalInputWatcher : TextWatcher {

    var decimalCount = 9
    var maxValue: Double = Double.MAX_VALUE
    private val separator = CurrencyFormatter.monetaryDecimalSeparator
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
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
        checkValueLimit(editable)
        checkDecimalCount(editable)
    }

    private fun checkValueLimit(editable: Editable) {
        val value = editable.toString().toDoubleOrNull() ?: return
        if (value > maxValue) {
            val newFormattedValue = CurrencyFormatter.format(
                value = BigDecimal.valueOf(maxValue),
                decimals = 0..decimalCount,
                group = false
            )
            editable.replace(0, editable.length, newFormattedValue)
        }
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
}