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

package io.tonapi.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param prices 
 * @param diff24h 
 * @param diff7d 
 * @param diff30d 
 */


data class TokenRates (

    @Json(name = "prices")
    val prices: kotlin.collections.Map<kotlin.String, java.math.BigDecimal>? = null,

    @Json(name = "diff_24h")
    val diff24h: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "diff_7d")
    val diff7d: kotlin.collections.Map<kotlin.String, kotlin.String>? = null,

    @Json(name = "diff_30d")
    val diff30d: kotlin.collections.Map<kotlin.String, kotlin.String>? = null

)

