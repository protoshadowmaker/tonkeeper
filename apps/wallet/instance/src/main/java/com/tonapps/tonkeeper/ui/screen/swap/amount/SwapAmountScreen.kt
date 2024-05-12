package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.confirm.ConfirmSwapScreen
import com.tonapps.tonkeeper.ui.screen.swap.search.SearchSwapTokenScreen
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeper.view.LineInfoSimpleView
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.gone
import uikit.extensions.invisible
import uikit.extensions.visible
import uikit.extensions.withAnimation
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.HeaderView

class SwapAmountScreen : BaseFragment(R.layout.fragment_swap_amount), BaseFragment.BottomSheet {

    private val viewModel: SwapAmountViewModel by viewModel()

    private val headerView: HeaderView by lazy { requireView().findViewById(R.id.header) }
    private val maxValueTextView: TextView by lazy { requireView().findViewById(R.id.amountMax) }
    private val srcBalanceTextView: TextView by lazy { requireView().findViewById(R.id.srcBalance) }
    private val srcTokenContainer: View by lazy { requireView().findViewById(R.id.srcTokenContainer) }
    private val srcTokenTextView: TextView by lazy { requireView().findViewById(R.id.srcToken) }
    private val srcTokenIcon: FrescoView by lazy { requireView().findViewById(R.id.srcTokenIcon) }
    private val srcValueInput: AmountInput by lazy { requireView().findViewById(R.id.srcValue) }
    private val dstBalanceTextView: TextView by lazy { requireView().findViewById(R.id.dstBalance) }
    private val dstTokenContainer: View by lazy { requireView().findViewById(R.id.dstTokenContainer) }
    private val dstTokenTextView: TextView by lazy { requireView().findViewById(R.id.dstToken) }
    private val dstTokenIcon: FrescoView by lazy { requireView().findViewById(R.id.dstTokenIcon) }
    private val dstValueInput: AmountInput by lazy { requireView().findViewById(R.id.dstValue) }
    private val actionButton: Button by lazy { requireView().findViewById(R.id.action) }

    private val swapInfoContainer: View by lazy { requireView().findViewById(R.id.swapInfoContainer) }
    private val rate: LineInfoSimpleView by lazy { requireView().findViewById(R.id.rate) }
    private val priceImpact: LineInfoSimpleView by lazy { requireView().findViewById(R.id.priceImpact) }
    private val minimumReceived: LineInfoSimpleView by lazy { requireView().findViewById(R.id.minimumReceived) }
    private val providerFee: LineInfoSimpleView by lazy { requireView().findViewById(R.id.providerFee) }
    private val blockchainFee: LineInfoSimpleView by lazy { requireView().findViewById(R.id.blockchainFee) }
    private val route: LineInfoSimpleView by lazy { requireView().findViewById(R.id.route) }
    private val provider: LineInfoSimpleView by lazy { requireView().findViewById(R.id.provider) }

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
        headerView.doOnActionClick = {
            finish()
        }
        headerView.doOnCloseClick = {
            navigation?.add(SwapSettingsScreen.newInstance())
        }
        srcValueInput.doOnTextChanged { _, _, _, _ ->
            viewModel.onSourceValueChanged(srcValueInput.getValue())
        }
        dstValueInput.doOnTextChanged { _, _, _, _ ->
            viewModel.onDestinationValueChanged(srcValueInput.getValue())
        }
        actionButton.setOnClickListener {
            navigation?.add(ConfirmSwapScreen.newInstance())
        }

        collectFlow(viewModel.uiStateFlow) { state ->
            onStateChanged(state)
        }
    }

    private fun onStateChanged(state: SwapAmountScreenState) {
        srcTokenTextView.text = state.srcTokenState.symbol
        if (state.srcTokenState.selected) {
            srcTokenIcon.setImageURI(state.srcTokenState.iconUri)
            srcTokenIcon.visible()
        } else {
            srcTokenIcon.clear(null)
            srcTokenIcon.gone()
        }
        if (state.srcTokenState.balanceFormat != null) {
            srcBalanceTextView.text = state.srcTokenState.balanceFormat
            srcBalanceTextView.visible()
            maxValueTextView.visible()
        } else {
            srcBalanceTextView.invisible()
            maxValueTextView.invisible()
        }
        if (state.srcTokenState.updateAmount) {
            srcValueInput.setText(state.srcTokenState.amountFormat)
        }

        dstTokenTextView.text = state.dstTokenState.symbol
        if (state.dstTokenState.selected) {
            dstTokenIcon.setImageURI(state.dstTokenState.iconUri)
            dstTokenIcon.visible()
        } else {
            dstTokenIcon.clear(null)
            dstTokenIcon.gone()
        }
        if (state.dstTokenState.balanceFormat != null) {
            dstBalanceTextView.text = state.dstTokenState.balanceFormat
            dstBalanceTextView.visible()
        } else {
            dstBalanceTextView.invisible()
        }
        if (state.dstTokenState.updateAmount) {
            dstValueInput.setText(state.dstTokenState.amountFormat)
        }

        srcTokenContainer.setOnClickListener {
            navigation?.add(
                SearchSwapTokenScreen.newInstance(
                    request = SRC_TOKEN_REQUEST_KEY,
                    selectedAddress = state.srcTokenState.address
                )
            )
        }
        dstTokenContainer.setOnClickListener {
            navigation?.add(
                SearchSwapTokenScreen.newInstance(
                    DST_TOKEN_REQUEST_KEY,
                    selectedAddress = state.dstTokenState.address,
                    excludedAddress = state.srcTokenState.address
                )
            )
        }
        onSwapInfoStateChanged(state.swapInfoState)
    }

    private fun onSwapInfoStateChanged(swapInfoState: SwapInfoState?) {
        if (swapInfoState == null) {
            post {
                withAnimation(duration = ANIMATION_DURATION) {
                    swapInfoContainer.gone()
                }
            }
            return
        }
        post {
            withAnimation(duration = ANIMATION_DURATION) {
                swapInfoContainer.visible()
            }
        }
    }

    private fun AmountInput.getValue(): Float {
        val text = Coin.prepareValue(text.toString())
        return text.toFloatOrNull() ?: 0f
    }

    companion object {

        private const val ANIMATION_DURATION = 1800L
        private const val SRC_TOKEN_REQUEST_KEY = "src_token_request"
        private const val DST_TOKEN_REQUEST_KEY = "dst_token_request"

        fun newInstance(): SwapAmountScreen {
            return SwapAmountScreen()
        }
    }
}