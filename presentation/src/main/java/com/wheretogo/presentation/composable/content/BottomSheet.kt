package com.wheretogo.presentation.composable.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dhkim139.core.ui.theme.Palette
import com.wheretogo.presentation.SheetVisibleMode
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    isOpen: Boolean = false,
    minHeight: Dp,
    isSpaceVisibleWhenClose: Boolean,
    bottomSpace: Dp = 0.dp,
    onSheetStateChange: (SheetVisibleMode) -> Unit,
    onSheetHeightChange: (Dp) -> Unit,
    dragHandleColor: Color? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var latestDp by remember { mutableStateOf(0.dp) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetState = scaffoldState.bottomSheetState
    BackHandler(isOpen) {}

    val onStateChange by rememberUpdatedState(onSheetStateChange)
    var latestVisibleValue by remember { mutableStateOf<SheetVisibleMode?>(null) }

    LaunchedEffect(isOpen) {
        if (isOpen) sheetState.expand() else sheetState.partialExpand()
    }

    val scope = rememberCoroutineScope()
    var skipOpening by remember { mutableStateOf(false) }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.targetValue to sheetState.currentValue }
            .distinctUntilChanged()
            .drop(1)
            .collect { (target, current) ->
                val mode = toVisibleMode(target, current) ?: return@collect

                when (mode) {
                    SheetVisibleMode.Closing -> {
                        // 닫기도중 열기 방지
                        if (!skipOpening) {
                            skipOpening = true
                            scope.launch { sheetState.partialExpand() }
                        }
                    }
                    SheetVisibleMode.Closed -> skipOpening = false
                    else -> {}
                }

                if (skipOpening && mode == SheetVisibleMode.Opening) return@collect
                if (mode == latestVisibleValue) return@collect

                latestVisibleValue = mode
                onStateChange(mode)
            }
    }

    val initHeightWithSpace =
        minHeight + if (isSpaceVisibleWhenClose) bottomSpace else 0.dp

    Box(
        modifier = modifier
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetSwipeEnabled = !skipOpening,
            sheetContainerColor = Palette.White,
            containerColor = Color.Transparent,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            val heightPx = coordinates.size.height
                            val dp =
                                if (isOpen)
                                    with(density) { heightPx.toDp() + 20.dp }
                                else
                                    initHeightWithSpace.run { if (this < 0.dp) 0.dp else this }
                            if (dp != latestDp) {
                                onSheetHeightChange(dp)
                                latestDp = dp
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                    ) {
                        content()
                        Spacer(Modifier.height(bottomSpace)) // 시스템바 패딩
                    }
                }
            },
            sheetDragHandle = {
                DragHandle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .run {
                        if (dragHandleColor != null)
                            this.background(dragHandleColor)
                        else
                            this
                    }
                )
            },
            sheetPeekHeight = initHeightWithSpace,
            content = {}
        )
    }
}


@Composable
fun DragHandle(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .padding(top = 15.dp)
                .clip(RoundedCornerShape(16.dp))
                .width(40.dp)
                .height(5.dp)
                .background(Palette.Gray100.copy(alpha = 0.5f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun toVisibleMode(target: SheetValue, current: SheetValue): SheetVisibleMode? =
    when {
        target == Expanded && current == Expanded -> SheetVisibleMode.Opened
        target == PartiallyExpanded && current == PartiallyExpanded -> SheetVisibleMode.Closed
        target == Expanded -> SheetVisibleMode.Opening
        target == PartiallyExpanded -> SheetVisibleMode.Closing
        else -> null
    }



