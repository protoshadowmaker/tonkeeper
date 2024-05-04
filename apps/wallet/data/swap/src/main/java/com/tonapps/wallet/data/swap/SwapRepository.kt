package com.tonapps.wallet.data.swap

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.data.swap.entity.toSwapTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwapRepository(
    private val api: API
) {
    suspend fun getRemote(): List<SwapTokenEntity> = withContext(Dispatchers.IO) {
        //TODO handle exception
        api.getAssetList().map {
            it.toSwapTokenEntity()
        }
    }
}