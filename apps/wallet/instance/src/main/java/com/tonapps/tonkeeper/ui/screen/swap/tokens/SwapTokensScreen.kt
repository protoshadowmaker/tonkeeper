package com.tonapps.tonkeeper.ui.screen.swap.tokens

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.ui.screen.swap.tokens.list.Adapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.hideKeyboard
import uikit.extensions.pinToBottomInsets
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class SwapTokensScreen : BaseFragment(R.layout.fragment_swap_tokens), BaseFragment.BottomSheet {

    private val swapTokensViewModel: SwapTokensViewModel by viewModel()

    private val adapter = Adapter {

    }

    private lateinit var listView: SimpleRecyclerView

    private lateinit var closeButton: Button
    private lateinit var header: HeaderView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton = view.findViewById(R.id.closeButton)
        header = view.findViewById(R.id.header)
        listView = view.findViewById(R.id.list)

        listView.adapter = adapter
        header.doOnActionClick = {
            finish()
        }
        closeButton.setOnClickListener {
            finish()
        }
        closeButton.pinToBottomInsets()
        collectFlow(swapTokensViewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    companion object {

        fun newInstance(): SwapTokensScreen {
            return SwapTokensScreen()
        }
    }
}