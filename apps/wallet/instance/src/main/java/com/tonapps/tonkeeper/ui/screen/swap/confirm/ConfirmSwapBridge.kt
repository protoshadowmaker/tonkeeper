package com.tonapps.tonkeeper.ui.screen.swap.confirm

import android.util.Log
import com.tonapps.tonkeeper.sign.SignRequestEntity
import org.json.JSONArray
import uikit.widget.webview.bridge.JsBridge

class ConfirmSwapBridge(
    val sendTransaction: suspend (request: SignRequestEntity) -> String?
) : JsBridge("tonkeeperStonfi") {
    override val availableFunctions = arrayOf(FUNCTION_SEND_TRANSACTION)
    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        if (name == FUNCTION_SEND_TRANSACTION) {
            val request = SignRequestEntity(args.getJSONObject(0))
            Log.d("CONFIRM_SWAP", sendTransaction(request) ?: "No result")
        }
        return null
    }

    companion object {
        private const val FUNCTION_SEND_TRANSACTION = "sendTransaction"
    }
}