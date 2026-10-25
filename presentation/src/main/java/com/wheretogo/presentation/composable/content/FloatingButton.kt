package com.wheretogo.presentation.composable.content

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dhkim139.core.ui.screen.Transitions
import com.dhkim139.core.ui.theme.Palette
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.presentation.DriveFloatHighlight
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.animation.highlightCircle
import com.wheretogo.presentation.state.FloatingButtonState


@Composable
fun FloatingButtons(
    modifier: Modifier = Modifier,
    state: FloatingButtonState = FloatingButtonState(),
    guideStep: DriveTutorialStep,
    isVisible: Boolean,
    onCommentClick: () -> Unit,
    onInfoClick: () -> Unit,
    onExportMapClick: () -> Unit,
    onFoldClick: () -> Unit,
) {
    val isCommentVisible = isVisible && FloatingButtonState.commentVisible.contains(state.stateMode)
    val isInfoVisible: Boolean =
        isVisible && FloatingButtonState.infoVisible.contains(state.stateMode)
    val isExportVisible: Boolean =
        isVisible && FloatingButtonState.exportVisible.contains(state.stateMode)
    val isFoldVisible: Boolean =
        isVisible && FloatingButtonState.foldVisible.contains(state.stateMode)

    val swap = when {
        isExportVisible -> 1
        isCommentVisible -> 2
        else -> 0
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        val buttonEndPadding = 12.dp

        ActiveOfFloatBtnArea(
            guideStep
        ) { active ->
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = buttonEndPadding)
            ) {
                AnimatedContent(
                    targetState = swap,
                    transitionSpec = Transitions.sharedAxisX(),
                    label = "floatButtonSwap"
                ) { mode ->
                    when (mode) {
                        1 -> CircleButton(
                            modifier = Modifier.padding(end = buttonEndPadding),
                            vector = Icons.Default.DriveEta,
                            isHighLight = state.highlight == DriveFloatHighlight.EXPORT,
                            isActive = active.export,
                            onClick = onExportMapClick
                        )

                        2 -> CircleButton(
                            modifier = Modifier.padding(end = buttonEndPadding),
                            icon = R.drawable.ic_message,
                            isHighLight = state.highlight == DriveFloatHighlight.COMMENT,
                            isActive = active.comment,
                            onClick = onCommentClick
                        )

                        else -> {}
                    }
                }


                SlideAnimation(
                    modifier = Modifier,
                    visible = isInfoVisible,
                    direction = AnimationDirection.RightCenter
                ) {
                    CircleButton(
                        modifier = Modifier.padding(
                            top = 0.dp,
                            end = buttonEndPadding
                        ),
                        icon = R.drawable.ic_info,
                        isHighLight = state.highlight == DriveFloatHighlight.INFO,
                        isActive = active.info
                    ) {
                        onInfoClick()
                    }
                }

                SlideAnimation(
                    modifier = Modifier.padding(end = buttonEndPadding),
                    visible = isFoldVisible,
                    direction = AnimationDirection.RightCenter
                ) {
                    CircleButton(
                        modifier = Modifier,
                        icon = R.drawable.ic_close,
                        isHighLight = state.highlight == DriveFloatHighlight.FOLD,
                        isActive = active.fold
                    ) {
                        onFoldClick()
                    }
                }
            }
        }


    }
}


@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    isHighLight: Boolean = false,
    isActive: Boolean = true,
    @DrawableRes icon: Int? = null,
    vector: ImageVector? = null,
    color: Color = Palette.White100.copy(alpha = 0.85f),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isActive,
        modifier = modifier
            .size(60.dp)
            .highlightCircle(isHighLight),
        shape = CircleShape,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.25f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        when {
            vector != null -> Icon(
                imageVector = vector,
                contentDescription = null,
                tint = Palette.Gray260,
                modifier = Modifier.size(28.dp)
            )

            icon != null -> Image(
                painter = painterResource(id = icon),
                contentDescription = "Icon Description",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
