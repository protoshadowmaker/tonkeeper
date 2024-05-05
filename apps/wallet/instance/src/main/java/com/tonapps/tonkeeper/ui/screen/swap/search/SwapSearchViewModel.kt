package com.tonapps.tonkeeper.ui.screen.swap.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.swap.search.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwapSearchViewModel(
    private val settings: SettingsRepository,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private var searchJob: Job? = null
    private var pendingSearchQuery: String? = null
    private var allTokensUi: List<Item.Token> = emptyList()
    private val _uiItemsFlow = MutableStateFlow<List<Item>>(
        listOf(
            Item.TokenSkeleton(ListCell.Position.FIRST),
            Item.TokenSkeleton(ListCell.Position.MIDDLE),
            Item.TokenSkeleton(ListCell.Position.LAST)
        )
    )
    val uiItemsFlow = _uiItemsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            getRemoteSwapTokens()?.let {
                allTokensUi = convertToUi(it)
                pendingSearchQuery?.let { query ->
                    search(query)
                    pendingSearchQuery = null
                } ?: submitDataToUi(allTokensUi)
            }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        if (allTokensUi.isEmpty()) {
            pendingSearchQuery = query
            return
        }
        if (query.isBlank()) {
            submitDataToUi(allTokensUi)
            return
        }
        searchJob = viewModelScope.launch {
            submitDataToUi(convertToUi(swapRepository.search(query)))
        }
    }

    private fun submitDataToUi(items: List<Item>) {
        _uiItemsFlow.value = items
    }

    private suspend fun getRemoteSwapTokens(): List<SwapTokenEntity>? =
        withContext(Dispatchers.IO) {
            try {
                swapRepository.getRemote()
            } catch (e: Throwable) {
                null
            }
        }

    private suspend fun convertToUi(tokens: List<SwapTokenEntity>): List<Item.Token> =
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