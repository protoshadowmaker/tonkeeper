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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class SearchSwapTokenViewModel(
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

    var selectedAddress: String = ""
    var excludedAddress: String = ""

    fun loadData() {
        viewModelScope.launch {
            var tokens: List<SwapTokenEntity>?
            var delay = 0.seconds
            do {
                delay(delay)
                tokens = loadSwapTokens().getOrNull()
                delay = 3.seconds
            } while (tokens == null)
            allTokensUi = convertToUi(tokens)
            pendingSearchQuery?.let { query ->
                search(query)
                pendingSearchQuery = null
            } ?: submitDataToUi(allTokensUi)
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
            submitDataToUi(convertToUi(swapRepository.searchToken(query)))
        }
    }

    private fun submitDataToUi(items: List<Item>) {
        _uiItemsFlow.value = items
    }

    private suspend fun loadSwapTokens(): Result<List<SwapTokenEntity>> {
        return swapRepository.gteCachedOrLoadRemoteTokens()
    }

    private suspend fun convertToUi(tokens: List<SwapTokenEntity>): List<Item.Token> =
        withContext(Dispatchers.Default) {
            val hiddenBalance = settings.hiddenBalances
            tokens
                .filter { it.contractAddress != excludedAddress }
                .mapIndexed { index, swapTokenEntity ->
                    swapTokenEntity.toUi(
                        testnet = false,
                        hiddenBalance = hiddenBalance,
                        index = index,
                        size = tokens.size,
                        selected = swapTokenEntity.contractAddress == selectedAddress
                    )
                }
        }

    private fun SwapTokenEntity.toUi(
        testnet: Boolean,
        hiddenBalance: Boolean,
        index: Int,
        size: Int,
        selected: Boolean,
    ): Item.Token {
        return Item.Token(
            position = ListCell.getPosition(size, index),
            selected = selected,
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