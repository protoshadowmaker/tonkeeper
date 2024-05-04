package com.tonapps.tonkeeper

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.tonapps.tonkeeper.core.fiat.Fiat
import com.tonapps.wallet.api.apiModule
import com.tonapps.wallet.data.account.accountModule
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.browser.browserModule
import com.tonapps.wallet.data.collectibles.collectiblesModule
import com.tonapps.wallet.data.events.eventsModule
import com.tonapps.wallet.data.push.pushModule
import com.tonapps.wallet.data.rates.ratesModule
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.swap.swapModule
import com.tonapps.wallet.data.token.tokenModule
import com.tonapps.wallet.data.tonconnect.tonConnectModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import uikit.widget.webview.bridge.BridgeWebView

class App : Application(), CameraXConfig.Provider {

    companion object {

        @Deprecated("Use injection")
        lateinit var walletManager: WalletManager

        lateinit var fiat: Fiat

        @Deprecated("Use injection")
        lateinit var settings: SettingsRepository

        lateinit var db: AppDatabase
        lateinit var instance: App
        lateinit var bridgeWebView: BridgeWebView
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        db = AppDatabase.getInstance(this)
        walletManager = WalletManager(this)
        fiat = Fiat(this)
        settings = SettingsRepository(this)
        bridgeWebView = BridgeWebView(this)

        startKoin {
            androidContext(this@App)
            modules(
                koinModel,
                browserModule,
                pushModule,
                tonConnectModule,
                apiModule,
                accountModule,
                ratesModule,
                swapModule,
                tokenModule,
                eventsModule,
                collectiblesModule
            )
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        initFresco()
    }

    private fun initFresco() {
        val configBuilder = ImagePipelineConfig.newBuilder(this)
        configBuilder.experiment().setNativeCodeDisabled(true)
        configBuilder.setDownsampleEnabled(false)

        Fresco.initialize(this, configBuilder.build())
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder
            .fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }

    fun isOriginalAppInstalled(): Boolean {
        val pm = packageManager
        return try {
            pm.getPackageInfo("com.ton_keeper", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}