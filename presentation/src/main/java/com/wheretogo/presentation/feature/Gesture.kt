package com.wheretogo.presentation.feature

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

enum class GestureDirection {
    Right, Left, Down, Up
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

        GestureDirection.Up -> {
            {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount < 5) {
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


@Composable
fun Modifier.eventConsumption(): Modifier {
    return this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent()
            }
        }
    }
}

@Composable
fun Modifier.topShadow(): Modifier {
    return this.graphicsLayer {
        shadowElevation = 8.dp.toPx()
        shape = RectangleShape
        clip = false
    }
}

