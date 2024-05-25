package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyBottomInsets
import uikit.extensions.collectFlow
import uikit.widget.InputView
import uikit.widget.ModalHeader
import uikit.widget.SwitchView
import kotlin.math.roundToInt

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    private val viewModel: SwapSettingsViewModel by viewModel()

    private val headerView: ModalHeader by lazy { requireView().findViewById(R.id.header) }
    private val saveButton: Button by lazy { requireView().findViewById(R.id.saveButton) }
    private val slippageInput: InputView by lazy { requireView().findViewById(R.id.slippageInput) }
    private val slippage1: Button by lazy { requireView().findViewById(R.id.slippage1) }
    private val slippage3: Button by lazy { requireView().findViewById(R.id.slippage3) }
    private val slippage5: Button by lazy { requireView().findViewById(R.id.slippage5) }
    private val expertMode: SwitchView by lazy { requireView().findViewById(R.id.expertMode) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView.onCloseClick = {
            finish()
        }
        slippageInput.inputType =
            EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        slippageInput.disableClearButton = true
        saveButton.applyBottomInsets()
        collectFlow(viewModel.uiStateFlow) {
            slippageInput.isEnabled = it.slippageExpert
            if (it.slippageExpert) {
                expertMode.checked = true
                slippageInput.text = it.slippageValue.toString()
                initListeners()
            } else {
                initListeners()
                when (it.slippageValue.roundToInt()) {
                    1 -> slippage1.callOnClick()
                    3 -> slippage3.callOnClick()
                    5 -> slippage5.callOnClick()
                    else -> slippage1.callOnClick()
                }
            }
        }
    }

    private fun initListeners() {
        saveButton.setOnClickListener {
            if (viewModel.onSaveClicked()) {
                finish()
            } else {
                slippageInput.error = true
            }
        }
        slippage1.setOnClickListener {
            onDefaultSlippageClicked {
                slippage1.isSelected = true
                viewModel.onSlippage1Clicked()
            }
        }
        slippage3.setOnClickListener {
            onDefaultSlippageClicked {
                slippage3.isSelected = true
                viewModel.onSlippage3Clicked()
            }
        }
        slippage5.setOnClickListener {
            onDefaultSlippageClicked {
                slippage5.isSelected = true
                viewModel.onSlippage5Clicked()
            }
        }
        slippageInput.doOnTextChange = {
            viewModel.onSlippageChanged(it.toFloatOrNull() ?: 0f)
        }
        expertMode.doCheckedChanged = ::onExpertModeChanged
    }

    private fun onDefaultSlippageClicked(callback: () -> Unit) {
        expertMode.checked = false
        unselect()
        callback()
    }

    private fun unselect() {
        slippage1.isSelected = false
        slippage3.isSelected = false
        slippage5.isSelected = false
    }

    private fun onExpertModeChanged(checked: Boolean) {
        slippageInput.isEnabled = checked
        if (checked) {
            unselect()
            slippageInput.focus()
            viewModel.onExpertModeSelected()
        } else {
            slippageInput.error = false
            slippageInput.hideKeyboard()
            slippageInput.text = ""
            slippage1.callOnClick()
        }
    }

    companion object {

        fun newInstance(): SwapSettingsScreen {
            return SwapSettingsScreen()
        }
    }
}