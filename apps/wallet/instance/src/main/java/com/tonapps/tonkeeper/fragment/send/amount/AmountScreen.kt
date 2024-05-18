package com.tonapps.tonkeeper.fragment.send.amount

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.localization.Localization
import com.tonapps.tonkeeperx.R
import com.tonapps.tonkeeper.fragment.send.pager.PagerScreen
import com.tonapps.tonkeeper.fragment.send.popup.SelectTokenPopup
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard

class AmountScreen: PagerScreen<AmountScreenState, AmountScreenEffect, AmountScreenFeature>(R.layout.fragment_send_amount) {

    companion object {
        fun newInstance() = AmountScreen()
    }

    override val feature: AmountScreenFeature by viewModel()

    private val selectTokenPopup: SelectTokenPopup by lazy {
        val popup = SelectTokenPopup(requireContext())
        popup.doOnSelectJetton = { token ->
            feature.selectToken(token)
            forceSetAmount(0f)
        }
        popup
    }

    private lateinit var tokenView: AppCompatTextView
    private lateinit var valueView: AmountInput
    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var availableView: AppCompatTextView
    private lateinit var maxButton: Button
    private lateinit var continueButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenView = view.findViewById(R.id.token)
        tokenView.setOnClickListener { selectTokenPopup.show(it) }

        valueView = view.findViewById(R.id.value)
        valueView.doAfterValueChanged = feature::setValue
        valueView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (continueButton.isEnabled) {
                    next()
                } else {
                    valueView.focusWithKeyboard()
                }
            }
            true
        }

        valueCurrencyView = view.findViewById(R.id.value_currency)

        rateView = view.findViewById(R.id.rate)
        availableView = view.findViewById(R.id.available)

        maxButton = view.findViewById(R.id.max)
        maxButton.setOnClickListener {
            if (maxButton.isActivated) {
                clearValue()
            } else {
                setMaxValue()
            }
        }

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener { next() }

        post {
            feature.setValue(0.0)
        }
    }

    fun forceSetJetton(address: String?) {
        address?.let { feature.selectToken(it) }
    }

    fun forceSetAmount(amount: Float) {
        val text = if (0f >= amount) {
            ""
        } else {
            amount.toString()
        }
        val editable = valueView.text ?: return
        editable.replace(0, editable.length, text)
    }

    private fun next() {
        sendFeature.setAmount(valueView.text.toString())
        sendFeature.nextPage()
    }

    private fun setMaxValue() {
        val maxValue = feature.currentBalance
        val text = valueView.text ?: return
        val format = maxValue.toString() // CurrencyFormatter.format(value = maxValue.toFloat(), decimals = feature.decimals)
        text.replace(0, text.length, format)
    }

    private fun clearValue() {
        forceSetAmount(0f)
    }

    override fun newUiState(state: AmountScreenState) {
        rateView.text = state.rate
        valueView.setDecimalCount(state.decimals)

        if (1 >= state.tokens.size) {
            tokenView.visibility = View.GONE
        } else {
            tokenView.visibility = View.VISIBLE
            tokenView.text = state.selectedTokenCode
            selectTokenPopup.tokens = state.tokens
            selectTokenPopup.selectedToken = state.selectedToken
        }

        sendFeature.setJetton(state.selectedToken)

        valueCurrencyView.text = state.selectedTokenCode

        if (state.insufficientBalance) {
            availableView.setText(Localization.insufficient_balance)
            availableView.setTextColor(requireContext().constantRedColor)
        } else if (state.remaining != "") {
            availableView.text = getString(Localization.remaining_balance, state.remaining)
            availableView.setTextColor(requireContext().textSecondaryColor)
        } else {
            availableView.text = getString(Localization.available_balance, state.available)
            availableView.setTextColor(requireContext().textSecondaryColor)
        }

        continueButton.isEnabled = state.canContinue

        if (state.maxActive) {
            maxButton.background.setTint(requireContext().buttonPrimaryBackgroundColor)
        } else {
            maxButton.background.setTint(requireContext().buttonSecondaryBackgroundColor)
        }

        maxButton.isActivated = state.maxActive
        sendFeature.setMax(state.maxActive)
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            sendFeature.setAmountHeader(requireContext())
            valueView.focusWithKeyboard()
        } else {
            valueView.hideKeyboard()
        }
    }
}