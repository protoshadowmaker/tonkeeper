package com.tonapps.tonkeeper.ui.screen.buysell.amount

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.ui.screen.buysell.amount.list.PaymentMethodItem
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.localization.LocalizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BuySellAmountViewModel(
    private val l10n: LocalizationRepository
) : ViewModel() {

    private var paymentMethod: PaymentMethod = PaymentMethod.CREDIT

    private val _uiStateFlow = MutableStateFlow(BuySellAmountScreenState())
    val uiStateFlow: StateFlow<BuySellAmountScreenState> = _uiStateFlow.asStateFlow()

    val state: BuySellAmountScreenState get() = uiStateFlow.value

    init {
        refreshPaymentMethods()
    }

    fun onPaymentMethodClicked(id: String) {
        if (paymentMethod.name == id) {
            return
        }
        paymentMethod = PaymentMethod.valueOf(id)
        refreshPaymentMethods()
    }

    private fun refreshPaymentMethods() {
        submitDataToUi(state.copy(paymentMethods = PaymentMethod.entries.mapIndexed { index, method ->
            PaymentMethodItem.PaymentMethod(
                position = ListCell.getPosition(PaymentMethod.entries.size, index),
                id = method.name,
                selected = method == paymentMethod,
                iconResId = method.iconResId,
                title = l10n.getString(method.nameResId)
            )
        }))
    }

    private fun submitDataToUi(state: BuySellAmountScreenState) {
        _uiStateFlow.value = state
    }

    private enum class PaymentMethod(
        @StringRes
        val nameResId: Int,
        @DrawableRes
        val iconResId: Int
    ) {
        CREDIT(
            com.tonapps.wallet.localization.R.string.payment_method_credit,
            R.drawable.ic_payment_master_visa
        ),
        CREDIT_RU(
            com.tonapps.wallet.localization.R.string.payment_method_credit_rub,
            R.drawable.ic_payment_mir
        ),
        CRYPTO(
            com.tonapps.wallet.localization.R.string.payment_method_crypto,
            R.drawable.ic_payment_crypto
        )
    }
}