package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.screen.swap.search.SwapSearchScreen
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation

class SwapAmountScreen : BaseFragment(R.layout.fragment_swap_amount), BaseFragment.BottomSheet {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(TOKEN_REQUEST_KEY) { bundle ->
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.source).setOnClickListener {
            navigation?.add(SwapSearchScreen.newInstance(TOKEN_REQUEST_KEY))
        }
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