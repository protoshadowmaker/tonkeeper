package com.tonapps.tonkeeper.ui.screen.swap.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.ui.screen.swap.search.list.all.TokenItem
import com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.SuggestedTokenItem
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
    private var allTokensUi: List<TokenItem.Token> = emptyList()
    private val _uiItemsFlow = MutableStateFlow<List<TokenItem>>(
        listOf(
            TokenItem.TokenSkeleton(ListCell.Position.FIRST),
            TokenItem.TokenSkeleton(ListCell.Position.MIDDLE),
            TokenItem.TokenSkeleton(ListCell.Position.LAST)
        )
    )
    val uiItemsFlow = _uiItemsFlow.asStateFlow()

    private val _uiSuggestedItemsFlow = MutableStateFlow<List<SuggestedTokenItem>>(emptyList())
    val uiSuggestedItemsFlow = _uiSuggestedItemsFlow.asStateFlow()

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
            allTokensUi = convertToTokenUi(tokens)
            pendingSearchQuery?.let { query ->
                search(query)
                pendingSearchQuery = null
            } ?: submitTokenItemsToUi(allTokensUi)

            submitSuggestedTokensToUi(convertToSuggestedTokenUi(tokens.subList(0, 10)))
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        if (allTokensUi.isEmpty()) {
            pendingSearchQuery = query
            return
        }
        if (query.isBlank()) {
            submitTokenItemsToUi(allTokensUi)
            return
        }
        searchJob = viewModelScope.launch {
            submitTokenItemsToUi(convertToTokenUi(swapRepository.searchToken(query)))
        }
    }

    private fun submitTokenItemsToUi(tokenItems: List<TokenItem>) {
        _uiItemsFlow.value = tokenItems
    }

    private fun submitSuggestedTokensToUi(tokenItems: List<SuggestedTokenItem>) {
        _uiSuggestedItemsFlow.value = tokenItems
    }

    private suspend fun loadSwapTokens(): Result<List<SwapTokenEntity>> {
        return swapRepository.gteCachedOrLoadRemoteTokens()
    }

    private suspend fun convertToTokenUi(tokens: List<SwapTokenEntity>): List<TokenItem.Token> =
        withContext(Dispatchers.Default) {
            val hiddenBalance = settings.hiddenBalances
            tokens
                .filter { it.contractAddress != excludedAddress }
                .mapIndexed { index, swapTokenEntity ->
                    swapTokenEntity.toTokenUi(
                        testnet = false,
                        hiddenBalance = hiddenBalance,
                        index = index,
                        size = tokens.size,
                        selected = swapTokenEntity.contractAddress == selectedAddress
                    )
                }
        }

    private suspend fun convertToSuggestedTokenUi(tokens: List<SwapTokenEntity>): List<SuggestedTokenItem.Token> =
        withContext(Dispatchers.Default) {
            tokens
                .filter { it.contractAddress != excludedAddress }
                .map { swapTokenEntity ->
                    swapTokenEntity.toSuggestedTokenUi(
                        selected = swapTokenEntity.contractAddress == selectedAddress
                    )
                }
        }

    private fun SwapTokenEntity.toTokenUi(
        testnet: Boolean,
        hiddenBalance: Boolean,
        index: Int,
        size: Int,
        selected: Boolean,
    ): TokenItem.Token {
        return TokenItem.Token(
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

    private fun SwapTokenEntity.toSuggestedTokenUi(
        selected: Boolean,
    ): SuggestedTokenItem.Token {
        return SuggestedTokenItem.Token(
            selected = selected,
            iconUri = iconUri,
            contractAddress = contractAddress,
            symbol = symbol,
        )
    }
}