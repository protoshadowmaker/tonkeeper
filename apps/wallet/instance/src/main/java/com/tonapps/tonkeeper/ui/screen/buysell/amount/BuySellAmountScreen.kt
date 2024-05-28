package com.tonapps.tonkeeper.ui.screen.buysell.amount

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.screen.buysell.amount.list.PaymentMethodAdapter
import com.tonapps.tonkeeper.ui.screen.buysell.amount.list.PaymentMethodItem
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.SimpleRecyclerView

class BuySellAmountScreen : BaseFragment(
    R.layout.fragment_buy_sell_amount
), BaseFragment.BottomSheet {

    private val viewModel: BuySellAmountViewModel by viewModel()

    private val paymentMethodsListView: SimpleRecyclerView by lazy { requireView().findViewById(R.id.list) }

    private val paymentMethodsAdapter = PaymentMethodAdapter { id ->
        onPaymentMethodsClicked(id)
    }

    private fun onPaymentMethodsClicked(id: String) {
        viewModel.onPaymentMethodClicked(id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentMethodsListView.adapter = paymentMethodsAdapter

        collectFlow(viewModel.uiStateFlow) {
            onStateChanged(it)
        }
    }

    private fun onStateChanged(state: BuySellAmountScreenState) {
        onPaymentMethodsChanged(state.paymentMethods)
    }

    private fun onPaymentMethodsChanged(methods: List<PaymentMethodItem>) {
        paymentMethodsAdapter.submitList(methods)
    }

    companion object {
        fun newInstance(): BuySellAmountScreen {
            return BuySellAmountScreen()
        }
    }
}