package com.dhkim139.admin.wheretogo.core


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wheretogo.domain.repository.AppRepository
import com.wheretogo.domain.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminMsgService() : FirebaseMessagingService() {
    @Inject
    lateinit var appRepository: AppRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var adminNotification: AdminNotification

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        CoroutineScope(Dispatchers.Main).launch {
            handleMessage(message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.updateMsgToken(token)
        }
    }

    private suspend fun handleMessage(msg: RemoteMessage) {
        val reportId = msg.data.getOrDefault("id", null)
        val title = msg.data.getOrDefault("title", null)
        val body = msg.data.getOrDefault("body", null)
        adminNotification.show(title, body, targetId = reportId)
    }
}