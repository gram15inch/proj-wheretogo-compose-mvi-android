package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.theme.Gray6080

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
    var latestDoneValue by remember { mutableStateOf<SheetValue?>(null) }
    var latestIngValue by remember { mutableStateOf<SheetValue?>(null) }
    var latestVisibleValue by remember { mutableStateOf<SheetVisibleMode?>(null) }
    LaunchedEffect(isOpen) {
        if (isOpen) {
            scaffoldState.bottomSheetState.expand()
        }else if (latestVisibleValue == SheetVisibleMode.Opened)
            scaffoldState.bottomSheetState.partialExpand()
    }

    LaunchedEffect(sheetState.targetValue) {
        if (latestIngValue == sheetState.targetValue) return@LaunchedEffect
        when (sheetState.targetValue) {
            SheetValue.Expanded -> {
                if(latestVisibleValue == SheetVisibleMode.Closing) {
                    return@LaunchedEffect scaffoldState.bottomSheetState.partialExpand()
                }
                onSheetStateChange(SheetVisibleMode.Opening)
                latestVisibleValue = SheetVisibleMode.Opening
            }

            SheetValue.PartiallyExpanded -> {
                if(latestIngValue!=null)
                    onSheetStateChange(SheetVisibleMode.Closing)
                latestVisibleValue = SheetVisibleMode.Closing
            }

            else -> {}
        }
        latestIngValue = sheetState.targetValue
    }

    LaunchedEffect(sheetState.currentValue) {
        if (latestDoneValue == sheetState.targetValue) return@LaunchedEffect
        when (sheetState.currentValue) {
            SheetValue.Expanded -> {
                onSheetStateChange(SheetVisibleMode.Opened)
                latestVisibleValue = SheetVisibleMode.Opened
            }

            SheetValue.PartiallyExpanded -> {
                if(latestDoneValue!=null) //시트 처음 생성시 호출 방지
                    onSheetStateChange(SheetVisibleMode.Closed)
                latestVisibleValue = SheetVisibleMode.Closed
            }

            else -> {}
        }
        latestDoneValue = sheetState.currentValue
    }

    val initHeightWithSpace =
        minHeight + if (isSpaceVisibleWhenClose) bottomSpace else 0.dp

    Box(
        modifier = modifier
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContainerColor = Color.White,
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
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                    ) {
                        content()
                        Spacer(Modifier.height(bottomSpace))
                    }
                }
            },
            sheetDragHandle = {
                DragHandle(
                    modifier = Modifier.run {
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
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .padding(top = 15.dp)
                .clip(RoundedCornerShape(16.dp))
                .width(40.dp)
                .height(5.dp)
                .background(Gray6080)
        )
    }
}



