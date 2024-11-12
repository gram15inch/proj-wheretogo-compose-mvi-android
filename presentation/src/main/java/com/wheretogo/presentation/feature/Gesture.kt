package com.wheretogo.presentation.feature

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.PointerInputScope

enum class GestureDirection {
    Right, Left, Down
}

suspend fun detectDrag(
    direction: GestureDirection,
    onDrag: () -> Unit
): suspend PointerInputScope.() -> Unit {
    return when (direction) {
        GestureDirection.Left -> {
            {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        if (dragAmount < -5) {
                            onDrag()
                        }
                    }
                )
            }
        }

        GestureDirection.Down -> {
            {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 5) {
                            onDrag()
                        }
                    }
                )
            }
        }

        else -> {
            {
                detectVerticalDragGestures(onVerticalDrag = { _, _ -> })
            }
        }
    }
}