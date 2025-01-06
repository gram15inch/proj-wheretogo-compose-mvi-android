package com.wheretogo.presentation.feature


fun formatFileSizeToMB(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return "%.1f MB".format(mb)
}
