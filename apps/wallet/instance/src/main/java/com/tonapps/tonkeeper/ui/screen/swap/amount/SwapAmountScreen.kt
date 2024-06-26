package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.swap.confirm.ConfirmSwapArgs
import com.tonapps.tonkeeper.ui.screen.swap.confirm.ConfirmSwapScreen
import com.tonapps.tonkeeper.ui.screen.swap.search.SearchSwapTokenScreen
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeper.ui.screen.swap.view.AutoSizeAmountInput
import com.tonapps.tonkeeper.view.LineInfoSimpleView
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.FooterDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.applyBottomNavigationInsets
import uikit.extensions.collectFlow
import uikit.extensions.gone
import uikit.extensions.invisible
import uikit.extensions.isGone
import uikit.extensions.isVisible
import uikit.extensions.visible
import uikit.extensions.withAnimation
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.HeaderView

class SwapAmountScreen : BaseFragment(R.layout.fragment_swap_amount), BaseFragment.BottomSheet {

    private val args: SwapAmountArgs by lazy { SwapAmountArgs(requireArguments()) }

    private val viewModel: SwapAmountViewModel by viewModel()

    private val scrollContainer: View by lazy { requireView().findViewById(R.id.scrollContainer) }

    private val headerView: HeaderView by lazy { requireView().findViewById(R.id.header) }
    private val maxValueTextView: TextView by lazy { requireView().findViewById(R.id.amountMax) }
    private val srcBalanceTextView: TextView by lazy { requireView().findViewById(R.id.srcBalance) }
    private val srcTokenContainer: View by lazy { requireView().findViewById(R.id.srcTokenContainer) }
    private val srcTokenTextView: TextView by lazy { requireView().findViewById(R.id.srcToken) }
    private val srcTokenIcon: FrescoView by lazy { requireView().findViewById(R.id.srcTokenIcon) }
    private val srcValueInput: AutoSizeAmountInput by lazy { requireView().findViewById(R.id.srcValue) }
    private val dstBalanceTextView: TextView by lazy { requireView().findViewById(R.id.dstBalance) }
    private val dstTokenContainer: View by lazy { requireView().findViewById(R.id.dstTokenContainer) }
    private val dstTokenTextView: TextView by lazy { requireView().findViewById(R.id.dstToken) }
    private val dstTokenIcon: FrescoView by lazy { requireView().findViewById(R.id.dstTokenIcon) }
    private val dstValueInput: AutoSizeAmountInput by lazy { requireView().findViewById(R.id.dstValue) }

    private val swapInfoContainer: View by lazy { requireView().findViewById(R.id.swapInfoContainer) }
    private val rate: LineInfoSimpleView by lazy { requireView().findViewById(R.id.rate) }
    private val priceImpact: LineInfoSimpleView by lazy { requireView().findViewById(R.id.priceImpact) }
    private val minimumReceived: LineInfoSimpleView by lazy { requireView().findViewById(R.id.minimumReceived) }
    private val providerFee: LineInfoSimpleView by lazy { requireView().findViewById(R.id.providerFee) }
    private val blockchainFee: LineInfoSimpleView by lazy { requireView().findViewById(R.id.blockchainFee) }
    private val route: LineInfoSimpleView by lazy { requireView().findViewById(R.id.route) }
    private val provider: LineInfoSimpleView by lazy { requireView().findViewById(R.id.provider) }

    private val swapTokens: View by lazy { requireView().findViewById(R.id.swapTokens) }
    private val action: Button by lazy { requireView().findViewById(R.id.action) }
    private val continueAction: Button by lazy { requireView().findViewById(R.id.continueAction) }
    private val actionLoader: View by lazy { requireView().findViewById(R.id.actionLoader) }

    private val rateDrawable: Drawable by lazy {
        LayerDrawable(
            arrayOf(
                HeaderDrawable(requireContext()).apply { setDivider(true) },
                FooterDrawable(requireContext()).apply { setDivider(true) }
            )
        )
    }

    private val srcTextChangeListener: ToggleableTextWatcher =
        ToggleableTextWatcher(onTextChanged = { _, _, _, _ ->
            viewModel.onSourceValueChanged(srcValueInput.getValue())
        })

    private val dstTextChangeListener: ToggleableTextWatcher =
        ToggleableTextWatcher(onTextChanged = { _, _, _, _ ->
            viewModel.onDestinationValueChanged(dstValueInput.getValue())
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(SRC_TOKEN_REQUEST_KEY) { bundle ->
            bundle.getString(SRC_TOKEN_REQUEST_KEY)?.let { symbol ->
                viewModel.onSourceChanged(symbol)
            }

        }
        navigation?.setFragmentResultListener(DST_TOKEN_REQUEST_KEY) { bundle ->
            bundle.getString(DST_TOKEN_REQUEST_KEY)?.let { symbol ->
                viewModel.onDestinationChanged(symbol)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rate.foreground = rateDrawable
        headerView.doOnActionClick = {
            finish()
        }
        headerView.doOnCloseClick = {
            navigation?.add(SwapSettingsScreen.newInstance())
        }
        srcValueInput.addTextChangedListener(srcTextChangeListener)
        dstValueInput.addTextChangedListener(dstTextChangeListener)
        continueAction.setOnClickListener {
            onContinueClicked()
        }
        swapTokens.setOnClickListener {
            viewModel.onSwapTokensClicked()
        }
        maxValueTextView.setOnClickListener {
            viewModel.onMaxClicked()
        }
        priceImpact.setOnClickListener {
            navigation?.toast(getString(com.tonapps.wallet.localization.R.string.price_impact_info))
        }
        minimumReceived.setOnClickListener {
            navigation?.toast(getString(com.tonapps.wallet.localization.R.string.minimum_received_info))
        }
        providerFee.setOnClickListener {
            navigation?.toast(getString(com.tonapps.wallet.localization.R.string.liquidity_provider_fee_info))
        }
        scrollContainer.applyBottomNavigationInsets()

        collectFlow(viewModel.uiStateFlow) { state ->
            onStateChanged(state)
        }

        if (savedInstanceState == null) {
            viewModel.init(srcToken = args.fromToken, dstToken = args.toToken)
        }
    }

    private fun onContinueClicked() {
        val swapRequest = viewModel.buildSwapRequest() ?: return
        val swapInfoState = viewModel.state.swapInfoState ?: return
        navigation?.add(
            ConfirmSwapScreen.newInstance(
                ConfirmSwapArgs(
                    swapRequest = swapRequest,
                    initialState = ConfirmSwapArgs.InitialState(
                        swapRate = swapInfoState.swapRate,
                        priceImpact = swapInfoState.priceImpact,
                        minimumReceived = swapInfoState.minimumReceived,
                        providerFee = swapInfoState.providerFee,
                        blockchainFee = swapInfoState.blockchainFee,
                        route = swapInfoState.route,
                        provider = swapInfoState.provider
                    )
                )
            )
        )
    }

    private fun onStateChanged(state: SwapAmountScreenState) {
        onSrcTokenStateChanged(
            state.srcTokenState,
            state.sideEffects.contains(SideEffect.UPDATE_SRC_TOKEN)
        )
        onDstTokenStateChanged(
            state.dstTokenState,
            state.sideEffects.contains(SideEffect.UPDATE_DST_TOKEN)
        )
        onSwapInfoStateChanged(state.swapInfoState)
        onActionStateChanged(state.swapActionState, state.srcTokenState.symbol)

        srcTokenContainer.setOnClickListener {
            navigation?.add(
                SearchSwapTokenScreen.newInstance(
                    request = SRC_TOKEN_REQUEST_KEY,
                    selectedAddress = state.srcTokenState.address.toString()
                )
            )
        }
        dstTokenContainer.setOnClickListener {
            navigation?.add(
                SearchSwapTokenScreen.newInstance(
                    DST_TOKEN_REQUEST_KEY,
                    selectedAddress = state.dstTokenState.address.toString(),
                    excludedAddress = state.srcTokenState.address.toString()
                )
            )
        }
    }

    private fun onSrcTokenStateChanged(state: TokenState, updateValue: Boolean) {
        srcTokenTextView.text = state.symbol
        srcValueInput.isEnabled = state.selected
        srcValueInput.setDecimalCount(state.decimalCount)
        if (state.selected) {
            srcTokenIcon.setImageURI(state.iconUri)
            srcTokenIcon.visible()
        } else {
            srcTokenIcon.clear(null)
            srcTokenIcon.gone()
        }
        if (state.balanceFormat != null) {
            srcBalanceTextView.text = state.balanceFormat
            srcBalanceTextView.visible()
            maxValueTextView.visible()
        } else {
            srcBalanceTextView.invisible()
            maxValueTextView.invisible()
        }
        if (updateValue) {
            srcTextChangeListener.ignore {
                srcValueInput.setTextKeepState(state.amountFormat)
            }
        }
    }

    private fun onDstTokenStateChanged(state: TokenState, updateValue: Boolean) {
        dstTokenTextView.text = state.symbol
        dstValueInput.isEnabled = state.selected
        dstValueInput.setDecimalCount(state.decimalCount)
        if (state.selected) {
            dstTokenIcon.setImageURI(state.iconUri)
            dstTokenIcon.visible()
        } else {
            dstTokenIcon.clear(null)
            dstTokenIcon.gone()
        }
        if (state.balanceFormat != null) {
            dstBalanceTextView.text = state.balanceFormat
            dstBalanceTextView.visible()
        } else {
            dstBalanceTextView.invisible()
        }
        if (updateValue) {
            dstTextChangeListener.ignore {
                dstValueInput.setTextKeepState(state.amountFormat)
            }
        }
    }

    private fun onSwapInfoStateChanged(state: SwapInfoState?) {
        if (state == null) {
            if (!swapInfoContainer.isGone()) {
                post { withAnimation(duration = ANIMATION_DURATION) { swapInfoContainer.gone() } }
            }
            return
        }
        if (state.loading) {
            rate.setLoading()
        } else {
            rate.setDefault()
        }
        rate.title = state.swapRate
        priceImpact.value = state.priceImpact
        minimumReceived.value = state.minimumReceived
        providerFee.value = state.providerFee
        blockchainFee.value = state.blockchainFee
        route.value = state.route
        provider.value = state.provider
        if (!swapInfoContainer.isVisible()) {
            post { withAnimation(duration = ANIMATION_DURATION) { swapInfoContainer.visible() } }
        }
    }

    private fun onActionStateChanged(state: SwapActionState, srcTokenSymbol: CharSequence?) {
        action.gone()
        actionLoader.gone()
        continueAction.gone()
        when (state) {
            SwapActionState.ENTER_AMOUNT -> {
                action.visible()
                action.setText(com.tonapps.wallet.localization.R.string.enter_amount)
            }

            SwapActionState.CHOOSE_TOKEN -> {
                action.visible()
                action.setText(com.tonapps.wallet.localization.R.string.choose_token)
            }

            SwapActionState.PROGRESS -> actionLoader.visible()

            SwapActionState.CONTINUE -> continueAction.visible()

            SwapActionState.NOT_ENOUGH_TOKENS -> {
                action.visible()
                action.text = getString(
                    com.tonapps.wallet.localization.R.string.insufficient_balance_buy,
                    srcTokenSymbol
                )
            }
        }
    }

    private fun AutoSizeAmountInput.getValue(): String {
        return Coin.prepareValue(text.toString())
    }

    companion object {

        private const val ANIMATION_DURATION = 180L
        private const val SRC_TOKEN_REQUEST_KEY = "src_token_request"
        private const val DST_TOKEN_REQUEST_KEY = "dst_token_request"

        fun newInstance(
            address: String,
            fromToken: String,
            toToken: String? = null
        ): SwapAmountScreen {
            return SwapAmountScreen().apply {
                arguments = SwapAmountArgs(fromToken = fromToken, toToken = toToken).toBundle()
            }
        }
    }
}