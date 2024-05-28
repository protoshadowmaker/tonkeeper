package com.tonapps.tonkeeper.ui.screen.swap.confirm

import com.tonapps.tonkeeper.sign.SignRequestEntity
import org.json.JSONArray
import uikit.widget.webview.bridge.JsBridge

class ConfirmSwapBridge(
    val stonApiKey: String,
    val sendTransaction: suspend (request: SignRequestEntity) -> String?,
    val sendTransactionCompleted: () -> Unit,
    val sendTransactionErrorCallback: (e: Throwable) -> Unit,
    val sendTransactionWebErrorCallback: (e: Throwable) -> Unit,
) : JsBridge("tonkeeperStonfi") {
    override val availableFunctions =
        arrayOf(FUNCTION_SEND_TRANSACTION, FUNCTION_SEND_TRANSACTION_ERROR)

    override fun jsInjection(): String {
        return "window.apiKey = \"$stonApiKey\";"
    }

    override suspend fun invokeFunction(name: String, args: JSONArray): Any? {
        if (name == FUNCTION_SEND_TRANSACTION) {
            val request = SignRequestEntity(args.getJSONObject(0))
            try {
                sendTransaction(request)
                sendTransactionCompleted()
            } catch (e: Throwable) {
                sendTransactionErrorCallback(e)
            }
        } else if (name == FUNCTION_SEND_TRANSACTION_ERROR) {
            sendTransactionWebErrorCallback(Exception("Unknown error"))
        }
        return null
    }

    companion object {
        private const val FUNCTION_SEND_TRANSACTION = "sendTransaction"
        private const val FUNCTION_SEND_TRANSACTION_ERROR = "sendTransactionError"
    }
}