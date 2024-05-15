package com.tonapps.tonkeeper.ui.screen.swap.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapInfoEntity
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import com.tonapps.wallet.localization.LocalizationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class SwapAmountViewModel(
    private val l10n: LocalizationRepository,
    private val settings: SettingsRepository,
    private val swapRepository: SwapRepository
) : ViewModel() {

    private var srcToken: SwapTokenEntity? = SwapTokenEntity.TON
    private var dstToken: SwapTokenEntity? = null
    private var baseToken: SwapTokenEntity = SwapTokenEntity.TON
    private var srcAmount: Long = 0
    private var dstAmount: Long = 0

    private var swapRateJob: Job? = null

    private val _uiStateFlow = MutableStateFlow(
        SwapAmountScreenState(
            srcTokenState = buildTokenState(srcToken),
            dstTokenState = buildTokenState(dstToken)
        )
    )
    val uiStateFlow: StateFlow<SwapAmountScreenState> = _uiStateFlow.asStateFlow()
    val state: SwapAmountScreenState get() = uiStateFlow.value

    fun onSourceChanged(contractAddress: String) {
        if (srcToken?.contractAddress == contractAddress) {
            return
        }
        srcToken = swapRepository.getCached(contractAddress)
        val state = this.state
        val tokenState = buildTokenState(srcToken)
            .copy(amountFormat = state.srcTokenState.amountFormat)
        submitDataToUi(
            state.copy(
                srcTokenState = tokenState,
                swapActionState = buildActionState(state.swapInfoState)
            )
        )
        loadSwapRate()
    }

    fun onSourceValueChanged(amount: String) {
        val srcToken = this.srcToken ?: return
        srcAmount = Coin.bigDecimal(amount, srcToken.decimals).toLong()
        baseToken = srcToken
        val state = this.state
        submitDataToUi(
            state.copy(
                srcTokenState = state.srcTokenState.copy(base = true),
                dstTokenState = state.dstTokenState.copy(base = false),
                swapActionState = buildActionState(state.swapInfoState)
            )
        )
        loadSwapRate()
    }

    fun onDestinationChanged(contractAddress: String) {
        if (dstToken?.contractAddress == contractAddress) {
            return
        }
        dstToken = swapRepository.getCached(contractAddress)
        val state = this.state
        val tokenState = buildTokenState(dstToken)
            .copy(amountFormat = state.dstTokenState.amountFormat)
        submitDataToUi(
            state.copy(
                dstTokenState = tokenState,
                swapActionState = buildActionState(state.swapInfoState)
            )
        )
        loadSwapRate()
    }

    fun onDestinationValueChanged(amount: String) {
        val dstToken = this.dstToken ?: return
        dstAmount = Coin.bigDecimal(amount, dstToken.decimals).toLong()
        baseToken = dstToken
        val state = this.state
        submitDataToUi(
            state.copy(
                srcTokenState = state.srcTokenState.copy(base = false),
                dstTokenState = state.dstTokenState.copy(base = true),
                swapActionState = buildActionState(state.swapInfoState)
            )
        )
        loadSwapRate()
    }

    private fun buildTokenState(token: SwapTokenEntity?): TokenState {
        return if (token == null) {
            TokenState(
                selected = false,
                symbol = l10n.getString(com.tonapps.wallet.localization.R.string.choose_token),
                amountFormat = ""
            )
        } else {
            TokenState(
                selected = true,
                displayName = token.displayName,
                symbol = token.symbol,
                address = token.contractAddress,
                iconUri = token.iconUri
            )
        }
    }

    private fun buildActionState(swapInfoState: SwapInfoState?): SwapActionState {
        return when {
            srcAmount == 0L && dstAmount == 0L -> SwapActionState.ENTER_AMOUNT
            srcToken == null || dstToken == null -> SwapActionState.CHOOSE_TOKEN
            swapInfoState == null -> SwapActionState.PROGRESS
            else -> SwapActionState.CONTINUE
        }
    }

    private fun loadSwapRate() {
        swapRateJob?.cancel()
        val offerToken = srcToken ?: return
        val askToken = dstToken ?: return
        val baseToken = this.baseToken
        val srcAmount = this.srcAmount
        val dstAmount = this.dstAmount

        submitDataToUi(state.copy(swapInfoState = state.swapInfoState?.copy(loading = true)))
        swapRateJob = viewModelScope.launch {
            val swapInfo = if (baseToken == offerToken) {
                swapRepository.getSwapInfo(
                    offerAddress = offerToken.contractAddress,
                    askAddress = askToken.contractAddress,
                    units = srcAmount,
                    slippageTolerance = 0.001f
                )
            } else {
                swapRepository.getReverseSwapInfo(
                    offerAddress = offerToken.contractAddress,
                    askAddress = askToken.contractAddress,
                    units = dstAmount,
                    slippageTolerance = 0.001f
                )
            }.getOrNull()
            if (swapInfo != null) {
                onSwapInfoLoaded(
                    offerToken = offerToken,
                    askToken = askToken,
                    baseToken = baseToken,
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
        baseToken: SwapTokenEntity,
        swapInfo: SwapInfoEntity
    ) {
        val state = this.state
        val minFee = CurrencyFormatter.format(value = 0.08f, decimals = 2)
        val maxFee = CurrencyFormatter.format(value = 0.25f, decimals = 2)
        val offer = CurrencyFormatter.format(
            currency = " " + offerToken.symbol, value = 1f, decimals = 0
        )
        val ask = CurrencyFormatter.format(
            currency = " " + askToken.symbol, value = swapInfo.swapRate, decimals = 0..4
        )
        val priceImpact = CurrencyFormatter.format(
            currency = " %", value = swapInfo.priceImpact, decimals = 0..3
        )
        val minReceived = CurrencyFormatter.format(
            currency = " " + askToken.symbol,
            value = Coin.toCoins(swapInfo.minimumReceived, askToken.decimals),
            decimals = 0..2
        )
        val providerFee = CurrencyFormatter.format(
            currency = " " + askToken.symbol,
            value = Coin.toCoins(swapInfo.liquidityFee, askToken.decimals),
            decimals = 0..askToken.decimals
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
        newState = if (baseToken == offerToken) {
            val formatAmount = Coin.toCoins(swapInfo.askValue, askToken.decimals).toString()
            newState.copy(dstTokenState = newState.dstTokenState.copy(amountFormat = formatAmount))
        } else {
            val formatAmount = Coin.toCoins(swapInfo.offerValue, offerToken.decimals).toString()
            newState.copy(srcTokenState = newState.srcTokenState.copy(amountFormat = formatAmount))
        }
        newState = newState.copy(swapActionState = buildActionState(newState.swapInfoState))
        submitDataToUi(newState)
    }

    private fun submitDataToUi(state: SwapAmountScreenState) {
        _uiStateFlow.value = state
    }
}