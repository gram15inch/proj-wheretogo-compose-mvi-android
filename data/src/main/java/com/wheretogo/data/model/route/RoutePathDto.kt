package com.wheretogo.data.model.route


data class RoutePathResponseDto(
    val format: String? = null,
    val forward: String? = null,
    val backward: String? = null,
) {

    val isSupported: Boolean get() = format.equals(FORMAT_POLYLINE5, ignoreCase = true)

    companion object {
        // 정밀도 1e-5(약 1.1m)
        const val FORMAT_POLYLINE5 = "polyline5"
    }
}
