package com.tonapps.wallet.api.core

import fi.ston.apis.DexApi
import okhttp3.OkHttpClient

class StonAPI(
    basePath: String,
    okHttpClient: OkHttpClient
) {

    val dex: DexApi by lazy { DexApi(basePath, okHttpClient) }
}