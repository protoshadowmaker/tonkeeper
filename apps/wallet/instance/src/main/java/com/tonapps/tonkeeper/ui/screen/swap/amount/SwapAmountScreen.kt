package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.search.SwapSearchScreen
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation

class SwapAmountScreen : BaseFragment(R.layout.fragment_swap_amount), BaseFragment.BottomSheet {

    private val viewModel: SwapAmountViewModel by viewModel()

    private val maxValueTextView: TextView by lazy { requireView().findViewById(R.id.amountMax) }
    private val srcBalanceTextView: TextView by lazy { requireView().findViewById(R.id.srcBalance) }
    private val srcTokenTextView: TextView by lazy { requireView().findViewById(R.id.srcToken) }
    private val srcValueInput: AmountInput by lazy { requireView().findViewById(R.id.srcValue) }
    private val dstBalanceTextView: TextView by lazy { requireView().findViewById(R.id.dstBalance) }
    private val dstTokenTextView: TextView by lazy { requireView().findViewById(R.id.dstToken) }
    private val dstValueInput: AmountInput by lazy { requireView().findViewById(R.id.dstValue) }
    private val actionButton: Button by lazy { requireView().findViewById(R.id.action) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(TOKEN_REQUEST_KEY) { bundle ->
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        srcTokenTextView.setOnClickListener {
            navigation?.add(SwapSearchScreen.newInstance(TOKEN_REQUEST_KEY))
        }
        dstTokenTextView.setOnClickListener {
            navigation?.add(SwapSearchScreen.newInstance(TOKEN_REQUEST_KEY))
        }

        collectFlow(viewModel.uiStateFlow) { state ->
            onStateChanged(state)
        }
    }

    private fun onStateChanged(state: SwapAmountScreenState) {
        srcTokenTextView.text = state.srcTokenName
    }

    private fun selectToken() {

    }

    companion object {

        private const val TOKEN_REQUEST_KEY = "token_request"

        fun newInstance(): SwapAmountScreen {
            return SwapAmountScreen()
        }
    }
}