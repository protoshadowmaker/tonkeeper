package com.tonapps.tonkeeper.ui.screen.swap.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapInfoEntity
import com.tonapps.wallet.data.swap.entity.SwapRequestEntity
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.Duration.Companion.seconds

class ConfirmSwapViewModel(
    private val ratesRepository: RatesRepository,
    private val swapRepository: SwapRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private var swapRequest: SwapRequestEntity? = null
    private var srcToken: SwapTokenEntity? = null
    private var dstToken: SwapTokenEntity? = null

    private var swapRateJob: Job? = null

    private val _uiStateFlow: MutableStateFlow<ConfirmSwapScreenState> = MutableStateFlow(
        ConfirmSwapScreenState.Init
    )
    val uiStateFlow: StateFlow<ConfirmSwapScreenState> = _uiStateFlow.asStateFlow()
    val state: ConfirmSwapScreenState get() = uiStateFlow.value

    fun init(
        swapRequest: SwapRequestEntity,
        initialState: ConfirmSwapInfoState,
    ) {
        this.swapRequest = swapRequest
        viewModelScope.launch {
            srcToken = swapRepository.getCachedToken(swapRequest.fromTokenAddress)
            dstToken = swapRepository.getCachedToken(swapRequest.toTokenAddress)
            prepareAndSubmitDataToUi(
                buildConfirmSwapScreenState(
                    processState = ProcessState.IDLE,
                    confirmSwapInfoState = initialState
                )
            )
            loadSwapRate()
        }
    }


    private fun buildConfirmSwapScreenState(
        processState: ProcessState,
        confirmSwapInfoState: ConfirmSwapInfoState,
        walletCurrencyAmountFormat: CharSequence = ""
    ): ConfirmSwapScreenState {
        val swapRequest = this.swapRequest ?: return ConfirmSwapScreenState.Error
        val srcToken = this.srcToken ?: return ConfirmSwapScreenState.Error
        val dstToken = this.dstToken ?: return ConfirmSwapScreenState.Error
        return ConfirmSwapScreenState.Data(
            processState = processState,
            srcConfirmTokenState = buildConfirmTokenState(
                srcToken,
                swapRequest.srcAmount,
                walletCurrencyAmountFormat
            ),
            dstConfirmTokenState = buildConfirmTokenState(
                dstToken,
                swapRequest.dstAmount,
                walletCurrencyAmountFormat
            ),
            confirmSwapInfo = confirmSwapInfoState
        )
    }

    private fun buildConfirmTokenState(
        token: SwapTokenEntity,
        amount: Long,
        walletCurrencyAmountFormat: CharSequence
    ): ConfirmTokenState {
        return ConfirmTokenState(
            symbol = token.symbol,
            iconUri = token.iconUri,
            amountFormat = token.formatCoins(amount),
            walletCurrencyAmountFormat = walletCurrencyAmountFormat
        )
    }

    private fun buildConfirmSwapInfoState(
        offerToken: SwapTokenEntity,
        askToken: SwapTokenEntity,
        swapInfo: SwapInfoEntity
    ): ConfirmSwapInfoState {
        val minFee = CurrencyFormatter.format(value = 0.08f, decimals = 2)
        val maxFee = CurrencyFormatter.format(value = 0.25f, decimals = 2)
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

        return ConfirmSwapInfoState(
            priceImpact = priceImpact,
            minimumReceived = minReceived,
            providerFee = providerFee,
            blockchainFee = "$minFee - $maxFee TON",
            route = "${offerToken.symbol} » ${askToken.symbol}",
            provider = "STON.fi"
        )
    }

    fun onConfirmClicked() {
        val swapRequest = this.swapRequest ?: return
        val srcToken = this.srcToken ?: return
        val dstToken = this.dstToken ?: return
        val state = state as? ConfirmSwapScreenState.Data ?: return
        prepareAndSubmitDataToUi(
            state.copy(processState = ProcessState.PROCESSING),
            sideEffects = setOf(
                SideEffect.ExecuteCommand(
                    buildJsSwapCommand(
                        swapRequest = swapRequest,
                        srcToken = srcToken,
                        dstToken = dstToken
                    )
                )
            )
        )
    }

    fun onSignCanceled() {
        val state = state as? ConfirmSwapScreenState.Data ?: return
        prepareAndSubmitDataToUi(state.copy(processState = ProcessState.IDLE))
    }

    private fun buildJsSwapCommand(
        swapRequest: SwapRequestEntity,
        srcToken: SwapTokenEntity,
        dstToken: SwapTokenEntity,
    ): String {
        return when {
            srcToken.isTon -> buildTonToJettonSwapCommand(swapRequest)
            dstToken.isTon -> buildJettonToTonSwapCommand(swapRequest)
            else -> buildJettonToJettonSwapCommand(swapRequest)
        }
    }

    private fun buildTonToJettonSwapCommand(
        swapRequest: SwapRequestEntity
    ): String {
        return "swapTonToJetton('${swapRequest.walletAddress}', '${swapRequest.srcAmount}', '${swapRequest.toTokenAddress}', '${swapRequest.minAmount}')"
    }

    private fun buildJettonToTonSwapCommand(
        swapRequest: SwapRequestEntity
    ): String {
        return "swapJettonToTon('${swapRequest.walletAddress}', '${swapRequest.srcAmount}', '${swapRequest.fromTokenAddress}', '${swapRequest.minAmount}')"
    }

    private fun buildJettonToJettonSwapCommand(
        swapRequest: SwapRequestEntity
    ): String {
        return "swapJettonToJetton('${swapRequest.walletAddress}', '${swapRequest.fromTokenAddress}', '${swapRequest.srcAmount}', '${swapRequest.toTokenAddress}', '${swapRequest.minAmount}')"
    }

    private fun loadSwapRate() {
        swapRateJob?.cancel()
        val swapRequest = this.swapRequest ?: return

        swapRateJob = viewModelScope.launch {
            val swapInfoDeferrer = async { getMainSwapInfo(swapRequest) }
            val toTonSwapRateDeferrer = async { getToTonSwapRate(swapRequest) }
            val swapInfo = swapInfoDeferrer.await()
            val toTonSwapRate = toTonSwapRateDeferrer.await()
            if (swapInfo != null) {
                val currency = settings.currency
                val currencyValue = getCurrencyValue(currency, toTonSwapRate, swapInfo)
                onSwapInfoLoaded(swapRequest, swapInfo, currency, currencyValue)
            }
            delay(5.seconds)
            loadSwapRate()
        }
    }

    private suspend fun getCurrencyValue(
        currency: WalletCurrency,
        toTonSwapRate: Float,
        swapInfo: SwapInfoEntity
    ): Float = withContext(Dispatchers.IO) {
        val srcToken = this@ConfirmSwapViewModel.srcToken ?: return@withContext 0f
        if (toTonSwapRate > 0f) {
            val tonAmount =
                Coin.toCoins(swapInfo.offerValue, srcToken.decimals) * toTonSwapRate
            var rates = ratesRepository.getRates(currency, WalletCurrency.TON.code)
            if (rates.isEmpty) {
                ratesRepository.load(currency, WalletCurrency.TON.code)
                rates = ratesRepository.getRates(currency, WalletCurrency.TON.code)
            }
            tonAmount * rates.getRate(WalletCurrency.TON.code)
        } else {
            0f
        }
    }

    private suspend fun getMainSwapInfo(swapRequest: SwapRequestEntity): SwapInfoEntity? {
        return if (swapRequest.reverse) {
            swapRepository.getReverseSwapInfo(
                offerAddress = swapRequest.fromTokenAddress,
                askAddress = swapRequest.toTokenAddress,
                units = swapRequest.dstAmount,
                slippageTolerance = swapRequest.slippageTolerance
            )
        } else {
            swapRepository.getSwapInfo(
                offerAddress = swapRequest.fromTokenAddress,
                askAddress = swapRequest.toTokenAddress,
                units = swapRequest.srcAmount,
                slippageTolerance = swapRequest.slippageTolerance
            )
        }.getOrNull()
    }

    private suspend fun getToTonSwapRate(swapRequest: SwapRequestEntity): Float {
        val contractAddress = swapRequest.fromTokenAddress
        val amount = swapRequest.srcAmount
        return if (contractAddress == SwapTokenEntity.TON.contractAddress) {
            return 1f
        } else {
            swapRepository.getSwapInfo(
                offerAddress = contractAddress,
                askAddress = SwapTokenEntity.TON.contractAddress,
                units = amount,
                slippageTolerance = 0.001f //0.1%
            ).getOrNull()?.swapRate ?: 0f
        }
    }

    private fun onSwapInfoLoaded(
        swapRequest: SwapRequestEntity,
        swapInfo: SwapInfoEntity,
        currency: WalletCurrency,
        currencyValue: Float
    ) {
        this.swapRequest = swapRequest.copy(
            srcAmount = if (swapRequest.reverse) swapInfo.offerValue else swapRequest.srcAmount,
            dstAmount = if (swapRequest.reverse) swapRequest.dstAmount else swapInfo.askValue,
            minAmount = swapInfo.minimumReceived
        )
        val state = state as? ConfirmSwapScreenState.Data ?: return
        val offerToken = srcToken ?: return
        val askToken = dstToken ?: return
        val walletCurrencyAmountFormat = if (currencyValue > 0) {
            CurrencyFormatter.formatFiat(currency.code, currencyValue)
        } else {
            ""
        }
        prepareAndSubmitDataToUi(
            buildConfirmSwapScreenState(
                state.processState,
                buildConfirmSwapInfoState(
                    offerToken = offerToken,
                    askToken = askToken,
                    swapInfo = swapInfo
                ),
                walletCurrencyAmountFormat
            )
        )
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

    private fun prepareAndSubmitDataToUi(state: ConfirmSwapScreenState) {
        submitDataToUi(state)
    }

    private fun prepareAndSubmitDataToUi(
        state: ConfirmSwapScreenState.Data,
        sideEffects: Set<SideEffect> = emptySet()
    ) {
        submitDataToUi(state.copy(sideEffects = sideEffects))
    }

    private fun submitDataToUi(state: ConfirmSwapScreenState) {
        _uiStateFlow.value = state
    }
}