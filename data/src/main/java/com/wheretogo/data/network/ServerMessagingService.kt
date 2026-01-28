package com.wheretogo.data.network

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wheretogo.domain.BanReason
import com.wheretogo.domain.FcmMsg
import com.wheretogo.domain.model.app.AppMessage
import com.wheretogo.domain.model.app.Ban
import com.wheretogo.domain.repository.AppRepository
import com.wheretogo.domain.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ServerMessagingService() : FirebaseMessagingService() {
    @Inject lateinit var appRepository: AppRepository
    @Inject lateinit var userRepository: UserRepository

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        CoroutineScope(Dispatchers.Main).launch {
            if (message.data.isNotEmpty()) {
                handleDataMessage(message.data)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.updateMsgToken(token)
        }
    }

    private suspend fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            FcmMsg.BAN.toString() -> {
                val reason = runCatching {
                    BanReason.valueOf(data["reason"]?:"")
                }.getOrDefault(BanReason.OTHER)
                val releaseAt = runCatching {
                    val strNum= data["releaseAt"]?:""
                    strNum.toLong()
                }.getOrDefault(0)
                appRepository.sendMsg(AppMessage(FcmMsg.BAN, Ban(reason, releaseAt)))
            }
        }
    }
}