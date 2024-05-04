package com.tonapps.tonkeeper.ui.screen.swap.tokens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.swap.tokens.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwapTokensViewModel(
    private val settings: SettingsRepository,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private val _uiItemsFlow = MutableStateFlow<List<Item>>(emptyList())
    val uiItemsFlow = _uiItemsFlow.asStateFlow().filter { it.isNotEmpty() }

    init {
        viewModelScope.launch {
            getRemoteSwapTokens()?.let {
                _uiItemsFlow.value = convertToUi(it)
            }
        }
    }

    private suspend fun getRemoteSwapTokens(): List<SwapTokenEntity>? =
        withContext(Dispatchers.IO) {
            try {
                swapRepository.getRemote()
            } catch (e: Throwable) {
                null
            }
        }

    private suspend fun convertToUi(tokens: List<SwapTokenEntity>): List<Item> =
        withContext(Dispatchers.Default) {
            val hiddenBalance = settings.hiddenBalances
            tokens.mapIndexed { index, swapTokenEntity ->
                swapTokenEntity.toUi(
                    testnet = false,
                    hiddenBalance = hiddenBalance,
                    index = index,
                    size = tokens.size
                )
            }
        }

    private fun SwapTokenEntity.toUi(
        testnet: Boolean,
        hiddenBalance: Boolean,
        index: Int,
        size: Int
    ): Item.Token {
        return Item.Token(
            position = ListCell.getPosition(size, index),
            iconUri = iconUri,
            contractAddress = contractAddress,
            symbol = symbol,
            name = displayName,
            balance = 0f,
            balanceFormat = "0",
            fiat = 0f,
            fiatFormat = "0",
            testnet = testnet,
            hiddenBalance = hiddenBalance
        )
    }
}