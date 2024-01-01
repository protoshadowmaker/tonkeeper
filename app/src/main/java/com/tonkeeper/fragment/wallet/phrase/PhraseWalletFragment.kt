package com.tonkeeper.fragment.wallet.phrase

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeperx.R
import com.tonkeeper.App
import com.tonkeeper.event.WalletSettingsEvent
import com.tonkeeper.extensions.setRecoveryPhraseBackup
import core.EventBus
import uikit.widget.PhraseWords
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class PhraseWalletFragment: BaseFragment(R.layout.fragment_phrase_wallet), BaseFragment.SwipeBack {

    companion object {
        fun newInstance() = PhraseWalletFragment()
    }

    override var doOnDragging: ((Boolean) -> Unit)? = null
    override var doOnDraggingProgress: ((Float) -> Unit)? = null

    private var words = listOf<String>()
        set(value) {
            field = value
            wordsView.setWords(value)
        }

    private lateinit var headerView: HeaderView
    private lateinit var contentView: View
    private lateinit var wordsView: PhraseWords
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadWords()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        contentView = view.findViewById(R.id.content)
        wordsView = view.findViewById(R.id.words)

        nextButton = view.findViewById(R.id.next)
        nextButton.setOnClickListener {
            navigation?.add(PhraseWalletCheckFragment.newInstance())
        }
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        EventBus.post(WalletSettingsEvent)
    }

    private fun loadWords() {
        lifecycleScope.launch {
            val wallet = App.walletManager.getWalletInfo() ?: return@launch
            wallet.setRecoveryPhraseBackup(true)
            words = App.walletManager.getMnemonic(wallet.id)
        }
    }

}