package com.tonapps.tonkeeper.ui.screen.swap.search

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.swap.search.list.all.TokenAdapter
import com.tonapps.tonkeeper.ui.screen.swap.search.list.suggested.SuggestedAdapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyBottomNavigationInsets
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hideKeyboard
import uikit.extensions.visible
import uikit.extensions.withAnimation
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ModalHeader
import uikit.widget.SearchInput
import uikit.widget.SimpleRecyclerView

class SearchSwapTokenScreen : BaseFragment(R.layout.fragment_swap_search),
    BaseFragment.BottomSheet {

    private val request: String by lazy { arguments?.getString(REQUEST_KEY) ?: "" }
    private val selectedAddress: String by lazy { arguments?.getString(SELECTED_ADDRESS) ?: "" }
    private val excludedAddress: String by lazy { arguments?.getString(EXCLUDED_ADDRESS) ?: "" }

    private val viewModel: SearchSwapTokenViewModel by viewModel()

    private val tokenAdapter = TokenAdapter { contractAddress ->
        onTokenClicked(contractAddress)
    }
    private val suggestedAdapter = SuggestedAdapter { contractAddress ->
        onTokenClicked(contractAddress)
    }

    private val root: View by lazy { requireView().findViewById(R.id.swapTokensRoot) }
    private val closeButton: Button by lazy { requireView().findViewById(R.id.closeButton) }
    private val header: ModalHeader by lazy { requireView().findViewById(R.id.header) }
    private val searchInput: SearchInput by lazy { requireView().findViewById(R.id.search) }
    private val tokensListView: SimpleRecyclerView by lazy { requireView().findViewById(R.id.list) }
    private val suggestionList: SimpleRecyclerView by lazy { requireView().findViewById(R.id.suggestionList) }
    private val suggestedContainer: View by lazy { requireView().findViewById(R.id.suggestedContainer) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokensListView.adapter = tokenAdapter
        suggestionList.adapter = suggestedAdapter
        initDecorator()
        root.applyBottomNavigationInsets()

        header.onCloseClick = {
            finish()
        }
        closeButton.setOnClickListener {
            finish()
        }
        searchInput.doOnTextChanged = { viewModel.search(it.toString()) }

        viewModel.selectedAddress = selectedAddress
        viewModel.excludedAddress = excludedAddress
        collectFlow(viewModel.uiItemsFlow) { items ->
            tokenAdapter.submitList(items) {
                tokensListView.scrollToPosition(0)
            }
        }
        collectFlow(viewModel.uiSuggestedItemsFlow) { items ->
            suggestedAdapter.submitList(items)
            if (items.isNotEmpty()) {
                post {
                    withAnimation(duration = ANIMATION_DURATION) { suggestedContainer.visible() }
                }
            }
        }
        viewModel.loadData()
    }

    private fun initDecorator() {
        suggestionList.addItemDecoration(object : RecyclerView.ItemDecoration() {

            private val offsetHorizontal =
                requireContext().getDimensionPixelSize(uikit.R.dimen.offsetSmall)

            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val position = parent.getChildAdapterPosition(view)
                if (position == 0) {
                    return
                }
                outRect.left = offsetHorizontal
            }
        })
    }

    private fun onTokenClicked(contractAddress: String) {
        navigation?.setFragmentResult(request, Bundle().apply {
            putString(request, contractAddress)
        })
        finish()
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    companion object {

        private const val ANIMATION_DURATION = 180L
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