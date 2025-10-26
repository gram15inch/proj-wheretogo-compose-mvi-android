package com.wheretogo.data.model.auth

import com.wheretogo.data.DataAuthCompany

data class DataSyncToken(
    val authCompany: DataAuthCompany = DataAuthCompany.PROFILE,
    val token: String = ""
)