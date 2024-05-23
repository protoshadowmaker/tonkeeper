package com.tonapps.tonkeeper.ui.screen.swap.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.HIDDEN_BALANCE
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapInfoEntity
import com.tonapps.wallet.data.swap.entity.SwapRequestEntity
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.data.token.TokenRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.LocalizationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.Duration.Companion.seconds

class SwapAmountViewModel(
    private val l10n: LocalizationRepository,
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository,
    private val settings: SettingsRepository,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private var activeWallet: WalletEntity? = null
    private var tokensMap: Map<String, AccountTokenEntity> = emptyMap()
    private var srcToken: SwapTokenEntity? = SwapTokenEntity.TON
    private var dstToken: SwapTokenEntity? = null
    private var srcAmount: Long = 0
    private var dstAmount: Long = 0
    private var minAmount: Long = 0
    private var reverse: Boolean = false
    private var slippageTolerance: Float = 0f

    private var swapRateJob: Job? = null

    private val _uiStateFlow = MutableStateFlow(
        SwapAmountScreenState(
            srcTokenState = buildTokenState(srcToken),
            dstTokenState = buildTokenState(dstToken)
        )
    )
    val uiStateFlow: StateFlow<SwapAmountScreenState> = _uiStateFlow.asStateFlow()
    val state: SwapAmountScreenState get() = uiStateFlow.value

    init {
        combine(
            walletRepository.activeWalletFlow,
            settings.currencyFlow
        ) { wallet, currency ->
            activeWallet = wallet
            tokensMap = tokenRepository
                .getLocal(currency, wallet.accountId, wallet.testnet)
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
            prepareAndSubmitDataToUi(
                state.copy(
                    srcTokenState = buildTokenState(srcToken),
                    dstTokenState = buildTokenState(dstToken)
                )
            )
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            settings.slippageValueFlow.collect {
                slippageTolerance = it / 100f //e.g. 0.1% is 0.001
                loadSwapRate()
            }
        }

        viewModelScope.launch {
            while (!swapRepository.hasCachedData()) {
                swapRepository.gteCachedOrLoadRemoteTokens()
                delay(5.seconds)
            }
        }
    }

    fun buildSwapRequest(): SwapRequestEntity? {
        val wallet = activeWallet ?: return null
        val from = srcToken?.contractAddress ?: return null
        val to = dstToken?.contractAddress ?: return null
        return SwapRequestEntity(
            walletAddress = wallet.address.toUserFriendly(testnet = wallet.testnet),
            fromTokenAddress = from,
            toTokenAddress = to,
            srcAmount = srcAmount,
            dstAmount = dstAmount,
            minAmount = minAmount,
            reverse = reverse,
            slippageTolerance = slippageTolerance
        )
    }

    fun onSourceChanged(contractAddress: String) {
        if (srcToken?.contractAddress == contractAddress) {
            return
        }
        srcToken = swapRepository.getCachedToken(contractAddress)
        if (dstToken?.contractAddress == contractAddress) {
            dstToken = null
        }
        val sideEffects = mutableSetOf(SideEffect.UPDATE_SRC_TOKEN)
        val srcTokenState: TokenState
        val dstTokenState: TokenState
        if (reverse) {
            srcAmount = 0
            srcTokenState = buildTokenState(srcToken)
            dstTokenState = buildTokenState(dstToken)
                .copy(amountFormat = state.dstTokenState.amountFormat)
        } else {
            srcAmount = 0
            dstAmount = 0
            srcTokenState = buildTokenState(srcToken)
            dstTokenState = buildTokenState(dstToken)
            sideEffects.add(SideEffect.UPDATE_DST_TOKEN)
        }
        val state = this.state
        prepareAndSubmitDataToUi(
            state.copy(
                srcTokenState = srcTokenState,
                dstTokenState = dstTokenState,
                swapInfoState = null,
                swapActionState = buildActionState(null)
            ),
            sideEffects
        )
        loadSwapRate()
    }

    fun onSourceValueChanged(amount: String) {
        val srcToken = this.srcToken ?: return
        srcAmount = Coin.bigDecimal(amount, srcToken.decimals).toLong()
        val state = this.state
        val swapInfoState: SwapInfoState?
        val dstTokenState: TokenState
        val sideEffects: Set<SideEffect>
        if (srcAmount == 0L) {
            dstAmount = 0
            swapInfoState = null
            dstTokenState = state.dstTokenState.copy(amountFormat = "")
            sideEffects = setOf(SideEffect.UPDATE_DST_TOKEN)
        } else {
            swapInfoState = state.swapInfoState
            dstTokenState = state.dstTokenState
            sideEffects = emptySet()
        }
        reverse = false
        val formatAmount = srcToken.formatCoins(srcAmount).toString()
        prepareAndSubmitDataToUi(
            state.copy(
                srcTokenState = state.srcTokenState.copy(amountFormat = formatAmount),
                dstTokenState = dstTokenState,
                swapInfoState = swapInfoState,
                swapActionState = buildActionState(state.swapInfoState)
            ),
            sideEffects = sideEffects
        )
        loadSwapRate()
    }

    fun onDestinationChanged(contractAddress: String) {
        if (dstToken?.contractAddress == contractAddress) {
            return
        }
        dstToken = swapRepository.getCachedToken(contractAddress)
        val sideEffects = mutableSetOf(SideEffect.UPDATE_DST_TOKEN)
        val srcTokenState: TokenState
        val dstTokenState: TokenState
        if (reverse) {
            srcAmount = 0
            dstAmount = 0
            srcTokenState = buildTokenState(srcToken)
            dstTokenState = buildTokenState(dstToken)
            sideEffects.add(SideEffect.UPDATE_SRC_TOKEN)
        } else {
            dstAmount = 0
            srcTokenState = buildTokenState(srcToken)
                .copy(amountFormat = state.srcTokenState.amountFormat)
            dstTokenState = buildTokenState(dstToken)
        }
        val state = this.state
        prepareAndSubmitDataToUi(
            state.copy(
                srcTokenState = srcTokenState,
                dstTokenState = dstTokenState,
                swapInfoState = null,
                swapActionState = buildActionState(null)
            ),
            sideEffects
        )
        loadSwapRate()
    }

    fun onDestinationValueChanged(amount: String) {
        val dstToken = this.dstToken ?: return
        dstAmount = Coin.bigDecimal(amount, dstToken.decimals).toLong()
        val state = this.state
        val swapInfoState: SwapInfoState?
        val srcTokenState: TokenState
        val sideEffects: Set<SideEffect>
        if (dstAmount == 0L) {
            srcAmount = 0
            swapInfoState = null
            srcTokenState = state.srcTokenState.copy(amountFormat = "")
            sideEffects = setOf(SideEffect.UPDATE_SRC_TOKEN)
        } else {
            swapInfoState = state.swapInfoState
            srcTokenState = state.srcTokenState
            sideEffects = emptySet()
        }
        reverse = true

        val formatAmount = dstToken.formatCoins(dstAmount)
        prepareAndSubmitDataToUi(
            state.copy(
                srcTokenState = srcTokenState,
                dstTokenState = state.dstTokenState.copy(amountFormat = formatAmount),
                swapInfoState = swapInfoState,
                swapActionState = buildActionState(state.swapInfoState)
            ),
            sideEffects = sideEffects
        )
        loadSwapRate()
    }

    fun onSwapTokensClicked() {
        val swapToken = srcToken
        srcToken = dstToken
        dstToken = swapToken
        val swapAmount = srcAmount
        srcAmount = dstAmount
        dstAmount = swapAmount
        val state = this.state
        reverse = !reverse
        prepareAndSubmitDataToUi(
            state.copy(
                srcTokenState = state.dstTokenState,
                dstTokenState = state.srcTokenState,
                swapInfoState = null,
                swapActionState = buildActionState(null)
            ),
            sideEffects = setOf(SideEffect.UPDATE_SRC_TOKEN, SideEffect.UPDATE_DST_TOKEN)
        )
        loadSwapRate()
    }

    fun onMaxClicked() {
        val srcToken = this.srcToken ?: return
        val srcAccount = tokensMap[srcToken.contractAddress] ?: return
        val amountFormat = srcToken.formatCoins(
            Coin.toNano(srcAccount.balance.value, srcAccount.decimals)
        )
        prepareAndSubmitDataToUi(
            state = state.copy(
                srcTokenState = state.srcTokenState.copy(amountFormat = amountFormat)
            ), sideEffects = setOf(SideEffect.UPDATE_SRC_TOKEN)
        )
        onSourceValueChanged(amountFormat.toString())
    }

    private fun buildTokenState(token: SwapTokenEntity?): TokenState {
        return if (token == null) {
            TokenState(
                selected = false,
                symbol = l10n.getString(com.tonapps.wallet.localization.R.string.choose_token),
                amountFormat = ""
            )
        } else {
            val accountToken = tokensMap[token.contractAddress]
            val balanceFormat = if (accountToken != null) {
                val formattedValue = if (settings.hiddenBalances) {
                    HIDDEN_BALANCE
                } else {
                    token.flexFormatCoins(
                        Coin.toNano(
                            accountToken.balance.value,
                            accountToken.decimals
                        )
                    )
                }
                l10n.getString(com.tonapps.wallet.localization.R.string.balance, formattedValue)
            } else {
                null
            }
            TokenState(
                selected = true,
                symbol = token.symbol,
                address = token.contractAddress,
                iconUri = token.iconUri,
                balanceFormat = balanceFormat
            )
        }
    }

    private fun buildActionState(swapInfoState: SwapInfoState?): SwapActionState {
        val srcAccountToken: AccountTokenEntity? = srcToken?.let { tokensMap[it.contractAddress] }
        val srcAccountBalance = if (srcAccountToken == null) {
            0
        } else {
            Coin.toNano(srcAccountToken.balance.value, srcAccountToken.decimals)
        }
        return when {
            srcAmount == 0L && dstAmount == 0L -> SwapActionState.ENTER_AMOUNT
            srcAmount > srcAccountBalance -> SwapActionState.NOT_ENOUGH_TOKENS
            srcToken == null || dstToken == null -> SwapActionState.CHOOSE_TOKEN
            swapInfoState == null -> SwapActionState.PROGRESS
            else -> SwapActionState.CONTINUE
        }
    }

    private fun loadSwapRate() {
        swapRateJob?.cancel()
        val offerToken = srcToken ?: return
        val askToken = dstToken ?: return
        val srcAmount = this.srcAmount
        val dstAmount = this.dstAmount
        val slippageTolerance = this.slippageTolerance
        if (slippageTolerance <= 0f) {
            return
        }

        prepareAndSubmitDataToUi(state.copy(swapInfoState = state.swapInfoState?.copy(loading = true)))
        swapRateJob = viewModelScope.launch {
            val swapInfo = if (reverse) {
                swapRepository.getReverseSwapInfo(
                    offerAddress = offerToken.contractAddress,
                    askAddress = askToken.contractAddress,
                    units = dstAmount,
                    slippageTolerance = slippageTolerance
                )
            } else {
                swapRepository.getSwapInfo(
                    offerAddress = offerToken.contractAddress,
                    askAddress = askToken.contractAddress,
                    units = srcAmount,
                    slippageTolerance = slippageTolerance
                )
            }.getOrNull()
            if (swapInfo != null) {
                onSwapInfoLoaded(
                    offerToken = offerToken,
                    askToken = askToken,
                    reverse = reverse,
                    swapInfo = swapInfo
                )
            }
            delay(5.seconds)
            loadSwapRate()
        }
    }

    private fun onSwapInfoLoaded(
        offerToken: SwapTokenEntity,
        askToken: SwapTokenEntity,
        reverse: Boolean,
        swapInfo: SwapInfoEntity
    ) {
        minAmount = swapInfo.minimumReceived
        val state = this.state
        val minFee = CurrencyFormatter.format(value = 0.08f, decimals = 2)
        val maxFee = CurrencyFormatter.format(value = 0.25f, decimals = 2)
        val offer = CurrencyFormatter.format(
            currency = " " + offerToken.symbol, value = 1f, decimals = 0
        )
        val ask = CurrencyFormatter.format(
            currency = " " + askToken.symbol,
            value = swapInfo.swapRate,
            decimals = askToken.getFlexDecimals(Coin.toNano(swapInfo.swapRate, askToken.decimals))
        )
        val priceImpact = CurrencyFormatter.format(
            currency = " %", value = swapInfo.priceImpact, decimals = 0..3
        )
        val minReceived = CurrencyFormatter.format(
            currency = " " + askToken.symbol,
            value = Coin.toCoins(swapInfo.minimumReceived, askToken.decimals),
            decimals = askToken.getFlexDecimals(swapInfo.minimumReceived)
        )
        val providerFee = CurrencyFormatter.format(
            currency = " " + askToken.symbol,
            value = Coin.toCoins(swapInfo.liquidityFee, askToken.decimals),
            decimals = askToken.getFlexDecimals(swapInfo.liquidityFee)
        )

        var newState = state.copy(
            swapInfoState = SwapInfoState(
                loading = false,
                swapRate = "$offer ≈ $ask",
                priceImpact = priceImpact,
                minimumReceived = minReceived,
                providerFee = providerFee,
                blockchainFee = "$minFee - $maxFee TON",
                route = "${offerToken.symbol} » ${askToken.symbol}",
                provider = "STON.fi"
            )
        )
        val sideEffects: Set<SideEffect>
        newState = if (reverse) {
            srcAmount = swapInfo.offerValue
            sideEffects = setOf(SideEffect.UPDATE_SRC_TOKEN)
            val formatAmount = offerToken.formatCoins(swapInfo.offerValue)
            newState.copy(srcTokenState = newState.srcTokenState.copy(amountFormat = formatAmount))
        } else {
            dstAmount = swapInfo.askValue
            sideEffects = setOf(SideEffect.UPDATE_DST_TOKEN)
            val formatAmount = askToken.formatCoins(swapInfo.askValue)
            newState.copy(dstTokenState = newState.dstTokenState.copy(amountFormat = formatAmount))
        }
        newState = newState.copy(swapActionState = buildActionState(newState.swapInfoState))

        prepareAndSubmitDataToUi(newState, sideEffects)
    }

    private fun SwapTokenEntity.formatCoins(value: Long): CharSequence {
        return CurrencyFormatter.format(
            value = BigDecimal.valueOf(value).divide(BigDecimal.TEN.pow(decimals)),
            decimals = 0..decimals,
            group = false
        )
    }

    private fun SwapTokenEntity.flexFormatCoins(value: Long): CharSequence {
        return CurrencyFormatter.format(
            value = BigDecimal.valueOf(value).divide(BigDecimal.TEN.pow(decimals)),
            decimals = getFlexDecimals(value),
            group = false
        )
    }

    private fun SwapTokenEntity.getFlexDecimals(value: Long): IntRange {
        val oneCoinInNano = BigInteger.TEN.pow(decimals).toLong()
        return if (value > oneCoinInNano) {
            2..2
        } else {
            0..decimals
        }
    }

    private fun prepareAndSubmitDataToUi(
        state: SwapAmountScreenState,
        sideEffects: Set<SideEffect> = emptySet()
    ) {
        submitDataToUi(state.copy(sideEffects = sideEffects))
    }

    private fun submitDataToUi(state: SwapAmountScreenState) {
        _uiStateFlow.value = state
    }
}