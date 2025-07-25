package com.wheretogo.presentation.composable.content

import android.net.Uri
import android.view.MotionEvent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.valentinilk.shimmer.shimmer
import com.wheretogo.domain.model.dummy.getCommentDummy
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.SheetState
import com.wheretogo.presentation.WIDE_WIDTH
import com.wheretogo.presentation.composable.ExtendArea
import com.wheretogo.presentation.feature.BlurEffect
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.topShadow
import com.wheretogo.presentation.getCommentEmogiGroup
import com.wheretogo.presentation.state.BottomSheetState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.theme.Green50
import com.wheretogo.presentation.theme.Purple200
import com.wheretogo.presentation.theme.Teal200
import com.wheretogo.presentation.theme.White
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MapPopup(
    modifier: Modifier,
    state : PopUpState = PopUpState(),
    isLoading: Boolean,
    onPopupImageClick: () -> Unit,
    onPopupBlurClick: () -> Unit,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (CommentItemState) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit,
    onCommentRemoveClick: (CommentItemState) -> Unit,
    onCommentReportClick: (CommentItemState) -> Unit,
    onCommentAddClick: (CommentAddState) -> Unit,
    onCommentEditValueChange: (TextFieldValue) -> Unit,
    onCommentEmogiPress: (String) -> Unit,
    onCommentTypePress: (CommentType) -> Unit
) {
    var imeContainerHeight by remember { mutableStateOf(0.dp) }
    val isWideSize = screenSize(true) >= WIDE_WIDTH.dp
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.isVisible) {
        if(state.isVisible && isWideSize){
            coroutineScope.launch {
                delay(250)
                if(!state.commentState.isCommentVisible)
                    onPopupImageClick()
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        ExtendArea( // 넓은 화면에서 확장
            isExtend = isWideSize,
            holdContent = {
                Column(modifier=modifier) {
                    PopUpImage( // 고정
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
                        uri = state.imageUri,
                        isBlur = state.commentState.isCommentVisible && !isWideSize,
                        onPopupImageClick = {
                            if(!isLoading)
                                onPopupImageClick()
                        },
                        onPopupBlurClick = onPopupBlurClick
                    )
                    if(isWideSize)
                        Surface(modifier=Modifier.height(70.dp)) {  }
                }
            },
            moveContent = { // 이동
                val maxHeight = (if (isWideSize) 500 else 400)
                if(state.commentState.isCommentVisible) {
                    Column(modifier=Modifier.padding(bottom = 12.dp)) {
                        CommentDragSheet(
                            modifier=modifier.sizeIn(maxHeight=maxHeight.dp),
                            state = state.commentState,
                            onCommentListItemClick = onCommentListItemClick,
                            onCommentListItemLongClick = onCommentListItemLongClick,
                            onCommentLikeClick = onCommentLikeClick,
                            onCommentRemoveClick = onCommentRemoveClick,
                            onCommentReportClick = onCommentReportClick,
                            onPopupBlurClick = onPopupBlurClick
                        )
                        Surface(modifier=Modifier.height(70.dp)) {  }
                    }
                }
                else
                    Surface(modifier=Modifier.fillMaxWidth()) {  }
            }
        )
        val isImeVisible =
            if (!isWideSize) state.commentState.isCommentVisible
            else state.commentState.isCommentVisible && !state.commentState.isCommentSettingVisible
        SlideAnimation(
            visible = isImeVisible,
            direction = AnimationDirection.CenterDown
        ) {
            CommentImeStickyBox( modifier = Modifier
                .consumptionEvent()
                .padding(top = 1.dp)
                .fillMaxWidth()
                .align(alignment = Alignment.BottomCenter),
                state = state.commentState,
                onCommentAddClick=onCommentAddClick,
                onCommentEditValueChange=onCommentEditValueChange,
                onCommentEmogiPress=onCommentEmogiPress,
                onCommentTypePress=onCommentTypePress,
                onBoxHeightChange = { height ->
                    imeContainerHeight = height
                }
            )
        }
    }
}

@Composable
fun CommentDragSheet(
    modifier: Modifier = Modifier,
    state: CommentState,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (CommentItemState) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit,
    onCommentRemoveClick: (CommentItemState) -> Unit,
    onCommentReportClick: (CommentItemState) -> Unit,
    onPopupBlurClick: () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    Box(modifier=modifier){
        BottomSheet(
            modifier = Modifier
                .fillMaxWidth(),
            state = BottomSheetState(
                isVisible = true,
                minHeight = if(isPreview) 400 else 0
            ),
            onSheetStateChange = {
                if (it == SheetState.PartiallyExpand)
                    onPopupBlurClick()
            },
            onSheetHeightChange = {}
        ) {

        CommentContent(
                modifier = Modifier
                    .height(500.dp)
                    .consumptionEvent()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White),
                commentState = state,
                onCommentListItemClick = onCommentListItemClick,
                onCommentListItemLongClick = onCommentListItemLongClick,
                onCommentLikeClick = onCommentLikeClick,
            )
        }

        FadeAnimation(modifier= Modifier.clip(RoundedCornerShape(topStart = 28.5.dp, topEnd = 28.5.dp)),
            visible = state.isCommentSettingVisible) {
            CommentSetting(
                selectedItem = state.selectedCommentSettingItem,
                onCommentRemoveClick = onCommentRemoveClick,
                onCommentReportClick = onCommentReportClick,
                onBackgroundClick = {
                    onCommentListItemLongClick(state.selectedCommentSettingItem)
                })
        }
    }
}

@Composable
fun CommentContent(
    modifier: Modifier,
    commentState: CommentState,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (CommentItemState) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit
){
    Box(modifier = modifier){
        CommentList(
            isLoading = commentState.isLoading,
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
    }
}

@Composable
fun CommentImeStickyBox(
    modifier: Modifier,
    state: CommentState,
    onCommentAddClick: (CommentAddState) -> Unit,
    onCommentEditValueChange: (TextFieldValue) -> Unit,
    onCommentEmogiPress: (String) -> Unit,
    onCommentTypePress: (CommentType) -> Unit,
    onBoxHeightChange: (Dp) -> Unit
) {
    ImeStickyBox(modifier=modifier, onBoxHeightChange = onBoxHeightChange) { imeHeight ->
        Column(
            modifier = Modifier
                .topShadow()
                .background(Color.White)
        ) {
            // 리뷰버튼
            ReviewButtonGroup(
                modifier = Modifier.run {
                    if (imeHeight > 100.dp) this
                    else this.height(0.dp)
                },
                selectedType = state.commentAddState.commentType,
                onReviewButtonClick = onCommentTypePress
            )

            // 이모지
            CommentEmojiGroupAndOneLinePreview(
                isEmojiGroup = state.commentAddState.isEmogiGroup,
                emojiGroup = state.commentAddState.emogiGroup,
                oneLinePreview = state.commentAddState.oneLinePreview,
                onImogiClick = onCommentEmogiPress
            )

            // 입력 텍스트
            CommentTextField(
                editText = state.commentAddState.editText,
                isEmoji = state.commentAddState.isLargeEmogi,
                emoji = state.commentAddState.largeEmoji.ifEmpty {
                    state.commentAddState.emogiGroup.firstOrNull() ?: ""
                },
                onValueChange = onCommentEditValueChange,
                onDone = { onCommentAddClick(state.commentAddState) }
            )
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
            color = Teal200,
            onReviewButtonClick = onReviewButtonClick
        )
        ReviewButton(
            type = CommentType.DETAIL,
            selectedType = selectedType,
            color = Purple200,
            onReviewButtonClick = onReviewButtonClick
        )
    }
}

@Composable
fun ReviewButton(
    type: CommentType,
    selectedType: CommentType,
    color: Color,
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
            .background(color)
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
fun PopUpImage(
    modifier: Modifier,
    uri: Uri?,
    isBlur: Boolean,
    onPopupImageClick: () -> Unit,
    onPopupBlurClick: () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp),
        contentAlignment = Alignment.Center
    ) {

        if (!isPreview && uri!= null){
            GlideImage(
                modifier = Modifier
                    .clickable {
                        onPopupImageClick()
                    }
                    .fillMaxSize(),
                imageModel = { uri },
                imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                )
        } else {
            Box(modifier=Modifier
                .fillMaxSize()
                .shimmer()
                .background(White)
                .consumptionEvent()
            ){}
        }

        FadeAnimation(visible = isBlur) {
            BlurEffect(
                onClick = {
                    onPopupBlurClick()
                })
        }
    }

}


@Preview(widthDp = 600, heightDp = 600)
@Preview(widthDp = 400, heightDp = 600)
@Composable
fun PopupPreview() {
    Box(modifier = Modifier.background(Green50)) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            state = PopUpState(
                commentState = CommentState(
                    isCommentVisible = true,
                    isCommentSettingVisible = false,
                    commentItemGroup = getCommentDummy().mapIndexed { idx, item ->
                        CommentItemState(
                            data = item,
                            isLike = if (item.like % 2 == 0) false else true,
                            isFold = if (item.like % 2 == 0) true else false,
                            isFocus = idx == 0
                        )
                    },
                    commentAddState = CommentAddState(
                        isEmogiGroup = true,
                        emogiGroup = getCommentEmogiGroup()
                    )
                ),
                imageUri = Uri.parse(""),
            ),
            isLoading = false,
            onPopupImageClick = {},
            onPopupBlurClick = {},
            onCommentListItemClick = {},
            onCommentListItemLongClick = {},
            onCommentLikeClick = {},
            onCommentRemoveClick = {},
            onCommentReportClick = {},
            onCommentAddClick = {},
            onCommentEditValueChange = {},
            onCommentEmogiPress = {},
            onCommentTypePress = {}
        )
    }
}