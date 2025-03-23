package com.wheretogo.presentation.composable.content

import android.net.Uri
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.dummy.getCommentDummy
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.ExtendArea
import com.wheretogo.presentation.feature.BlurEffect
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.topShadow
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.theme.hancomMalangFontFamily


@Preview
@Composable
fun PopupPreview() {
    Box(modifier = Modifier) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            CommentState(
                isCommentVisible = true,
                isCommentSettingVisible = false,
                commentItemGroup = getCommentDummy().mapIndexed {idx,item->
                    CommentItemState(
                        data = item,
                        isLike = if (item.like % 2 == 0) false else true,
                        isFold = if (item.like % 2 == 0) true else false,
                        isFocus = idx == 0
                    )
                },
                commentAddState = CommentAddState(
                    isEmogiGroup = true,
                    emogiGroup = getEmogiDummy()
                )
            ),
            imageUri = Uri.parse(""),
            isWideSize = false,
            isLoading = false,
            onPopupImageClick = {},
            onPopupBlurClick = {},
            onCommentListItemClick = {},
            onCommentListItemLongClick = {},
            onCommentLikeClick = {},
            onCommentAddClick = {},
            onCommentRemoveClick = {},
            onCommentReportClick = {},
            onCommentEditValueChange = {},
            onCommentEmogiPress = {},
            onCommentTypePress = {}
        )
    }
}


@Composable
fun MapPopup(
    modifier: Modifier,
    commentState: CommentState,
    imageUri: Uri?,
    isWideSize: Boolean,
    isLoading:Boolean,
    onPopupImageClick: () -> Unit ,
    onPopupBlurClick:() -> Unit ,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (CommentItemState) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit,
    onCommentAddClick: (CommentAddState) -> Unit,
    onCommentRemoveClick: (CommentItemState) -> Unit,
    onCommentReportClick: (CommentItemState) -> Unit,
    onCommentEditValueChange: (TextFieldValue) -> Unit,
    onCommentEmogiPress: (String) -> Unit,
    onCommentTypePress: (CommentType) -> Unit,
) {
    var imeContainerHeight by remember { mutableStateOf(0.dp) }
    Box {
        ExtendArea( // 넓은 화면에서 확장
            isExtend = isWideSize,
            holdContent = {
                PopUpImage( // 고정
                    modifier = modifier.clickable {
                        onPopupImageClick()
                    },
                    uri = imageUri
                )
            },
            moveContent = { // 이동
                FadeAnimation(visible = commentState.isCommentVisible && !isWideSize) {
                    BlurEffect(
                        modifier = Modifier
                            .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        onClick = {
                            onPopupBlurClick()
                        })
                }
                SlideAnimation(
                    modifier = modifier
                        .graphicsLayer(clip = true),
                    visible = commentState.isCommentVisible,
                    direction = if (isWideSize) AnimationDirection.CenterRight else AnimationDirection.CenterDown
                ) {
                    Box(
                        modifier = Modifier
                            .consumptionEvent()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorResource(R.color.white))
                    ) {
                        Column {
                            val maxHeight =
                                (if (!isWideSize) 480.dp else 500.dp) - imeContainerHeight
                            if (commentState.commentItemGroup.isNotEmpty()) {
                                CommentList(
                                    modifier = Modifier
                                        .sizeIn(maxHeight = maxHeight),
                                    isCompact = !isWideSize,
                                    commentItemGroup = commentState.commentItemGroup,
                                    onItemClick = { item ->
                                        onCommentListItemClick(item)
                                    },
                                    onItemLongClick = { item ->
                                        onCommentListItemLongClick(item)
                                    },
                                    onLikeClick = { item ->
                                        onCommentLikeClick(item)
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.sizeIn(maxHeight = maxHeight),
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if(isLoading)
                                            DelayLottieAnimation(Modifier.width(50.dp),R.raw.lt_loading, true,0)
                                        else
                                            Text(
                                                text = "첫 발자국을 남겨보세요.",
                                                fontFamily = hancomMalangFontFamily,
                                                fontSize = 14.sp
                                            )
                                    }
                                }
                            }
                            Spacer(modifier.height(imeContainerHeight))
                        }
                        FadeAnimation(visible = commentState.isCommentSettingVisible) {
                            CommentSetting(
                                modifier = Modifier
                                    .sizeIn(
                                        maxHeight = (if (!isWideSize) 480.dp else 500.dp)
                                    ),
                                selectedItem = commentState.selectedCommentSettingItem,
                                onCommentRemoveClick = onCommentRemoveClick,
                                onCommentReportClick = onCommentReportClick,
                                onBackgroundClick = {
                                    onCommentListItemLongClick(commentState.selectedCommentSettingItem)
                                })
                        }
                    }
                }
            }
        )
        SlideAnimation(
            modifier = modifier,
            visible = commentState.isCommentVisible && !commentState.isCommentSettingVisible,
            direction = AnimationDirection.CenterDown
        ) {
            ImeStickyBox(
                modifier = Modifier
                    .consumptionEvent()
                    .padding(top = 1.dp)
                    .fillMaxWidth()
                    .align(alignment = Alignment.BottomCenter),
                onBoxHeightChange = { height ->
                    imeContainerHeight = height
                }) { imeHeight ->
                Column(
                    modifier = Modifier
                        .topShadow()
                        .background(Color.White)
                ) {// 리뷰버튼
                    ReviewButtonGroup(
                        modifier = Modifier.run {
                            if (imeHeight > 100.dp) this
                            else this.height(0.dp)
                        },
                        selectedType = commentState.commentAddState.commentType,
                        onReviewButtonClick = onCommentTypePress
                    )

                    // 이모지
                    CommentEmojiGroupAndOneLinePreview(
                        isEmojiGroup = commentState.commentAddState.isEmogiGroup,
                        emojiGroup = commentState.commentAddState.emogiGroup,
                        oneLinePreview = commentState.commentAddState.oneLinePreview,
                        onImogiClick = onCommentEmogiPress
                    )

                    // 입력 텍스트
                    CommentTextField(
                        editText = commentState.commentAddState.editText,
                        isEmoji = commentState.commentAddState.isLargeEmogi,
                        emoji = commentState.commentAddState.largeEmoji.ifEmpty {
                            commentState.commentAddState.emogiGroup.firstOrNull() ?: ""
                        },
                        onValueChange = onCommentEditValueChange,
                        onDone = { onCommentAddClick(commentState.commentAddState) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewButtonGroup(
    modifier: Modifier,
    selectedType: CommentType,
    onReviewButtonClick: (CommentType) -> Unit
) {
    Row(
        modifier = modifier.padding(top = 0.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReviewButton(
            type = CommentType.ONE,
            selectedType = selectedType,
            color = R.color.teal_200,
            onReviewButtonClick = onReviewButtonClick
        )
        ReviewButton(
            type = CommentType.DETAIL,
            selectedType = selectedType,
            color = R.color.purple_200,
            onReviewButtonClick = onReviewButtonClick
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReviewButton(
    type: CommentType,
    selectedType: CommentType,
    @ColorRes color: Int,
    onReviewButtonClick: (CommentType) -> Unit
) {
    val buttonScale = remember { Animatable(1f) }
    var isPress by remember { mutableStateOf(false) }
    LaunchedEffect(isPress) {
        if (isPress)
            buttonScale.animateTo(0.9f, animationSpec = tween(100))
        else
            buttonScale.animateTo(1f, animationSpec = tween(100))
    }
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .scale(buttonScale.value)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(color))
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { // 누를 때
                        isPress = true
                        if (selectedType != type) onReviewButtonClick(type)
                        true
                    }

                    MotionEvent.ACTION_UP -> { // 뗄 때
                        isPress = false
                        true
                    }

                    else -> false
                }
            },
    ) {
        Text(
            modifier = Modifier.padding(top = 1.dp, bottom = 3.dp, start = 7.dp, end = 7.dp),
            text = stringResource(type.typeRes), color = Color.White,
            style = TextStyle(
                fontSize = 12.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false // 기본 글꼴 패딩 제거
                ),
                fontFamily = hancomMalangFontFamily,
                textAlign = TextAlign.Center
            )

        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CommentEmojiGroupAndOneLinePreview(
    isEmojiGroup: Boolean,
    emojiGroup: List<String>,
    oneLinePreview: String,
    onImogiClick: (String) -> Unit
) {

    Box(
        modifier = Modifier
            .height(32.dp)
            .padding(top = 3.dp, start = 8.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            emojiGroup.forEach {
                val imogiScale = remember { Animatable(1f) }
                var isPress by remember { mutableStateOf(false) }
                LaunchedEffect(isPress) {
                    if (isPress)
                        imogiScale.animateTo(0.85f, animationSpec = tween(100))
                    else
                        imogiScale.animateTo(1f, animationSpec = tween(100))
                }
                Box(
                    modifier = Modifier
                        .pointerInteropFilter { event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    isPress = true
                                    onImogiClick(it)
                                    true
                                }

                                MotionEvent.ACTION_UP -> {
                                    isPress = false
                                    true
                                }

                                else -> false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .padding(3.dp)
                            .scale(imogiScale.value),
                        text = "$it"
                    )
                }
            }
        }

        SlideAnimation(
            modifier = Modifier,
            visible = !isEmojiGroup,
            direction = AnimationDirection.RightCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectTapGestures()
                    }, contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = oneLinePreview,
                    fontFamily = hancomMalangFontFamily,
                    fontSize = 13.sp
                )
            }
        }
    }

}


@Composable
fun PopUpImage(modifier: Modifier, uri: Uri?) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp),
        contentAlignment = Alignment.Center
    ) {
        FadeAnimation(visible= uri != null) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { uri },
                imageOptions = ImageOptions(contentScale = ContentScale.FillHeight)
            )
        }
        FadeAnimation(visible = uri == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White), contentAlignment = Alignment.Center) {
                DelayLottieAnimation(Modifier.size(50.dp),R.raw.lt_loading,true,0)
            }
        }
    }

}
