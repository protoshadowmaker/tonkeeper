package com.tonapps.wallet.data.push

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.extensions.locale
import com.tonapps.network.getBitmap
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.events.EventsRepository
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.push.entities.WalletPushEntity
import com.tonapps.wallet.data.push.source.LocalDataSource
import com.tonapps.wallet.data.push.source.RemoteDataSource
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.tonconnect.TonConnectRepository
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PushManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val eventsRepository: EventsRepository,
    private val tonConnectRepository: TonConnectRepository,
    private val api: API
) {

    private val notificationChannelDefault = context.getString(R.string.default_push_channel_id)
    private val notificationChannelApp = context.getString(R.string.app_push_channel_id)

    private val recentlyReceiveAppPushIds = ArrayDeque<String>(10)
    private val helper = NotificationHelper(context)
    private val remoteDataSource = RemoteDataSource(api)
    private val localDataSource = LocalDataSource(context)

    private val _dAppPushFlow = MutableStateFlow<List<AppPushEntity>?>(null)
    val dAppPushFlow = _dAppPushFlow.asStateFlow()

    init {
        combine(
            settingsRepository.firebaseTokenFlow,
            walletRepository.walletsFlow,
            ::subscribe
        ).flowOn(Dispatchers.IO).launchIn(scope)

        walletRepository.activeWalletFlow.onEach {
            _dAppPushFlow.value = getRemoteDAppEvents(it)
        }.launchIn(scope)
    }

    suspend fun getLocalDAppEvents(wallet: WalletEntity): List<AppPushEntity> = withContext(Dispatchers.IO) {
        localDataSource.get(wallet.id)
    }

    suspend fun getRemoteDAppEvents(wallet: WalletEntity): List<AppPushEntity> = withContext(Dispatchers.IO) {
        val token = walletRepository.getTonProofToken(wallet.id) ?: return@withContext emptyList()
        val items = remoteDataSource.getEvents(token, wallet.accountId)
        localDataSource.save(wallet.id, items)
        return@withContext items
    }

    fun handleAppPush(push: AppPushEntity) {
        if (alreadyReceiveAppPush(push.from)) {
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                val wallet = walletRepository.getWallet(push.account) ?: throw IllegalStateException("Wallet not found")
                val app = tonConnectRepository.getApp(push.dappUrl, wallet) ?: throw IllegalStateException("App not found")
                localDataSource.insert(wallet.id, push)
                val largeIcon = api.defaultHttpClient.getBitmap(app.manifest.iconUrl)
                displayAppPush(app, push, wallet, largeIcon)

                if (walletRepository.activeWalletFlow.firstOrNull() == wallet) {
                    val old = _dAppPushFlow.value ?: emptyList()
                    _dAppPushFlow.value = old + push
                }
            } catch (ignored: Throwable) {}
        }
    }

    private suspend fun displayAppPush(
        app: DAppEntity,
        push: AppPushEntity,
        wallet: WalletEntity,
        largeIcon: Bitmap?
    ) = withContext(Dispatchers.Main) {
        val notificationId = helper.findIdOrNew {
            it.extras?.getCharSequence(Notification.EXTRA_TEXT) == push.message
        }

        val pending = helper.createPendingIntent(context, push.intent)
        val channel = helper.getChannel(notificationChannelApp)
        val builder = helper.baseBuilder(context, channel.id)
        builder.setContentTitle(app.manifest.name)
        builder.setContentText(push.message)
        builder.setColor(wallet.label.color)
        builder.setLargeIcon(largeIcon)
        builder.setContentIntent(pending)
        helper.display(notificationId, builder.build())
    }

    fun handleWalletPush(push: WalletPushEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val wallet = walletRepository.getWallet(push.account) ?: throw IllegalStateException("Wallet not found")
                displayWalletPush(push, wallet)
            } catch (ignored: Throwable) {}
        }
    }

    private suspend fun displayWalletPush(
        push: WalletPushEntity,
        wallet: WalletEntity,
    ) = withContext(Dispatchers.Main) {
        val notificationId = helper.findIdOrNew {
            it.extras?.getCharSequence(Notification.EXTRA_TEXT) == push.notificationBody
        }

        val pending = helper.createPendingIntent(context, push.intent)
        val channel = helper.getChannel(notificationChannelApp)
        val builder = helper.baseBuilder(context, channel.id)
        builder.setContentTitle(wallet.label.title)
        builder.setContentText(push.notificationBody)
        builder.setColor(wallet.label.color)
        builder.setContentIntent(pending)
        helper.display(notificationId, builder.build())
    }

    private fun alreadyReceiveAppPush(from: String): Boolean {
        if (recentlyReceiveAppPushIds.contains(from)) {
            return true
        }
        recentlyReceiveAppPushIds.add(from)
        if (recentlyReceiveAppPushIds.size > 10) {
            recentlyReceiveAppPushIds.removeFirst()
        }
        return false
    }

    private fun forceUpdatePushToken() {
        scope.launch {
            val firebaseToken = GooglePushService.requestToken() ?: return@launch
            tonConnectRepository.updatePushToken(firebaseToken)
        }
    }




    /*@SuppressLint("MissingPermission")
    private suspend fun displayNotification(push: PushData) {
        val activeNotifications = notificationManager.activeNotifications
        val oldNotification: StatusBarNotification? = activeNotifications.firstOrNull { it.notification.extras?.getCharSequence(Notification.EXTRA_TEXT) == push.notificationBody }
        val isNew = oldNotification == null
        val notificationTag = oldNotification?.tag ?: push.type
        val notificationId = oldNotification?.id ?: randomNotificationId()

        try {
            val event = getEvent(push)
            val wallet = walletRepository.getWallet(push.account) ?: return
            if (wallet.testnet) {
                return
            }
            val walletLabel = wallet.label
            val builder = NotificationCompat.Builder(context, notificationChannelDefault)
            builder.setContentTitle(walletLabel.title)
            builder.setContentText(push.notificationBody)
            builder.setSmallIcon(R.drawable.ic_push)
            builder.setColorized(true)
            builder.setGroupSummary(true)
            builder.setShowWhen(true)
            builder.setGroup(push.type)
            if (!isNew) {
                builder.setOnlyAlertOnce(true)
            }
            builder.setColor(walletLabel.color)
            builder.setLargeIcon(getLargeIcon(event))
            builder.setContentIntent(createPendingIntent(push.deeplink))
            notificationManager.notify(notificationTag, notificationId, builder.build())
        } catch (ignored: Throwable) { }
    }

    private fun getLargeIcon(event: EventEntity?): Bitmap? {
        val firstAction = event?.actions?.firstOrNull() ?: return null
        val bigIconUri = (firstAction.sender?.iconUri ?: firstAction.token?.imageUri) ?: return null
        if (bigIconUri.isLocal) {
            return null
        }
        return bigIconUri.getBitmap()
    }

    private suspend fun getEvent(
        push: PushData
    ): EventEntity? {
        return try {
            val eventId = push.transactionHash ?: return null
            eventsRepository.getSingleRemote(push.account, false, eventId)
        } catch (ignored: Throwable) {
            null
        }
    }

    private fun createPendingIntent(deepLink: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, deepLink.toUri())
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

     */

    private suspend fun subscribe(firebaseToken: String, wallets: List<WalletEntity>) {
        tonConnectRepository.updatePushToken(firebaseToken)

        val accounts = wallets.filter { !it.testnet }.map { it.accountId.toUserFriendly(testnet = false) }
        api.pushSubscribe(context.locale, firebaseToken, settingsRepository.installId, accounts)
    }
}