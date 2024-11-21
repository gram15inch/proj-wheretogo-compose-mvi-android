package com.wheretogo.presentation.exceptions

class UnexpectedTypeOfCredentialException(
    message: String = "예측되지않은 Credential"
) : Exception(message)