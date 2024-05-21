package com.tonapps.tonkeeper.ui.screen.swap.search.list.all

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell

sealed class TokenItem(type: Int) : BaseListItem(type) {
    companion object {
        const val TYPE_TOKEN = 0
        const val TYPE_TOKEN_SKELETON = 1
    }

    data class TokenSkeleton(val position: ListCell.Position) : TokenItem(TYPE_TOKEN_SKELETON)

    data class Token(
        val position: ListCell.Position,
        val selected: Boolean,
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
    ) : TokenItem(TYPE_TOKEN)
}