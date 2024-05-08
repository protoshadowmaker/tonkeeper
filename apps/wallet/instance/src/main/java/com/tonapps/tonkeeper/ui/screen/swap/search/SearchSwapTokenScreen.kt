package com.tonapps.tonkeeper.ui.screen.swap.search

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.ui.screen.swap.search.list.Adapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyBottomNavigationInsets
import uikit.extensions.collectFlow
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.SearchInput
import uikit.widget.SimpleRecyclerView

class SearchSwapTokenScreen : BaseFragment(R.layout.fragment_swap_search),
    BaseFragment.BottomSheet {

    private val request: String by lazy { arguments?.getString(REQUEST_KEY) ?: "" }
    private val selectedAddress: String by lazy { arguments?.getString(SELECTED_ADDRESS) ?: "" }
    private val excludedAddress: String by lazy { arguments?.getString(EXCLUDED_ADDRESS) ?: "" }

    private val viewModel: SearchSwapTokenViewModel by viewModel()

    private val adapter = Adapter { contractAddress ->
        navigation?.setFragmentResult(request, Bundle().apply {
            putString(request, contractAddress)
        })
        finish()
    }

    private lateinit var listView: SimpleRecyclerView
    private lateinit var root: View
    private lateinit var closeButton: Button
    private lateinit var header: HeaderView
    private lateinit var searchInput: SearchInput

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton = view.findViewById(R.id.closeButton)
        header = view.findViewById(R.id.header)
        listView = view.findViewById(R.id.list)
        root = view.findViewById(R.id.swapTokensRoot)
        searchInput = view.findViewById(R.id.search)

        listView.adapter = adapter
        header.startGravity()
        root.applyBottomNavigationInsets()

        header.doOnActionClick = {
            finish()
        }
        closeButton.setOnClickListener {
            finish()
        }
        searchInput.doOnTextChanged = { viewModel.search(it.toString()) }

        viewModel.selectedAddress = selectedAddress
        viewModel.excludedAddress = excludedAddress
        collectFlow(viewModel.uiItemsFlow) { items ->
            adapter.submitList(items) {
                listView.scrollToPosition(0)
            }
        }
        viewModel.loadData()
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    companion object {

        private const val REQUEST_KEY = "request"
        private const val SELECTED_ADDRESS = "selected_address"
        private const val EXCLUDED_ADDRESS = "excluded_address"

        fun newInstance(
            request: String,
            selectedAddress: String? = null,
            excludedAddress: String? = null
        ): SearchSwapTokenScreen {
            return SearchSwapTokenScreen().apply {
                arguments = Bundle().apply {
                    putString(REQUEST_KEY, request)
                    putString(SELECTED_ADDRESS, selectedAddress)
                    putString(EXCLUDED_ADDRESS, excludedAddress)
                }
            }
        }
    }
}