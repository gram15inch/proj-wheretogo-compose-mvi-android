package com.wheretogo.presentation.handler

import androidx.annotation.StringRes
import com.wheretogo.domain.BanReason
import com.wheretogo.domain.formatMillisToDate
import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.domain.handler.RootEvent
import com.wheretogo.domain.handler.RootHandler
import com.wheretogo.domain.model.app.Ban
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg

class RootHandlerImpl(val errorHandler: ErrorHandler) : RootHandler {
    override suspend fun handle(event: RootEvent, data:Any?) {
        when (event) {
            RootEvent.APP_CHECK_SUCCESS -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.app_check_success)))
            RootEvent.ACCOUNT_VALID_EXPIRE -> {
                EventBus.send(AppEvent.Navigation(null, AppScreen.Home, true))
                EventBus.send(AppEvent.SignInScreen)
                if(data is Ban) {
                    val resStr= data.reason.toStringRes()
                    val releaseAt = formatMillisToDate(data.releaseAt)
                    EventBus.send(AppEvent.SnackBar(EventMsg(resStr,releaseAt, isLongShow = true)))
                } else {
                    EventBus.send(AppEvent.SnackBar(EventMsg(R.string.banned_user)))
                }
            }
        }
    }

    override suspend fun handle(error: Throwable): Throwable {
        return errorHandler.handle(error)
    }


    @StringRes
    fun BanReason.toStringRes(): Int {
        return when (this) {
            BanReason.SPAM -> R.string.ban_reason_spam
            BanReason.ABUSE -> R.string.ban_reason_abuse
            BanReason.HARASSMENT -> R.string.ban_reason_harassment
            BanReason.INAPPROPRIATE -> R.string.ban_reason_inappropriate
            BanReason.ILLEGAL_CONTENT -> R.string.ban_reason_illegal_content
            BanReason.FRAUD -> R.string.ban_reason_fraud
            BanReason.IMPERSONATION -> R.string.ban_reason_impersonation
            BanReason.MALICIOUS_BEHAVIOR -> R.string.ban_reason_malicious_behavior
            BanReason.POLICY_VIOLATION -> R.string.ban_reason_policy_violation
            BanReason.OTHER -> R.string.banned_user
        }
    }
}