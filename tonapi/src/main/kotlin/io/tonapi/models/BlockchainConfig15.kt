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
 * The reward in nanoTons for block creation in the TON blockchain.
 *
 * @param validatorsElectedFor 
 * @param electionsStartBefore 
 * @param electionsEndBefore 
 * @param stakeHeldFor 
 */


data class BlockchainConfig15 (

    @Json(name = "validators_elected_for")
    val validatorsElectedFor: kotlin.Long,

    @Json(name = "elections_start_before")
    val electionsStartBefore: kotlin.Long,

    @Json(name = "elections_end_before")
    val electionsEndBefore: kotlin.Long,

    @Json(name = "stake_held_for")
    val stakeHeldFor: kotlin.Long

)

