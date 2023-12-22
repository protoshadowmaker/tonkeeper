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

import io.tonapi.models.JettonBridgeParams

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Bridge parameters for wrapping tokens from other networks into tokens on the TON network.
 *
 * @param jettonBridgeParams 
 */


data class BlockchainConfig79 (

    @Json(name = "jetton_bridge_params")
    val jettonBridgeParams: JettonBridgeParams

)

