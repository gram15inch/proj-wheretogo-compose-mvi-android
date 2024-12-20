package com.wheretogo.presentation.composable.content

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.dummy.getCommentDummy
import com.wheretogo.domain.model.dummy.getEmogiDummy
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.BlurEffect
import com.wheretogo.presentation.composable.ExtendArea
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentAddState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentItemState
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily
import kotlin.math.max


@Preview
@Composable
fun PopupPreview() {
    Box(modifier = Modifier) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            CommentState(
                getCommentDummy().map {
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
            imageUrl = "",
            isWideSize = false,
            isCommentVisible = true,
            onCommentFloatingButtonClick = {},
            onCommentListItemClick = {},
            onCommentLikeClick = {},
            onCommentAddClick = {},
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
    imageUrl: String,
    isWideSize: Boolean,
    isCommentVisible: Boolean,
    onCommentFloatingButtonClick: () -> Unit,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit,
    onCommentAddClick: (CommentAddState) -> Unit,
    onCommentEditValueChange: (TextFieldValue) -> Unit,
    onCommentEmogiPress: (String) -> Unit,
    onCommentTypePress: (CommentType) -> Unit,
) {
    ExtendArea(
        isExtend = isWideSize,
        holdContent = {
            PopUpImage(
                modifier = modifier.clickable {
                    onCommentFloatingButtonClick()
                },
                url = imageUrl
            )
        },
        moveContent = {
            FadeAnimation(visible = isCommentVisible && !isWideSize) {
                BlurEffect(
                    modifier = Modifier
                        .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    onClick = {
                        onCommentFloatingButtonClick()
                    })
            }
            SlideAnimation(
                modifier = modifier
                    .graphicsLayer(clip = true),
                visible = isCommentVisible,
                direction = if (isWideSize) AnimationDirection.CenterRight else AnimationDirection.CenterDown
            ) {
                Box(
                    modifier = Modifier
                        .sizeIn(maxWidth = 260.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    var containerHeight by remember { mutableStateOf(0.dp) }

                    Column {
                        PopupCommentList(
                            modifier = Modifier
                                .sizeIn(maxHeight = (if (!isWideSize) 480.dp else 500.dp) - containerHeight),
                            isCompact = !isWideSize,
                            commentItemGroup = commentState.commentItemGroup,
                            onLongItemClick = { item ->
                                onCommentListItemClick(item)
                            },
                            onLikeClick = { item ->
                                onCommentLikeClick(item)
                            }
                        )
                        Spacer(modifier.height(containerHeight))
                    }

                    ImeStickyBox(
                        modifier = Modifier
                            .align(alignment = Alignment.BottomCenter),
                        onContainerHeightChange = { height ->
                            containerHeight = height
                        }) { imeHeight ->
                        Column {// 리뷰버튼
                            ReviewButtonGroup(
                                modifier = Modifier.run {
                                    if (imeHeight > 100.dp) this
                                    else this.height(0.dp)
                                },
                                selectedType = commentState.commentAddState.commentType,
                                onReviewButtonClick = onCommentTypePress
                            )

                            // 이모지
                            CommentEmojiGroupAndOneLinePreivew(
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
    )
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun ImeStickyBox(
    modifier: Modifier,
    onContainerHeightChange: (Dp) -> Unit,
    content: @Composable (Dp) -> Unit
) {
    val context = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeHeight =
        max(0f, (imeInsets.getBottom(context) / context.density) - 60).dp // 알수없는 키보드 마진 조정 (-60.dp)
    Box(modifier = modifier
        .width(260.dp)
        .wrapContentHeight()
        .offset(y = -imeHeight)
        .graphicsLayer {
            shadowElevation = 8.dp.toPx() // 그림자 높이
            shape = RectangleShape // 그림자 모양
            clip = false
        }
        .background(Color.White)
        .onGloballyPositioned { layoutCoordinates ->
            onContainerHeightChange(with(context) {
                layoutCoordinates.size.height.toDp()
            })
        }
    ) {
        content(imeHeight)
    }
}

@Composable
fun ReviewButtonGroup(modifier: Modifier, selectedType:CommentType, onReviewButtonClick: (CommentType) -> Unit) {
    Row(
        modifier = modifier.padding(top = 0.dp, start = 8.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReviewButton(
            type = CommentType.ONE,
            selectedType=selectedType,
            color = R.color.teal_200,
            onReviewButtonClick = onReviewButtonClick
        )
        ReviewButton(
            type = CommentType.DETAIL,
            selectedType=selectedType,
            color = R.color.purple_200,
            onReviewButtonClick = onReviewButtonClick
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReviewButton(
    type: CommentType,
    selectedType:CommentType,
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
                        if (selectedType != type)
                            onReviewButtonClick(type)
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
fun CommentEmojiGroupAndOneLinePreivew(
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
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures()
                }, contentAlignment = Alignment.CenterStart) {
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
fun CommentTextField(
    editText: TextFieldValue,
    isEmoji: Boolean,
    emoji: String,
    onValueChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
) {
    Row {
        Box(
            modifier = Modifier
                .run { if (isEmoji) this else this.width(0.dp) }
                .height(50.dp)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                modifier = Modifier,
                text = emoji,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 28.sp,
                    lineHeight = 28.sp
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(30.dp))
                    .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                val keyboard = LocalSoftwareKeyboardController.current
                val focuse = LocalFocusManager.current
                var isDone by remember { mutableStateOf(false) }
                BasicTextField(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    value = editText,
                    onValueChange = { newText ->
                        if (!isDone) // 키보드 완료시 업데이트 막기
                            onValueChange(newText)
                        else
                            isDone=false
                    },
                    cursorBrush = SolidColor(Color.Black),
                    maxLines = Int.MAX_VALUE,
                    textStyle = TextStyle(
                        fontSize = 11.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboard?.hide()
                            focuse.clearFocus()
                            isDone = true
                            onDone()
                        }
                    ),
                )
            }
        }
    }
}


@Composable
fun PopUpImage(modifier: Modifier, url: String) {
    GlideImage(modifier = modifier
        .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp)
        .clip(RoundedCornerShape(16.dp)),
        imageModel = { url }
    )
}

@Composable
fun PopupCommentList(
    modifier: Modifier,
    isCompact: Boolean,
    commentItemGroup: List<CommentItemState>,
    onLongItemClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(
        modifier = modifier
            .background(colorResource(R.color.white))
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
                        onItemClick = { item ->
                            onLongItemClick(item)
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
                            onLongItemClick(it)
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

@Composable
fun CommentListItem(
    modifier: Modifier = Modifier,
    comment: CommentItemState,
    onItemClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .clickable {
            onItemClick(comment)
        }
        .padding(start = 10.dp, end = 10.dp, top = 4.dp)) {
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
                            painter = painterResource(if (comment.isLike) R.drawable.ic_heart_red else R.drawable.ic_heart_bk),
                            contentDescription = "",
                        )
                    }
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(start = 0.dp),
                        text = "${comment.data.like + if (comment.isLike) 1 else 0}",
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

@Composable
fun CommentFocusItem(
    modifier: Modifier = Modifier,
    comment: CommentItemState,
    onItemClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
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
                        painter = painterResource(if (comment.isLike) R.drawable.ic_heart_red else R.drawable.ic_heart_bk),
                        contentDescription = "",
                    )
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .padding(start = 0.dp),
                        text = "${comment.data.like + if (comment.isLike) 1 else 0}",
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
