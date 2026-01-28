package com.wheretogo.domain.model.app

import com.wheretogo.domain.FcmMsg

data class AppMessage(val type: FcmMsg, val data: Any? = null)