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
            val oldItem = list[idx]
            if (oldItem.getFingerPrint() != item.getFingerPrint()) {
                if(oldItem is MapOverlay)
                    oldItem.reflectClear()
                list[idx] = item
            }
            false
        }
    }

    fun remove(id: String): Boolean {
        val removeIdx = indexById[id] ?: return false
        val removeItem = list[removeIdx]
        if(removeItem is MapOverlay)
            removeItem.reflectClear()

        val lastIdx = list.lastIndex
        val lastItem = list[lastIdx]
        val lastId = keyOf(lastItem)

        if (removeIdx != lastIdx) {
            list[removeIdx] = lastItem
            indexById[lastId] = removeIdx
        }

        list.removeAt(lastIdx)
        indexById.remove(id)
        return true
    }

    fun isEmpty(): Boolean = list.isEmpty()

    fun contains(id: String): Boolean = indexById.containsKey(id)

    fun toList(): List<T> {
        return list
    }

    fun clear() {
        list.forEach { if(it is MapOverlay) it.reflectClear() }
        list.clear()
        indexById.clear()
    }

}