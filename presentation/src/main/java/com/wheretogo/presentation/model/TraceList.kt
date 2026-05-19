package com.wheretogo.presentation.model

import com.wheretogo.presentation.OverlayType


interface Traceable {
    fun getFingerPrint(): Int
}

interface MapOverlay : Traceable {
    val key: String
    val type: OverlayType
    fun replaceVisible(isVisible: Boolean): MapOverlay
    fun reflectClear()
}

class TraceList<T : Traceable>(
    private val keyOf: (T) -> String
) {
    private val list = mutableListOf<T>()
    private val indexById = HashMap<String, Int>()

    fun getFingerPrint(isOrder: Boolean = false): Int {
        return if (isOrder) {
            list.fold(1) { acc, item -> 31 * acc + item.getFingerPrint() }
        } else
            list.fold(1) { acc, item -> acc + item.getFingerPrint() }
    }

    fun getOrNull(id: String): T? {
        val idx = indexById[id] ?: return null
        return list[idx]
    }

    fun addOrReplace(item: T): Boolean {
        val id = keyOf(item)
        val idx = indexById[id]
        return if (idx == null) {
            val newIndex = list.size
            list.add(item)
            indexById[id] = newIndex
            true
        } else {
            if (list[idx].getFingerPrint() != item.getFingerPrint())
                list[idx] = item
            false
        }
    }

    fun remove(id: String): Boolean {
        val idx = indexById[id] ?: return false

        val lastIndex = list.lastIndex
        val lastItem = list[lastIndex]
        val lastId = keyOf(lastItem)

        if (idx != lastIndex) {
            list[idx] = lastItem
            indexById[lastId] = idx
        }

        list.removeAt(lastIndex)
        indexById.remove(id)
        return true
    }

    fun isEmpty(): Boolean = list.isEmpty()

    fun contains(id: String): Boolean = indexById.containsKey(id)

    fun toList(): List<T> {
        return list
    }

    fun clear() {
        list.clear()
        indexById.clear()
    }

}