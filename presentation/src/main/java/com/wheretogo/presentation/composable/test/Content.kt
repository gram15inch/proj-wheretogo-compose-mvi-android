package com.wheretogo.presentation.composable.test

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll


@Composable
fun NestedDragScroll(
    modifier: Modifier,
    onDrag: (Offset) -> Unit,
    content: @Composable () -> Unit
) {
    val nestedScrollDispatcher = remember { NestedScrollDispatcher() }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                onDrag(available)

                return super.onPreScroll(available, source)
            }
        }
    }

    Box(modifier = modifier.nestedScroll(nestedScrollConnection, nestedScrollDispatcher)) {
        content()
    }
}