package com.tonapps.wallet.data.swap.entity

import android.net.Uri
import android.os.Parcelable
import com.tonapps.wallet.api.R
import fi.ston.models.AssetInfoSchema
import fi.ston.models.AssetKindSchema
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapTokenEntity(
    val symbol: String,
    val tokenType: TokenType,
    val contractAddress: String,
    val decimals: Int,
    val defaultSymbol: Boolean,
    val community: Boolean,
    val blacklisted: Boolean,
    val deprecated: Boolean,
    val displayName: String,
    val iconUri: Uri,
) : Parcelable {

    enum class TokenType {
        TON, JETTON, WTON
    }

    val isTon: Boolean
        get() = contractAddress == TON.contractAddress

    companion object {
        val TON = SwapTokenEntity(
            symbol = "TON",
            contractAddress = "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c",
            tokenType = TokenType.TON,
            decimals = 9,
            defaultSymbol = true,
            community = false,
            blacklisted = false,
            deprecated = false,
            displayName = "TON",
            iconUri = Uri.Builder().scheme("res").path(R.drawable.ic_ton_with_bg.toString())
                .build(),
        )

        fun convertTokeType(kind: AssetKindSchema): TokenType {
            return when (kind) {
                AssetKindSchema.Ton -> TokenType.TON
                AssetKindSchema.Jetton -> TokenType.JETTON
                AssetKindSchema.Wton -> TokenType.WTON
            }
        }
    }
}

fun AssetInfoSchema.toSwapTokenEntity(): SwapTokenEntity {
    return SwapTokenEntity(
        symbol = this.symbol,
        contractAddress = this.contractAddress,
        tokenType = SwapTokenEntity.convertTokeType(this.kind),
        decimals = this.decimals,
        defaultSymbol = this.defaultSymbol,
        community = this.community,
        blacklisted = this.blacklisted,
        deprecated = this.deprecated,
        displayName = this.displayName ?: "",
        iconUri = Uri.parse(this.imageUrl ?: ""),
    )
}