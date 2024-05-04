package com.tonapps.tonkeeper.ui.screen.main

import androidx.lifecycle.ViewModel
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.WalletType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import uikit.extensions.collectFlow

class MainViewModel(
    private val walletRepository: WalletRepository,
    private val api: API,
): ViewModel() {

    private val _browserTabEnabledFlow = MutableStateFlow<Boolean?>(null)
    val browserTabEnabled = _browserTabEnabledFlow.asStateFlow().filterNotNull()

    private val _childBottomScrolled = MutableEffectFlow<Boolean>()
    val childBottomScrolled = _childBottomScrolled.asSharedFlow()

    init {
        collectFlow(walletRepository.activeWalletFlow) { wallet ->
            _browserTabEnabledFlow.value = !api.config.flags.disableDApps && (wallet.type == WalletType.Default || wallet.type == WalletType.Signer)
        }
    }

    fun setBottomScrolled(value: Boolean) {
        _childBottomScrolled.tryEmit(value)
    }
}