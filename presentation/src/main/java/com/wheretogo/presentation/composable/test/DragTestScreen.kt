package com.wheretogo.presentation.composable.test

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DragTestScreen() {
    val offset = remember { Animatable(Offset(0f, 0f),Offset.VectorConverter) }

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        CoroutineScope(Dispatchers.IO).launch{
            detectDragGestures { change, dragAmount ->
                launch {
                    change.consume()
                    val newOffset = offset.value + dragAmount
                    offset.snapTo(newOffset)
                }
            }
        }

    }){

        Text(modifier = Modifier.offset { offset.value.toIntOffset() }, text = "hi", fontSize = 24.sp)
        Text(modifier = Modifier.offset {( offset.value+Offset(50f,60f)).toIntOffset() }, text = "hi", fontSize = 24.sp)
    }
}
fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())
