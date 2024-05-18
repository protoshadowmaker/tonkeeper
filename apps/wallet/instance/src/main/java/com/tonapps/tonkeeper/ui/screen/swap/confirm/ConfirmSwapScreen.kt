package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.applyBottomNavigationInsets
import uikit.widget.ModalHeader
import uikit.widget.webview.bridge.BridgeWebView

class ConfirmSwapScreen : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    private val rootViewModel: RootViewModel by activityViewModel()

    private val headerView: ModalHeader by lazy { requireView().findViewById(R.id.header) }
    private val webView: BridgeWebView by lazy { requireView().findViewById(R.id.web) }
    private val root: View by lazy { requireView().findViewById(R.id.confirmSwapRoot) }
    private val cancel: Button by lazy { requireView().findViewById(R.id.cancel) }
    private val confirm: Button by lazy { requireView().findViewById(R.id.confirm) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.applyBottomNavigationInsets()
        headerView.onCloseClick = {
            finish()
        }
        webView.loadUrl("file:///android_asset/swap/index.html")
        webView.jsBridge = ConfirmSwapBridge(sendTransaction = ::sing)
        confirm.setOnClickListener {
            val userWallet = "UQA3P-be0OWgRKUKa3ZPmEbjSD7f8v9qKmrGC5Ep6OIA5BpM"
            val swapAmount = "10000000"
            val askWallet = "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"
            val minAskAmount = "1"
            webView.executeJS("swapTonToJetton('$userWallet', '$swapAmount', '$askWallet', '$minAskAmount')")
        }
        cancel.setOnClickListener { finish() }
    }

    private suspend fun sing(
        request: SignRequestEntity
    ): String {
        return rootViewModel.requestSign(requireContext(), request)
    }

    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
    }

    companion object {

        fun newInstance(): ConfirmSwapScreen {
            return ConfirmSwapScreen()
        }
    }
}