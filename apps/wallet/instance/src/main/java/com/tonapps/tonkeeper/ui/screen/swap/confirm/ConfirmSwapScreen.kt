package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import uikit.widget.webview.bridge.BridgeWebView

class ConfirmSwapScreen : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    private val rootViewModel: RootViewModel by activityViewModel()

    private val headerView: HeaderView by lazy { requireView().findViewById(R.id.header) }
    private val webView: BridgeWebView by lazy { requireView().findViewById(R.id.web) }
    private val action: Button by lazy { requireView().findViewById(R.id.action) }

    /*
    TON -> USDT
    "UQA3P-be0OWgRKUKa3ZPmEbjSD7f8v9qKmrGC5Ep6OIA5BpM",
    "10000000",
    "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs",

    USDT -> TON
    "UQA3P-be0OWgRKUKa3ZPmEbjSD7f8v9qKmrGC5Ep6OIA5BpM",
    "10000",
    "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView.startGravity()
        headerView.doOnActionClick = {
            finish()
        }
        webView.loadUrl("file:///android_asset/swap/index.html")
        webView.jsBridge = ConfirmSwapBridge(sendTransaction = ::sing)
        action.setOnClickListener {
            val userWallet = "UQA3P-be0OWgRKUKa3ZPmEbjSD7f8v9qKmrGC5Ep6OIA5BpM"
            val swapAmount = "10000000"
            val askWallet = "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"
            webView.executeJS("swapTonToJetton('$userWallet', '$swapAmount', '$askWallet')")
        }
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