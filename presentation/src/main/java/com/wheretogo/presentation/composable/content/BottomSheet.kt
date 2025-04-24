package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    initHeight: Int = 0,
    isVisible: Boolean = false,
    onStateChange: (SheetState) -> Unit,
    onHeightChange: (Dp) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var latestDp by remember { mutableStateOf(0.dp) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val sheetState = scaffoldState.bottomSheetState
    LaunchedEffect(isVisible) {
        if (isVisible) {
            scaffoldState.bottomSheetState.expand()
        } else {
            if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded)
                scaffoldState.bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(sheetState.targetValue) {
        snapshotFlow { sheetState.targetValue }.collect { value ->
            when (value) {
                SheetValue.Expanded -> {
                    onStateChange(SheetState.Expand)
                }

                SheetValue.PartiallyExpanded -> {
                    onStateChange(SheetState.PartiallyExpand)
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(sheetState.currentValue) {
        snapshotFlow { sheetState.currentValue }.collect { value ->
            when (value) {
                SheetValue.Expanded -> {
                    onStateChange(SheetState.Expanded)
                }

                SheetValue.PartiallyExpanded -> {
                    onStateChange(SheetState.PartiallyExpanded)
                }

                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .statusBarsPadding()
    ) {
        BottomSheetScaffold(
            modifier = Modifier
                .fillMaxWidth(),
            scaffoldState = scaffoldState,
            sheetContainerColor = Color.White,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val heightPx = coordinates.size.height
                            val dp =
                                if (isVisible) with(density) { heightPx.toDp() + 20.dp } else initHeight.dp
                            if (dp != latestDp) {
                                onHeightChange(dp)
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
                    }
                }
            },
            sheetDragHandle = {
                DragHandle()
            },
            sheetPeekHeight = initHeight.dp
        ) {}
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
                .background(colorResource(R.color.gray_C7C7C7_80))
        )
    }
}



