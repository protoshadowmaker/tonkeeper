package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.swap.view.AutoSizeAmountInput
import com.tonapps.tonkeeper.view.LineInfoSimpleView
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.FooterDrawable
import uikit.extensions.applyBottomNavigationInsets
import uikit.extensions.collectFlow
import uikit.extensions.gone
import uikit.extensions.visible
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.LoaderView
import uikit.widget.ModalHeader
import uikit.widget.webview.bridge.BridgeWebView

class ConfirmSwapScreen : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    private val args: ConfirmSwapArgs by lazy { ConfirmSwapArgs(requireArguments()) }

    private val rootViewModel: RootViewModel by activityViewModel()
    private val viewModel: ConfirmSwapViewModel by viewModel()

    private val headerView: ModalHeader by lazy { requireView().findViewById(R.id.header) }

    private val srcBalanceTextView: TextView by lazy { requireView().findViewById(R.id.srcBalance) }
    private val srcTokenTextView: TextView by lazy { requireView().findViewById(R.id.srcToken) }
    private val srcTokenIcon: FrescoView by lazy { requireView().findViewById(R.id.srcTokenIcon) }
    private val srcValueTextView: AutoSizeAmountInput by lazy { requireView().findViewById(R.id.srcValue) }
    private val dstBalanceTextView: TextView by lazy { requireView().findViewById(R.id.dstBalance) }
    private val dstTokenTextView: TextView by lazy { requireView().findViewById(R.id.dstToken) }
    private val dstTokenIcon: FrescoView by lazy { requireView().findViewById(R.id.dstTokenIcon) }
    private val dstValueTextView: AutoSizeAmountInput by lazy { requireView().findViewById(R.id.dstValue) }

    private val priceImpact: LineInfoSimpleView by lazy { requireView().findViewById(R.id.priceImpact) }
    private val minimumReceived: LineInfoSimpleView by lazy { requireView().findViewById(R.id.minimumReceived) }
    private val providerFee: LineInfoSimpleView by lazy { requireView().findViewById(R.id.providerFee) }
    private val blockchainFee: LineInfoSimpleView by lazy { requireView().findViewById(R.id.blockchainFee) }
    private val route: LineInfoSimpleView by lazy { requireView().findViewById(R.id.route) }
    private val provider: LineInfoSimpleView by lazy { requireView().findViewById(R.id.provider) }

    private val webView: BridgeWebView by lazy { requireView().findViewById(R.id.web) }
    private val root: View by lazy { requireView().findViewById(R.id.confirmSwapRoot) }
    private val cancel: Button by lazy { requireView().findViewById(R.id.cancel) }
    private val confirm: Button by lazy { requireView().findViewById(R.id.confirm) }
    private val loader: LoaderView by lazy { requireView().findViewById(R.id.confirmLoader) }
    private val swapInfoContainer: View by lazy { requireView().findViewById(R.id.swapInfoContainer) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.applyBottomNavigationInsets()
        headerView.onCloseClick = ::finish
        cancel.setOnClickListener { finish() }
        webView.loadUrl("file:///android_asset/swap/index.html")
        webView.jsBridge = ConfirmSwapBridge(
            sendTransaction = ::sing,
            sendTransactionErrorCallback = ::onSignCancelled
        )
        confirm.setOnClickListener { viewModel.onConfirmClicked() }
        priceImpact.setOnClickListener {
            navigation?.toast(getString(com.tonapps.wallet.localization.R.string.price_impact_info))
        }
        minimumReceived.setOnClickListener {
            navigation?.toast(getString(com.tonapps.wallet.localization.R.string.minimum_received_info))
        }
        providerFee.setOnClickListener {
            navigation?.toast(getString(com.tonapps.wallet.localization.R.string.liquidity_provider_fee_info))
        }
        swapInfoContainer.background = FooterDrawable(requireContext()).apply { setDivider(true) }
        collectFlow(viewModel.uiStateFlow) { state ->
            when (state) {
                is ConfirmSwapScreenState.Init -> {}
                is ConfirmSwapScreenState.Error -> onInitializationError()
                is ConfirmSwapScreenState.Data -> onStateChanged(state)
            }
        }
        viewModel.init(
            args.swapRequest,
            ConfirmSwapInfoState(
                priceImpact = args.initialState.priceImpact,
                minimumReceived = args.initialState.minimumReceived,
                providerFee = args.initialState.providerFee,
                blockchainFee = args.initialState.blockchainFee,
                route = args.initialState.route,
                provider = args.initialState.provider
            )
        )
    }

    private fun onInitializationError() {
        navigation?.toast(com.tonapps.wallet.localization.R.string.error)
        finish()
    }

    private fun onStateChanged(state: ConfirmSwapScreenState.Data) {
        onProcessStateChanged(state.processState)
        onSrcConfirmTokenStateChanged(state.srcConfirmTokenState)
        onDstConfirmTokenStateChanged(state.dstConfirmTokenState)
        onConfirmSwapInfoStateChanged(state.confirmSwapInfo)
        onSideEffectsReceived(state.sideEffects)
    }

    private fun onSideEffectsReceived(sideEffects: Set<SideEffect>) {
        sideEffects.forEach {
            when (it) {
                is SideEffect.ExecuteCommand -> {
                    webView.executeJS(it.jsCommand)
                }
            }
        }
    }

    private fun onProcessStateChanged(state: ProcessState) {
        loader.gone()
        confirm.setText(com.tonapps.wallet.localization.R.string.confirm)
        confirm.isEnabled = false
        when (state) {
            ProcessState.IDLE -> {
                confirm.isEnabled = true
            }

            ProcessState.PROCESSING, ProcessState.SUCCESS -> {
                loader.visible()
                confirm.text = ""
            }

            ProcessState.ERROR -> {
                navigation?.toast(com.tonapps.wallet.localization.R.string.error)
            }

        }
    }

    private fun onSrcConfirmTokenStateChanged(state: ConfirmTokenState) {
        srcBalanceTextView.text = state.walletCurrencyAmountFormat
        srcTokenTextView.text = state.symbol
        srcTokenIcon.setImageURI(state.iconUri)
        srcValueTextView.setText(state.amountFormat)
        srcValueTextView.setDecimalCount(state.decimalCount)
    }

    private fun onDstConfirmTokenStateChanged(state: ConfirmTokenState) {
        dstBalanceTextView.text = state.walletCurrencyAmountFormat
        dstTokenTextView.text = state.symbol
        dstTokenIcon.setImageURI(state.iconUri)
        dstValueTextView.setText(state.amountFormat)
        dstValueTextView.setDecimalCount(state.decimalCount)
    }

    private fun onConfirmSwapInfoStateChanged(state: ConfirmSwapInfoState) {
        priceImpact.value = state.priceImpact
        minimumReceived.value = state.minimumReceived
        providerFee.value = state.providerFee
        blockchainFee.value = state.blockchainFee
        route.value = state.route
        provider.value = state.provider
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    private fun onSignCancelled(e: Throwable) {
        viewModel.onSignCanceled()
    }

    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(args: ConfirmSwapArgs): ConfirmSwapScreen {
            return ConfirmSwapScreen().apply {
                arguments = args.toBundle()
            }
        }
    }
}