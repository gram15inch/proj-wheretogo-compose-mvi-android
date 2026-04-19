package com.wheretogo.domain.model.app

import com.wheretogo.domain.BanReason

data class Ban(val reason: BanReason, val releaseAt: Long)