package com.tonapps.tonkeeper.ui.screen.swap.search.list

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

sealed class Item(type: Int) : BaseListItem(type) {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_SUGGESTED_LIST = 1
        const val TYPE_TOKEN = 2
        const val TYPE_TOKEN_SKELETON = 3
    }

    data class Header(val title: String) : Item(TYPE_HEADER)

    data class TokenSkeleton(val position: ListCell.Position) : Item(TYPE_TOKEN_SKELETON)

    data class Token(
        val position: ListCell.Position,
        val iconUri: Uri,
        val contractAddress: String,
        val symbol: String,
        val name: String,
        val balance: Float,
        val balanceFormat: CharSequence,
        val fiat: Float,
        val fiatFormat: CharSequence,
        val testnet: Boolean,
        val hiddenBalance: Boolean
    ) : Item(TYPE_TOKEN)
}