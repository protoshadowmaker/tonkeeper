function swapTonToJetton(userWalletAddress, offerAmount, askJettonAddress, minAskAmount) {
    window.tonToJetton(
        userWalletAddress,
        offerAmount,
        askJettonAddress,
        minAskAmount,
        (result) => {
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransaction',
                args: [result],
            }));
        },
        (error) => {
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransactionError',
                args: [error],
            }));
        },
    )
}

function swapJettonToTon(userWalletAddress, offerAmount, offerJettonAddress, minAskAmount) {
    window.jettonToTon(
        userWalletAddress,
        offerAmount,
        offerJettonAddress,
        minAskAmount,
        (result) => {
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransaction',
                args: [result],
            }));
        },
        (error) => {
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransactionError',
                args: [error],
            }));
        },
    )
}

function swapJettonToJetton(userWalletAddress, offerJettonAddress, offerAmount, askJettonAddress, minAskAmount) {
    window.jettonToJetton(
        userWalletAddress,
        offerJettonAddress,
        offerAmount,
        askJettonAddress,
        minAskAmount,
        (result) => {
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransaction',
                args: [result],
            }));
        },
        (error) => {
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransactionError',
                args: [error],
            }));
        },
    )
}