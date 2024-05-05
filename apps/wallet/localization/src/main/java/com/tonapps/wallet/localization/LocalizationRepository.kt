package com.tonapps.wallet.localization

import android.content.Context
import androidx.annotation.StringRes

class LocalizationRepository(private val context: Context) {
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }
}