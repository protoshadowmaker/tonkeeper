package com.tonapps.tonkeeper.ui.screen.swap.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.screen.swap.search.list.all.TokenItem
import com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.SuggestedTokenItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.core.GenericCacheSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.parcelable.ParcelableString
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

class SearchSwapTokenViewModel(
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository,
    private val settings: SettingsRepository,
    private val swapRepository: SwapRepository,
    private val cacheSource: GenericCacheSource,
) : ViewModel() {

    private val globalScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var searchJob: Job? = null
    private var pendingSearchQuery: String? = null
    private var accountTokens: Map<String, AccountTokenEntity> = emptyMap()
    private var allTokensUi: List<TokenItem.Token> = emptyList()
    private var suggestedTokens: List<SwapTokenEntity> = emptyList()

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
            accountTokens = walletRepository.activeWalletFlow.firstOrNull()?.let { wallet ->
                tokenRepository.getLocal(settings.currency, wallet.accountId, wallet.testnet)
                    .associateBy {
                        if (it.address == "TON") {
                            SwapTokenEntity.TON.contractAddress
                        } else {
                            it.address.toUserFriendly(
                                wallet = false,
                                testnet = wallet.testnet
                            )
                        }
                    }
            } ?: emptyMap()

            var tokens: List<SwapTokenEntity>?
            var delay = 0.seconds
            do {
                delay(delay)
                tokens = loadSwapTokens().getOrNull()
                delay = 3.seconds
            } while (tokens == null)
            allTokensUi = convertToTokenUi(tokens, accountTokens, settings.currency)
            pendingSearchQuery?.let { query ->
                search(query)
                pendingSearchQuery = null
            } ?: submitTokenItemsToUi(allTokensUi)

            suggestedTokens = getSuggestedTokens().mapNotNull { swapRepository.getCachedToken(it) }
            submitSuggestedTokensToUi(convertToSuggestedTokenUi(suggestedTokens))
        }
    }

    private suspend fun getSuggestedTokens(): List<String> = withContext(Dispatchers.IO) {
        cacheSource.get(CACHE_NAME_SUGGESTED) { parcel ->
            ParcelableString(parcel.readString())
        }.mapNotNull { it.value }
    }

    private suspend fun saveSuggestedTokens(addresses: List<String>) = withContext(Dispatchers.IO) {
        cacheSource.set(
            CACHE_NAME_SUGGESTED,
            addresses.map { ParcelableString(it) }
        )
    }

    fun onTokeSelected(address: String) {
        globalScope.launch {
            val newSuggestedList = suggestedTokens
                .filter { it.contractAddress != address }
                .map { it.contractAddress }
                .toMutableList().apply {
                    add(0, address)
                }
            saveSuggestedTokens(newSuggestedList.subList(0, min(newSuggestedList.size, 10)))
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
            submitTokenItemsToUi(
                convertToTokenUi(
                    swapRepository.searchToken(query),
                    accountTokens,
                    settings.currency
                )
            )
        }
    }

    private fun submitTokenItemsToUi(tokenItems: List<TokenItem>) {
        _uiItemsFlow.value = tokenItems
    }

    private fun submitSuggestedTokensToUi(tokenItems: List<SuggestedTokenItem>) {
        _uiSuggestedItemsFlow.value = tokenItems
    }

    private suspend fun loadSwapTokens(): Result<List<SwapTokenEntity>> {
        return swapRepository.getCachedOrLoadRemoteTokens()
    }

    private suspend fun convertToTokenUi(
        tokens: List<SwapTokenEntity>,
        accountTokens: Map<String, AccountTokenEntity>,
        currency: WalletCurrency
    ): List<TokenItem.Token> =
        withContext(Dispatchers.Default) {
            val hiddenBalance = settings.hiddenBalances
            tokens
                .filter { it.contractAddress != excludedAddress }
                .mapIndexed { index, swapTokenEntity ->
                    val tokenBalance = accountTokens[swapTokenEntity.contractAddress]
                    val balance: Double
                    val balanceFormat: CharSequence
                    val fiat: Float
                    val fiatFormat: CharSequence
                    if (tokenBalance != null) {
                        balance = tokenBalance.balance.value
                        fiat = tokenBalance.fiat
                        balanceFormat = CurrencyFormatter.format(value = balance)
                        fiatFormat = CurrencyFormatter.formatFiat(currency.code, fiat)
                    } else {
                        balance = 0.0
                        fiat = 0f
                        balanceFormat = "0"
                        fiatFormat = "0"
                    }
                    swapTokenEntity.toTokenUi(
                        testnet = false,
                        hiddenBalance = hiddenBalance,
                        index = index,
                        size = tokens.size,
                        selected = swapTokenEntity.contractAddress == selectedAddress,
                        balance = balance,
                        balanceFormat = balanceFormat,
                        fiat = fiat,
                        fiatFormat = fiatFormat
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
        balance: Double,
        balanceFormat: CharSequence,
        fiat: Float,
        fiatFormat: CharSequence,
    ): TokenItem.Token {
        return TokenItem.Token(
            position = ListCell.getPosition(size, index),
            selected = selected,
            iconUri = iconUri,
            contractAddress = contractAddress,
            symbol = symbol,
            name = displayName,
            balance = balance,
            balanceFormat = balanceFormat,
            fiat = fiat,
            fiatFormat = fiatFormat,
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

    companion object {
        private const val CACHE_NAME_SUGGESTED = "swap_tokens_suggested"
    }
}