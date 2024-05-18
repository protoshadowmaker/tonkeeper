package com.tonapps.tonkeeper.ui.screen.swap.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.data.swap.SwapRepository
import com.tonapps.wallet.data.swap.entity.SwapInfoEntity
import com.tonapps.wallet.data.swap.entity.SwapRequestEntity
import com.tonapps.wallet.data.swap.entity.SwapTokenEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger

class ConfirmSwapViewModel(
    private val swapRepository: SwapRepository
) : ViewModel() {

    private var swapRequest: SwapRequestEntity? = null
    private var srcToken: SwapTokenEntity? = null
    private var dstToken: SwapTokenEntity? = null

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
            srcToken = swapRepository.getCached(swapRequest.fromTokenAddress)
            dstToken = swapRepository.getCached(swapRequest.toTokenAddress)
            prepareAndSubmitDataToUi(
                buildConfirmSwapScreenState(
                    processState = ProcessState.IDLE,
                    confirmSwapInfoState = initialState
                )
            )
        }
    }


    private fun buildConfirmSwapScreenState(
        processState: ProcessState,
        confirmSwapInfoState: ConfirmSwapInfoState
    ): ConfirmSwapScreenState {
        val swapRequest = this.swapRequest ?: return ConfirmSwapScreenState.Error
        val srcToken = this.srcToken ?: return ConfirmSwapScreenState.Error
        val dstToken = this.dstToken ?: return ConfirmSwapScreenState.Error
        return ConfirmSwapScreenState.Data(
            processState = processState,
            srcConfirmTokenState = buildConfirmTokenState(srcToken, swapRequest.srcAmount),
            dstConfirmTokenState = buildConfirmTokenState(dstToken, swapRequest.dstAmount),
            confirmSwapInfo = confirmSwapInfoState
        )
    }

    private fun buildConfirmTokenState(token: SwapTokenEntity, amount: Long): ConfirmTokenState {
        return ConfirmTokenState(
            symbol = token.symbol,
            iconUri = token.iconUri,
            amountFormat = token.formatCoins(amount),
            fiatBalanceFormat = ""
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
        val state = state as? ConfirmSwapScreenState.Data ?: return

    }

    private fun buildJsSwapCommand(
        swapRequest: SwapRequestEntity,

    ): String {
        val userWallet = "UQA3P-be0OWgRKUKa3ZPmEbjSD7f8v9qKmrGC5Ep6OIA5BpM"
        val swapAmount = "10000000"
        val askWallet = "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"
        val minAskAmount = "1"

        return "swapTonToJetton('$userWallet', '$swapAmount', '$askWallet', '$minAskAmount')"
    }

    private fun prepareAndSubmitDataToUi(state: ConfirmSwapScreenState) {
        submitDataToUi(state)
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
        state: ConfirmSwapScreenState.Data,
        sideEffects: Set<SideEffect> = emptySet()
    ) {
        submitDataToUi(state.copy(sideEffects = sideEffects))
    }

    private fun submitDataToUi(state: ConfirmSwapScreenState) {
        _uiStateFlow.value = state
    }
}