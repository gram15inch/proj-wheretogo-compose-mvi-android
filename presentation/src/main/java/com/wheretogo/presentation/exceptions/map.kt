package com.wheretogo.presentation.exceptions

class MapNotInitializedException(
    message: String = "맵이 초기화 되지 않음"
) : Exception(message)