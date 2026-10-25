package com.dhkim139.feature.navigationoption

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.core.ui.screen.navigationBarsBottomPadding
import com.dhkim139.feature.navigationoption.ui.NavOptionContent
import com.dhkim139.feature.navigationoption.ui.NavOptionEffect
import com.dhkim139.feature.navigationoption.ui.NavOptionIntent
import com.dhkim139.feature.navigationoption.ui.NavOptionScreenState
import com.dhkim139.feature.navigationoption.ui.NavOptionViewModel
import com.dhkim139.feature.navigationoption.ui.SheetContent
import com.dhkim139.feature.navigationoption.ui.SheetMotion
import com.dhkim139.feature.navigationoption.ui.UiText
import com.dhkim139.feature.navigationoption.ui.asString

@Composable
fun NavOptionSheet(
    navOption: NavOption?,
    isOpen: Boolean,
    onFocus: (FocusTarget) -> Unit,
    onStartNavigation: (NavOptionResult) -> Unit,
    overviewMaxHeight: Dp,
    onContentHeightChange: (Dp) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: NavOptionViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(isOpen, navOption) {
        when {
            isOpen -> navOption?.let { viewModel.onIntent(NavOptionIntent.Load(navOption)) }
            navOption == null -> viewModel.onIntent(NavOptionIntent.ClosePicker)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NavOptionEffect.Focus -> onFocus(effect.focusTarget)
                is NavOptionEffect.StartNavigation -> onStartNavigation(effect.result)
            }
        }
    }

    LaunchedEffect(state) {
        (state as? NavOptionScreenState.Ready)?.let {
            when (it.content) {
                is SheetContent.Overview -> {
                    onContentHeightChange(overviewMaxHeight)
                }

                is SheetContent.MapAppPicker ->{
                    onContentHeightChange(700.dp) // 실제보다 더 크게
                }

                is SheetContent.ParkingPicker -> {
                    if(!it.content.loading)
                        onContentHeightChange(700.dp)


                }
            }
        }
    }

    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = Color.Transparent,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            AnimatedContent(
                targetState = state,
                contentKey = { it::class },
                transitionSpec = {
                    fadeIn(tween(SheetMotion.DURATION_MS))
                        .togetherWith(fadeOut(tween(SheetMotion.EXIT_MS)))
                },
                label = "navOptionScreenState",
            ) { s ->
                Box(
                    modifier = Modifier
                        .heightIn(min=300.dp)
                        .navigationBarsBottomPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                ) {
                    when (s) {
                        NavOptionScreenState.Loading -> if (isOpen) LoadingBlock()

                        is NavOptionScreenState.Error -> ErrorBlock(
                            message = s.message,
                            onRetry = { viewModel.onIntent(NavOptionIntent.Retry) },
                        )

                        is NavOptionScreenState.Ready -> NavOptionContent(
                            content = s.content,
                            onIntent = viewModel::onIntent,
                            overviewMaxHeight = overviewMaxHeight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingBlock(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) { /* 추후 로딩 에니메이션 추가 */ }
}

@Composable
private fun ErrorBlock(
    message: UiText,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message.asString(),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.error_retry))
        }
    }
}
