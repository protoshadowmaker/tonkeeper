function swapTonToJetton(userWalletAddress, offerAmount, askJettonAddress) {
    window.tonToJetton(
        userWalletAddress,
        offerAmount,
        askJettonAddress,
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

function swapJettonToTon(userWalletAddress, offerAmount, offerJettonAddress) {
    window.jettonToTon(
        userWalletAddress,
        offerAmount,
        offerJettonAddress,
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