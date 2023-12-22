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

import io.tonapi.models.BlockParamLimits

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param bytes 
 * @param gas 
 * @param ltDelta 
 */


data class BlockLimits (

    @Json(name = "bytes")
    val bytes: BlockParamLimits,

    @Json(name = "gas")
    val gas: BlockParamLimits,

    @Json(name = "lt_delta")
    val ltDelta: BlockParamLimits

)

