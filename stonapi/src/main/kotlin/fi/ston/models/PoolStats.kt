/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package fi.ston.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param baseId Base asset id
 * @param baseLiquidity Amount of liquidity of base asset
 * @param baseName Base asset name
 * @param baseSymbol Base asset symbol
 * @param baseVolume volume of base asset
 * @param lastPrice Last asset quota price in base assets
 * @param lpPrice [DEPRECATED] Liquidity pool token price
 * @param lpPriceUsd Liquidity pool token price
 * @param poolAddress Address of the pool
 * @param quoteId Quote asset id
 * @param quoteLiquidity Amount of liquidity of quote asset
 * @param quoteName Quote asset name
 * @param quoteSymbol Quote asset symbol
 * @param quoteVolume Volume of quote asset
 * @param routerAddress Address of router
 * @param url URL of the pool
 * @param apy Annual percentage yield
 */


data class PoolStats (

    /* Base asset id */
    @Json(name = "base_id")
    val baseId: kotlin.String,

    /* Amount of liquidity of base asset */
    @Json(name = "base_liquidity")
    val baseLiquidity: kotlin.String,

    /* Base asset name */
    @Json(name = "base_name")
    val baseName: kotlin.String,

    /* Base asset symbol */
    @Json(name = "base_symbol")
    val baseSymbol: kotlin.String,

    /* volume of base asset */
    @Json(name = "base_volume")
    val baseVolume: kotlin.String,

    /* Last asset quota price in base assets */
    @Json(name = "last_price")
    val lastPrice: kotlin.String,

    /* [DEPRECATED] Liquidity pool token price */
    @Json(name = "lp_price")
    val lpPrice: kotlin.String,

    /* Liquidity pool token price */
    @Json(name = "lp_price_usd")
    val lpPriceUsd: kotlin.String,

    /* Address of the pool */
    @Json(name = "pool_address")
    val poolAddress: kotlin.String,

    /* Quote asset id */
    @Json(name = "quote_id")
    val quoteId: kotlin.String,

    /* Amount of liquidity of quote asset */
    @Json(name = "quote_liquidity")
    val quoteLiquidity: kotlin.String,

    /* Quote asset name */
    @Json(name = "quote_name")
    val quoteName: kotlin.String,

    /* Quote asset symbol */
    @Json(name = "quote_symbol")
    val quoteSymbol: kotlin.String,

    /* Volume of quote asset */
    @Json(name = "quote_volume")
    val quoteVolume: kotlin.String,

    /* Address of router */
    @Json(name = "router_address")
    val routerAddress: kotlin.String,

    /* URL of the pool */
    @Json(name = "url")
    val url: kotlin.String,

    /* Annual percentage yield */
    @Json(name = "apy")
    val apy: kotlin.String? = null

)
