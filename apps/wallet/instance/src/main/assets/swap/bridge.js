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
        },
    )
}