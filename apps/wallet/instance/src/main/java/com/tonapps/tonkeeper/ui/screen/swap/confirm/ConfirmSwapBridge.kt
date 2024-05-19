package com.tonapps.tonkeeper.ui.screen.swap.confirm

import com.tonapps.tonkeeper.sign.SignRequestEntity
import org.json.JSONArray
import uikit.widget.webview.bridge.JsBridge

class ConfirmSwapBridge(
    val sendTransaction: suspend (request: SignRequestEntity) -> String?,
    val sendTransactionErrorCallback: (e: Throwable) -> Unit
) : JsBridge("tonkeeperStonfi") {
    override val availableFunctions = arrayOf(FUNCTION_SEND_TRANSACTION)

    override fun jsInjection(): String {
        //TODO mark to readme
        return "window.apiKey = \"7af7a1b88b608b3a33b8088692e90b540ef18edbdb71c145c51b88ba4c38e2b9\";"
    }

    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        if (name == FUNCTION_SEND_TRANSACTION) {
            val request = SignRequestEntity(args.getJSONObject(0))
            try {
                sendTransaction(request)
            } catch (e: Throwable) {
                sendTransactionErrorCallback(e)
            }
        }
        return null
    }

    companion object {
        private const val FUNCTION_SEND_TRANSACTION = "sendTransaction"
    }
}