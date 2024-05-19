package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.pinToBottomInsets
import uikit.widget.InputView
import uikit.widget.ModalHeader
import uikit.widget.SwitchView

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
        saveButton.setOnClickListener {
            finish()
        }
        slippageInput.inputType =
            EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        slippageInput.disableClearButton = true
        slippage1.setOnClickListener {
            expertMode.checked = false
            viewModel.onSlippage1Clicked()
        }
        slippage3.setOnClickListener {
            expertMode.checked = false
            viewModel.onSlippage3Clicked()
        }
        slippage5.setOnClickListener {
            expertMode.checked = false
            viewModel.onSlippage5Clicked()
        }
        expertMode.doCheckedChanged = ::onExpertModeChanged
        saveButton.pinToBottomInsets()
        lifecycleScope.launch {
            viewModel.uiStateFlow.collect {
                if (it.slippageExpert) {
                    expertMode.checked = true
                    slippageInput.text = it.slippageValue.toString()
                }
            }
        }
    }

    private fun onExpertModeChanged(checked: Boolean) {
        slippageInput.isEnabled = checked
        if (checked) {
            slippageInput.focus()
        } else {
            slippageInput.hideKeyboard()
            slippageInput.text = ""
        }
    }

    companion object {

        fun newInstance(): SwapSettingsScreen {
            return SwapSettingsScreen()
        }
    }
}