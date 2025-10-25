package com.wheretogo.domain.model.auth

import com.wheretogo.domain.AuthCompany

data class SignToken(val token: String, val authCompany: AuthCompany)