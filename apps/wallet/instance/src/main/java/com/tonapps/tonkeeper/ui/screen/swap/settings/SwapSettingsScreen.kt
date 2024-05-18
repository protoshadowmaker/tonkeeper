package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.widget.ModalHeader

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    private val headerView: ModalHeader by lazy { requireView().findViewById(R.id.header) }
    private val saveButton: Button by lazy { requireView().findViewById(R.id.saveButton) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView.onCloseClick = {
            finish()
        }
        saveButton.setOnClickListener {
            finish()
        }
    }

    companion object {

        fun newInstance(): SwapSettingsScreen {
            return SwapSettingsScreen()
        }
    }
}