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

package fi.ston.apis

import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.HttpUrl

import fi.ston.models.PoolStats

import com.squareup.moshi.Json

import fi.ston.infrastructure.ApiClient
import fi.ston.infrastructure.ApiResponse
import fi.ston.infrastructure.ClientException
import fi.ston.infrastructure.ClientError
import fi.ston.infrastructure.ServerException
import fi.ston.infrastructure.ServerError
import fi.ston.infrastructure.MultiValueMap
import fi.ston.infrastructure.PartConfig
import fi.ston.infrastructure.RequestConfig
import fi.ston.infrastructure.RequestMethod
import fi.ston.infrastructure.ResponseType
import fi.ston.infrastructure.Success
import fi.ston.infrastructure.toMultiValue

class ExportApi(basePath: kotlin.String = defaultBasePath, client: OkHttpClient = ApiClient.defaultClient) : ApiClient(basePath, client) {
    companion object {
        @JvmStatic
        val defaultBasePath: String by lazy {
            System.getProperties().getProperty(ApiClient.baseUrlKey, "http://localhost")
        }
    }

    /**
     * 
     * 
     * @return kotlin.collections.List<kotlin.collections.Map<kotlin.String, PoolStats>>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     * @throws UnsupportedOperationException If the API returns an informational or redirection response
     * @throws ClientException If the API returns a client error response
     * @throws ServerException If the API returns a server error response
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class, UnsupportedOperationException::class, ClientException::class, ServerException::class)
    fun getCmcStats() : kotlin.collections.List<kotlin.collections.Map<kotlin.String, PoolStats>> {
        val localVarResponse = getCmcStatsWithHttpInfo()

        return when (localVarResponse.responseType) {
            ResponseType.Success -> (localVarResponse as Success<*>).data as kotlin.collections.List<kotlin.collections.Map<kotlin.String, PoolStats>>
            ResponseType.Informational -> throw UnsupportedOperationException("Client does not support Informational responses.")
            ResponseType.Redirection -> throw UnsupportedOperationException("Client does not support Redirection responses.")
            ResponseType.ClientError -> {
                val localVarError = localVarResponse as ClientError<*>
                throw ClientException("Client error : ${localVarError.statusCode} ${localVarError.message.orEmpty()}", localVarError.statusCode, localVarResponse)
            }
            ResponseType.ServerError -> {
                val localVarError = localVarResponse as ServerError<*>
                throw ServerException("Server error : ${localVarError.statusCode} ${localVarError.message.orEmpty()} ${localVarError.body}", localVarError.statusCode, localVarResponse)
            }
        }
    }

    /**
     * 
     * 
     * @return ApiResponse<kotlin.collections.List<kotlin.collections.Map<kotlin.String, PoolStats>>?>
     * @throws IllegalStateException If the request is not correctly configured
     * @throws IOException Rethrows the OkHttp execute method exception
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalStateException::class, IOException::class)
    fun getCmcStatsWithHttpInfo() : ApiResponse<kotlin.collections.List<kotlin.collections.Map<kotlin.String, PoolStats>>?> {
        val localVariableConfig = getCmcStatsRequestConfig()

        return request<Unit, kotlin.collections.List<kotlin.collections.Map<kotlin.String, PoolStats>>>(
            localVariableConfig
        )
    }

    /**
     * To obtain the request config of the operation getCmcStats
     *
     * @return RequestConfig
     */
    fun getCmcStatsRequestConfig() : RequestConfig<Unit> {
        val localVariableBody = null
        val localVariableQuery: MultiValueMap = mutableMapOf()
        val localVariableHeaders: MutableMap<String, String> = mutableMapOf()
        localVariableHeaders["Accept"] = "application/json"

        return RequestConfig(
            method = RequestMethod.GET,
            path = "/export/cmc/v1",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
            body = localVariableBody
        )
    }


    private fun encodeURIComponent(uriComponent: kotlin.String): kotlin.String =
        HttpUrl.Builder().scheme("http").host("localhost").addPathSegment(uriComponent).build().encodedPathSegments[0]
}
