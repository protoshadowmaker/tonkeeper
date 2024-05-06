package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.widget.HeaderView

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    private val headerView: HeaderView by lazy { requireView().findViewById(R.id.header) }
    private val saveButton: Button by lazy { requireView().findViewById(R.id.saveButton) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView.startGravity()
        headerView.doOnActionClick = {
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