function swapTonToJetton() {
    window.tonToJetton(
        "UQA3P-be0OWgRKUKa3ZPmEbjSD7f8v9qKmrGC5Ep6OIA5BpM", 
        "10000000",
        "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs",
        (result, error) => {
            var resultParagraph = document.getElementById("resultText");
            var text = JSON.stringify(result);
            resultParagraph.textContent = text;
            ReactNativeWebView.postMessage(JSON.stringify({
                type: 'invokeRnFunc',
                invocationId: 'id',
                name: 'sendTransaction',
                args: [result],
            }));
        }
    )
}