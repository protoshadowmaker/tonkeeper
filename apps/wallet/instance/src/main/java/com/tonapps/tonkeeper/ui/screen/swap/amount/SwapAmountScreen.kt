package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.search.SearchSwapTokenScreen
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.gone
import uikit.extensions.invisible
import uikit.extensions.visible
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(SRC_TOKEN_REQUEST_KEY) { bundle ->
        }
        navigation?.setFragmentResultListener(DST_TOKEN_REQUEST_KEY) { bundle ->
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

        collectFlow(viewModel.uiStateFlow) { state ->
            onStateChanged(state)
        }
    }

    private fun onStateChanged(state: SwapAmountScreenState) {
        srcTokenTextView.text = state.srcTokenState.displayName
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

        dstTokenTextView.text = state.dstTokenState.displayName
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
                    selectedSymbol = state.srcTokenState.symbol
                )
            )
        }
        dstTokenContainer.setOnClickListener {
            navigation?.add(
                SearchSwapTokenScreen.newInstance(
                    DST_TOKEN_REQUEST_KEY,
                    selectedSymbol = state.dstTokenState.symbol,
                    excludedSymbol = state.srcTokenState.symbol
                )
            )
        }
    }

    private fun selectToken() {

    }

    companion object {

        private const val SRC_TOKEN_REQUEST_KEY = "src_token_request"
        private const val DST_TOKEN_REQUEST_KEY = "dst_token_request"

        fun newInstance(): SwapAmountScreen {
            return SwapAmountScreen()
        }
    }
}