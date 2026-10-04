package com.wheretogo.data.feature

import com.wheretogo.domain.model.course.GeoPoint
import kotlin.collections.plusAssign


object PolylineCodec {

    private const val ASCII_OFFSET = 63
    private const val CHUNK_MASK = 0x1f
    private const val CHUNK_CONTINUE = 0x20

    /** 5자리 고정소수점 → 십진 좌표 */
    private const val PRECISION = 1e5

    /** 한 값이 쓸 수 있는 최대 시프트. 넘으면 32비트를 벗어난 손상된 입력이다 */
    private const val MAX_SHIFT = 30

    fun decode(encoded: String): List<GeoPoint> {
        if (encoded.isEmpty()) return emptyList()

        val points = ArrayList<GeoPoint>()
        val cursor = intArrayOf(0) // [0] = 지금 읽는 위치. 값과 함께 돌려줄 게 둘이라 배열로 나른다
        var lat = 0
        var lng = 0

        while (cursor[0] < encoded.length) {
            val deltaLat = readSigned(encoded, cursor) ?: return emptyList()
            val deltaLng = readSigned(encoded, cursor) ?: return emptyList()
            lat += deltaLat
            lng += deltaLng
            points += GeoPoint(lat = lat / PRECISION, lng = lng / PRECISION)
        }
        return points
    }

    private fun readSigned(encoded: String, cursor: IntArray): Int? {
        var shift = 0
        var acc = 0
        var chunk: Int
        do {
            if (cursor[0] >= encoded.length) return null // 값 중간에 문자열이 끝났다
            chunk = encoded[cursor[0]++].code - ASCII_OFFSET
            if (chunk < 0 || shift > MAX_SHIFT) return null
            acc = acc or ((chunk and CHUNK_MASK) shl shift)
            shift += 5
        } while (chunk >= CHUNK_CONTINUE)

        // 최하위 비트가 부호다 — 켜져 있으면 음수라 비트 반전
        return if (acc and 1 != 0) (acc shr 1).inv() else acc shr 1
    }
}
