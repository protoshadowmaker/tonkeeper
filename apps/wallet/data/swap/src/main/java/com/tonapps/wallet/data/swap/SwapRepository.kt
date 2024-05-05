package com.tonapps.wallet.data.swap

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.data.swap.entity.toSwapTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwapRepository(
    private val api: API
) {

    private var cache: List<SwapTokenEntity> = emptyList()

    suspend fun getRemote(): List<SwapTokenEntity> = withContext(Dispatchers.IO) {
        load()
    }

    suspend fun search(query: String): List<SwapTokenEntity> = withContext(Dispatchers.Default) {
        val searchQuery = query.trim()
        cache.filter {
            it.symbol.contains(searchQuery, true) || it.displayName.contains(query, true)
        }.sortedWith(
            compareBy(
                { it.symbol.searchIndexOf(searchQuery) },
                { it.displayName.indexOf(searchQuery) },
                { it.symbol.length },
                { it.displayName.length }
            )
        )
    }

    private suspend fun load(): List<SwapTokenEntity> = withContext(Dispatchers.IO) {
        cache = api.getAssetList().map {
            it.toSwapTokenEntity()
        }
        cache
    }

    private fun String.searchIndexOf(query: String): Int {
        val index = indexOf(query, ignoreCase = true)
        return if (index == -1) {
            Int.MAX_VALUE
        } else {
            index
        }
    }
}