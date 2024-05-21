package com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem

sealed class SuggestedTokenItem(type: Int) : BaseListItem(type) {
    companion object {
        const val TYPE_TOKEN = 0
    }

    data class Token(
        val selected: Boolean,
        val iconUri: Uri,
        val contractAddress: String,
        val symbol: String,
    ) : SuggestedTokenItem(TYPE_TOKEN)
}