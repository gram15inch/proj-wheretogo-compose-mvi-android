package com.wheretogo.presentation.composable.content

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.domain.model.util.ImageInfo
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.formatFileSizeToMB
import com.wheretogo.presentation.feature.getFileInfoFromUri
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.theme.Gray5060
import com.wheretogo.presentation.theme.PrimeBlue
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.theme.White
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily

@Composable
fun CheckPointAddContent(
    state: CheckPointAddState,
    focusRequester: FocusRequester,
    onSubmitClick: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onImageChange: (ImageInfo) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 위치 슬라이더
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionLabel(text = stringResource(R.string.checkpoint))
            LocationSlider(
                percentage = state.sliderPercent,
                onSliderChange = onSliderChange
            )
        }

        // 사진
        val contentResolver = LocalContext.current.contentResolver
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null && uri.path != null) {
                getFileInfoFromUri(contentResolver, uri)
                    .onSuccess { fileInfo ->
                        onImageChange(
                            ImageInfo(
                                uriString = uri.toString(),
                                fileName = fileInfo.first ?: "",
                                byte = fileInfo.second ?: 0
                            )
                        )
                    }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionLabel(text = stringResource(R.string.photo))
            PhotoDropZone(
                imgInfo = state.imgInfo,
                onClick = { launcher.launch("image/*") }
            )
        }

        // 설명
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionLabel(text = stringResource(R.string.description))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .fillMaxWidth()
                    .height(75.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    text = state.description.ifEmpty { stringResource(R.string.description_hint) },
                    fontFamily = interFontFamily,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (state.description.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }

        // 제출 버튼
        val btnBackground by animateColorAsState(
            targetValue = if (state.isSubmitActive) PrimeBlue else Gray5060,
            label = "btnBg"
        )
        val btnTextColor by animateColorAsState(
            targetValue = if (state.isSubmitActive) White else MaterialTheme.colorScheme.onSurfaceVariant,
            label = "btnText"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .height(58.dp)
                .background(btnBackground)
                .clickable(enabled = state.isSubmitActive || !state.isLoading) {
                    onSubmitClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                DelayLottieAnimation(
                    modifier = Modifier.size(36.dp),
                    ltRes = R.raw.lt_loading,
                    isVisible = true,
                    delay = 0
                )
            } else {
                Text(
                    text = stringResource(R.string.submit),
                    color = btnTextColor,
                    fontSize = 15.sp,
                    fontFamily = interBoldFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 15.sp,
        fontFamily = interBoldFontFamily,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun PhotoDropZone(
    imgInfo: ImageInfo?,
    onClick: () -> Unit
) {
    val hasImage = imgInfo != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.5.dp,
                color = if (hasImage)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mk_cm),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${imgInfo!!.fileName}  ${formatFileSizeToMB(imgInfo.byte)}",
                    fontSize = 13.sp,
                    fontFamily = interFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_marker),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.photo_hint),
                    fontSize = 13.sp,
                    fontFamily = interFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LocationSlider(
    modifier: Modifier = Modifier,
    percentage: Float,
    onSliderChange: (Float) -> Unit
) {
    Column(modifier = modifier) {
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            value = percentage,
            onValueChange = onSliderChange,
            colors = SliderDefaults.colors().copy(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            valueRange = 0f..1f,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.start),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontFamily = interFontFamily
            )
            Text(
                text = stringResource(R.string.end),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontFamily = interFontFamily
            )
        }
    }
}

@Preview
@Composable
fun CheckpointAddBottomSheetPreview() {
    val state = BottomSheetState(
        infoState = InfoState(isRemoveButton = true),
        checkPointAddState = CheckPointAddState(
            isLoading = false, description = "내용이 오는곳입니다 내용이 오는곳입니다 내용이 오는곳입니다 내용이 오는곳입니다 내용이 오는곳입니다 내용이 오는곳입니다",
            imgInfo = ImageInfo("", "새로운 사진.jpg", 30L)
        )
    )
    WhereTogoTheme {
        Box(modifier = Modifier) {
            BottomSheet(
                modifier = Modifier.height(600.dp),
                isOpen = true,
                minHeight = 400.dp,
                bottomSpace = 0.dp,
                onSheetStateChange = {},
                onSheetHeightChange = {},
                isSpaceVisibleWhenClose = true
            ) {
                if (true) {
                    CheckPointAddContent(
                        state = state.checkPointAddState,
                        FocusRequester(), {}, {}, {}
                    )
                } else {
                    InfoContent(
                        state = state.infoState,
                        {}, {}
                    )
                }
            }
        }
    }
}

