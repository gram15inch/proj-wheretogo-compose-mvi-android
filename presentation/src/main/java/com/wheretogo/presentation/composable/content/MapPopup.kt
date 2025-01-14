package com.wheretogo.presentation.composable.content

import android.net.Uri
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.dummy.getCommentDummy
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.BlurEffect
import com.wheretogo.presentation.composable.ExtendArea
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.topShadow
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentItemState
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily


@Preview
@Composable
fun PopupPreview() {
    Box(modifier = Modifier) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            CommentState(
                isCommentSettingVisible = false,
                isCommentVisible = true,
                commentItemGroup = getCommentDummy().map {
                    CommentItemState(
                        data = it,
                        isFold = if (it.like % 2 == 0) true else false,
                        isLike = if (it.like % 2 == 0) false else true
                    )
                },
                commentAddState = CommentAddState(
                    isEmogiGroup = true,
                    emogiGroup = getEmogiDummy()
                )
            ),
            imageUri = Uri.parse(""),
            isWideSize = false,
            onPopupImageClick = {},
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
    onPopupImageClick: () -> Unit,
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
                            onPopupImageClick()
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorResource(R.color.white))
                    ) {
                        Column {
                            val maxHeight =
                                (if (!isWideSize) 480.dp else 500.dp) - imeContainerHeight
                            if (commentState.commentItemGroup.isNotEmpty()) {
                                PopupCommentList(
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
                                    onCommentListItemLongClick(commentState.selectedCommentSettingItem)//todo 실제값변경
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
fun CommentSetting(
    modifier: Modifier = Modifier,
    selectedItem: CommentItemState,
    onCommentRemoveClick: (CommentItemState) -> Unit,
    onCommentReportClick: (CommentItemState) -> Unit,
    onBackgroundClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    onBackgroundClick()
                }
            )
            .background(colorResource(R.color.gray_C7C7C7_90)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.White),
        ) {

            if (selectedItem.isUserCreated) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            onCommentRemoveClick(selectedItem)
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(text = "삭제", fontSize = 16.sp, fontFamily = hancomSansFontFamily)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        onCommentReportClick(selectedItem)
                    }, contentAlignment = Alignment.Center
            ) {
                Text(text = "신고", fontSize = 16.sp, fontFamily = hancomSansFontFamily)
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
            .background(Color.Black)
            .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null)
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { uri },
                imageOptions = ImageOptions(contentScale = ContentScale.FillHeight)
            )
        else
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    modifier = Modifier.size(70.dp),
                    painter = painterResource(R.drawable.ic_picture),
                    contentDescription = ""
                )
            }
    }

}

@Composable
fun PopupCommentList(
    modifier: Modifier,
    isCompact: Boolean,
    commentItemGroup: List<CommentItemState>,
    onItemClick: (CommentItemState) -> Unit,
    onItemLongClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            if (commentItemGroup.isNotEmpty()) {
                val max = commentItemGroup.maxBy { it.data.like }
                item {
                    CommentFocusItem(
                        comment = max,
                        onItemLongClick = { item ->
                            onItemLongClick(item)
                        },
                        onLikeClick = { item ->
                            onLikeClick(item)
                        }
                    )
                }
                items(commentItemGroup.filter { it.data.commentId != max.data.commentId }) { item ->
                    CommentListItem(
                        comment = item,
                        onItemClick = {
                            onItemClick(it)
                        },
                        onItemLongClick = {
                            onItemLongClick(it)
                        },
                        onLikeClick = {
                            onLikeClick(it)
                        }
                    )
                }
            }
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentListItem(
    modifier: Modifier = Modifier,
    comment: CommentItemState,
    onItemClick: (CommentItemState) -> Unit,
    onItemLongClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {
                onItemClick(comment)
            }, onLongClick = {
                onItemLongClick(comment)
            })
            .padding(start = 10.dp, end = 10.dp, top = 4.dp)
    ) {
        Column {
            Row {
                Column {
                    Text(
                        modifier = Modifier.padding(),
                        text = comment.data.emoji,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontFamily = hancomSansFontFamily
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = comment.data.oneLineReview,
                        fontSize = 13.sp,
                        fontFamily = hancomMalangFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!comment.isFold)
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = comment.data.detailedReview,
                            fontSize = 10.sp,
                            fontFamily = hancomSansFontFamily,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                lineHeight = 16.sp
                            )
                        )
                    else
                        Text(
                            modifier = Modifier.padding(top = 4.dp, start = 1.dp),
                            text = "접힘 >",
                            style = TextStyle(
                                fontSize = 9.sp,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ),
                            fontFamily = hancomSansFontFamily,
                            color = colorResource(R.color.gray_848484)
                        )
                }
                Column(modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onLikeClick(comment)
                            }
                    ) {
                        Image(
                            modifier = Modifier
                                .size(22.dp)
                                .padding(5.dp),
                            painter = painterResource(if (comment.isLike) R.drawable.ic_heart else R.drawable.ic_heart),
                            contentDescription = "",
                        )
                    }
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(start = 0.dp),
                        text = "${comment.data.like}",
                        fontSize = 10.sp,
                    )
                }
            }
            Text(
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .padding(start = 5.dp),
                text = comment.data.userName.ifEmpty { "익명의드라이버" },
                fontSize = 9.sp,
                fontFamily = hancomSansFontFamily
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentFocusItem(
    modifier: Modifier = Modifier,
    comment: CommentItemState,
    onItemLongClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    onItemLongClick(comment)
                }
            )
            .padding(10.dp)
    ) {
        Column(
            modifier
                .fillMaxWidth()
        ) {
            Row {
                Text(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    text = comment.data.emoji,
                    textAlign = TextAlign.Center,
                    fontSize = 34.sp
                )

                Column {
                    Text(
                        text = comment.data.oneLineReview,
                        fontSize = 14.sp,
                        fontFamily = hancomSansFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = comment.data.detailedReview,
                        fontSize = 10.sp,
                        fontFamily = hancomSansFontFamily,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            lineHeight = 16.sp
                        )
                    )
                }
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                Text(
                    modifier = Modifier
                        .align(alignment = Alignment.CenterVertically)
                        .padding(start = 5.dp),
                    text = comment.data.userName.ifEmpty { "익명의드라이버" },
                    fontSize = 9.sp,
                    fontFamily = hancomSansFontFamily
                )
                Row(
                    modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onLikeClick(comment)
                        }) {
                    Image(
                        modifier = Modifier
                            .size(22.dp)
                            .padding(5.dp)
                            .align(alignment = Alignment.CenterVertically),
                        painter = painterResource(if (comment.isLike) R.drawable.ic_heart else R.drawable.ic_heart),
                        contentDescription = "",
                    )
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(start = 0.dp),
                        text = "${comment.data.like}",
                        fontSize = 10.sp
                    )
                }
            }
        }

    }
}

@Composable
fun CommentInput(
    modifier: Modifier = Modifier,
    comment: CommentItemState,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            //.clickable { onItemClick(comment) }
            .padding(10.dp)
    ) {
        Column {
            Row {
                Text(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    text = comment.data.emoji,
                    textAlign = TextAlign.Center,
                    fontSize = 34.sp
                )

                Column {
                    Text(
                        text = comment.data.oneLineReview,
                        fontSize = 14.sp,
                        fontFamily = hancomSansFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = comment.data.detailedReview,
                        fontSize = 10.sp,
                        fontFamily = hancomSansFontFamily,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
