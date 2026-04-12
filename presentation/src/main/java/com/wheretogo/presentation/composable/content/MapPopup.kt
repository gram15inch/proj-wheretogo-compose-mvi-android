package com.wheretogo.presentation.composable.content

import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.dummy.getCommentDummy
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.presentation.CommentType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SheetVisibleMode
import com.wheretogo.presentation.WIDE_WIDTH
import com.wheretogo.presentation.defaultCommentEmogiGroup
import com.wheretogo.presentation.feature.BlurEffect
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.topShadow
import com.wheretogo.presentation.model.SlideItem
import com.wheretogo.presentation.model.TypeEditText
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.state.PopUpState
import com.wheretogo.presentation.theme.Gray5050
import com.wheretogo.presentation.theme.Gray6080
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
    state: PopUpState = PopUpState(),
    isLoading: Boolean,
    isImageBlur : Boolean,
    requestCommentOpen: () -> Unit,
    onPopupBlurClick: () -> Unit,
    onPopupSlide: (index: Int) -> Unit,
    onCommentSheetStateChange: (SheetVisibleMode)-> Unit,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (Comment) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit,
    onCommentRemoveClick: (Comment) -> Unit,
    onCommentReportClick: (Comment, ReportReason) -> Unit,
    onCommentAddClick: (String) -> Unit,
    onCommentEmogiPress: (String) -> Unit,
    onCommentTypePress: (TypeEditText) -> Unit
) {
    val isWideSize = screenSize(true) >= WIDE_WIDTH.dp
    val coroutineScope = rememberCoroutineScope()

    // 넒은화면시 댓글창 오픈으로 표시하기위해 사용
    LaunchedEffect(isWideSize) {
        if (isWideSize) {
            coroutineScope.launch {
                delay(250)
                if (!state.commentState.isContentVisible && !isLoading)
                    requestCommentOpen()
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        ExtendArea( // 넓은 화면에서 확장
            isExtend = isWideSize,
            paddingHorizontalWhenExtend = 40.dp,
            holdContent = {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    PopUpImageSlide(
                        modifier = Modifier.consumptionEvent(true),
                        slideItems = state.slideItems,
                        initPage = state.initPage,
                        isBlur = isImageBlur || (state.commentState.isContentVisible && !isWideSize),
                        onPopupSlide = onPopupSlide,
                        onPopupBlurClick = onPopupBlurClick
                    )
                }
            },
            moveContent = { // 이동
                val maxHeight = (if (isWideSize) 500 else 460)
                Column(modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .sizeIn(maxHeight = maxHeight.dp)
                ) {
                    CommentDragSheet(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = state.commentState,
                        onCommentListItemClick = onCommentListItemClick,
                        onCommentListItemLongClick = onCommentListItemLongClick,
                        onCommentLikeClick = onCommentLikeClick,
                        onCommentRemoveClick = onCommentRemoveClick,
                        onCommentReportClick = onCommentReportClick,
                        onSheetStateChange = onCommentSheetStateChange
                    )
                    if(state.commentState.isImeVisible)
                        CommentImeStickyBox(
                            modifier = Modifier
                                .consumptionEvent(true)
                                .fillMaxWidth(),
                            state = state.commentState,
                            onCommentAddClick = onCommentAddClick,
                            onCommentEmogiPress = onCommentEmogiPress,
                            onCommentTypePress = onCommentTypePress,
                            onBoxHeightChange = { _ -> }
                        )
                }

            }
        )
    }
}

@Composable
fun CommentDragSheet(
    modifier: Modifier = Modifier,
    state: CommentState,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (Comment) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit,
    onCommentRemoveClick: (Comment) -> Unit,
    onCommentReportClick: (Comment, ReportReason) -> Unit,
    onSheetStateChange: (SheetVisibleMode) -> Unit
) {
    val isPreview = LocalInspectionMode.current
    Box(modifier = modifier) {
        BottomSheet(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(maxHeight = 500.dp),
            isOpen = state.isContentVisible,
            minHeight = if (isPreview) 400.dp else 0.dp,
            onSheetStateChange = onSheetStateChange,
            onSheetHeightChange = {},
            isSpaceVisibleWhenClose = false,
            dragHandleColor = if(state.isDragGuide) Gray6080 else null
        ) {
            Box{
                if(state.isDragGuide){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(999f)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Gray6080, Gray5050)
                                )
                            ),
                    ) {
                        DelayLottieAnimation(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.TopCenter),
                            ltRes = R.raw.lt_arrow_down,
                            isVisible = true,
                            max = 1f
                        )
                    }
                }

                CommentContent(
                    modifier = Modifier
                        .height(500.dp)
                        .consumptionEvent(true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(White),
                    commentState = state,
                    onCommentListItemClick = onCommentListItemClick,
                    onCommentListItemLongClick = onCommentListItemLongClick,
                    onCommentLikeClick = onCommentLikeClick,
                )
            }
        }

        FadeAnimation(
            modifier = Modifier.clip(RoundedCornerShape(topStart = 28.5.dp, topEnd = 28.5.dp)),
            visible = state.commentSettingState.isVisible
        ) {
            CommentSetting(
                state = state.commentSettingState,
                onCommentRemoveClick = onCommentRemoveClick,
                onCommentReportClick = onCommentReportClick,
                onBackgroundClick = {
                    onCommentListItemLongClick(state.commentSettingState.comment)
                })
        }
    }
}

@Composable
fun CommentContent(
    modifier: Modifier,
    commentState: CommentState,
    onCommentListItemClick: (CommentItemState) -> Unit,
    onCommentListItemLongClick: (Comment) -> Unit,
    onCommentLikeClick: (CommentItemState) -> Unit
) {
    Box(modifier = modifier) {
        CommentList(
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
    onCommentAddClick: (String) -> Unit,
    onCommentEmogiPress: (String) -> Unit,
    onCommentTypePress: (TypeEditText) -> Unit,
    onBoxHeightChange: (Dp) -> Unit
) {
    ImeStickyBox(modifier = modifier, onBoxHeightChange = onBoxHeightChange) { imeHeight ->
        var textState by remember {
            mutableStateOf(TextFieldValue())
        }

        LaunchedEffect(imeHeight == 0.dp) {
            if (imeHeight == 0.dp)
                textState = TextFieldValue(text = "")
        }

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
                onReviewButtonClick = { type ->
                    onCommentTypePress(
                        TypeEditText(
                            type,
                            textState.text
                        )
                    )

                    textState = if (type == CommentType.DETAIL) {
                        state.commentAddState.detailReview.run {
                            TextFieldValue(this, selection = TextRange(length))
                        }
                    } else {
                        state.commentAddState.oneLineReview.run {
                            TextFieldValue(this, selection = TextRange(length))
                        }
                    }
                }
            )

            // 이모지
            CommentEmojiGroupAndOneLinePreview(
                emojiGroup = state.commentAddState.emogiGroup,
                titleEmogi = state.commentAddState.titleEmoji,
                isOneLinePreview = state.commentAddState.isOneLinePreview,
                oneLineText = state.commentAddState.oneLineReview,
                onEmogiClick = onCommentEmogiPress
            )

            // 입력 텍스트
            CommentTextField(
                editText = textState,
                isEmoji = !state.commentAddState.isOneLinePreview,
                emoji = state.commentAddState.titleEmoji.ifEmpty {
                    state.commentAddState.emogiGroup.firstOrNull() ?: ""
                },
                isLoading = state.commentAddState.isLoading,
                onValueChange = { text ->
                    textState = text
                },
                onDone = {
                    onCommentAddClick(textState.text)
                    textState = TextFieldValue("")
                }
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
    emojiGroup: List<String>,
    titleEmogi: String,
    isOneLinePreview: Boolean,
    oneLineText: String,
    onEmogiClick: (String) -> Unit
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
                                    onEmogiClick(it)
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
            visible = isOneLinePreview,
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
                    text = "${titleEmogi.ifEmpty { emojiGroup.firstOrNull() ?: "" }}  $oneLineText",
                    fontFamily = hancomMalangFontFamily,
                    fontSize = 13.sp
                )
            }
        }
    }

}

@Composable
fun PopUpImageSlide(
    modifier: Modifier,
    slideItems: List<SlideItem>,
    initPage: Int? = null,
    isBlur: Boolean,
    onPopupBlurClick: () -> Unit,
    onPopupSlide: (index: Int) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .sizeIn(maxWidth = 260.dp, maxHeight = 500.dp),
        contentAlignment = Alignment.Center
    ) {
        if (initPage != null) {
            val pagerState = rememberPagerState(initPage, pageCount = { slideItems.size })

            LaunchedEffect(pagerState.currentPage) {
                onPopupSlide(pagerState.currentPage)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val item = slideItems.getOrNull(page)
                val url = item?.url
                when {
                    url != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmer()
                                .background(White)
                        )
                    }
                }
            }

            FadeAnimation(visible = isBlur) {
                BlurEffect(onClick = { onPopupBlurClick() })
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SlideIndicator(
                    currentIndex = pagerState.currentPage,
                    totalCount = slideItems.size,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val item = slideItems.getOrNull(pagerState.currentPage)
                ImageCation(
                    modifier = Modifier.fillMaxWidth(),
                    title = item?.title,
                    subTitle = item?.subtitle
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ImageCation(
    modifier: Modifier = Modifier,
    title: String?,
    subTitle: String?
) {
    Box(
        modifier = modifier
            .sizeIn(minHeight = 50.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            title?.let {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            subTitle?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SlideIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isActive = index == currentIndex
            val width by animateDpAsState(
                targetValue = if (isActive) 18.dp else 6.dp,
                animationSpec = tween(durationMillis = 250),
                label = "dot_width"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Color.White
                        else Color.White.copy(alpha = 0.4f)
                    )
            )
        }
    }
}

val previewItems = listOf(
    SlideItem(title = "설악산 대청봉 일출", subtitle = "강원도 속초시 · 해발 1,708m"),
    SlideItem(title = "지리산 천왕봉", subtitle = "경남 산청군 · 해발 1,915m"),
    SlideItem(title = "한라산 백록담", subtitle = "제주특별자치도 · 해발 1,947m"),
)

@Preview(widthDp = 800, heightDp = 900)
@Preview(widthDp = 400, heightDp = 600)
@Composable
fun PopupPreview() {
    Box(modifier = Modifier.background(Green50)) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            state = PopUpState(
                slideItems = previewItems,
                initPage = 1,
                commentState = CommentState(
                    isContentVisible = true,
                    isDragGuide = true,
                    isImeVisible = true,
                    commentItemGroup = getCommentDummy().mapIndexed { idx, item ->
                        val comment = item.copy(
                            isUserLiked = if (item.like % 2 == 0) false else true,
                            isFocus = idx == 0
                        )
                        CommentItemState(
                            data = comment,
                            isFold = if (comment.like % 2 == 0) true else false,
                        )
                    },
                    commentAddState = CommentAddState(
                        isOneLinePreview = false,
                        isLoading = false,
                        emogiGroup = defaultCommentEmogiGroup()
                    ),
                    commentSettingState = CommentState.CommentSettingState()
                )
            ),
            isLoading = false,
            isImageBlur = false,
            requestCommentOpen = {},
            onPopupBlurClick = {},
            onCommentListItemClick = {},
            onCommentListItemLongClick = {},
            onCommentLikeClick = {},
            onCommentRemoveClick = {},
            onCommentReportClick = {_,_->},
            onCommentAddClick = {},
            onCommentEmogiPress = {},
            onCommentTypePress = {},
            onCommentSheetStateChange = {},
            onPopupSlide = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PopUpImagePreview() {
    PopUpImageSlide(
        modifier = Modifier,
        slideItems = previewItems,
        isBlur = false,
        onPopupBlurClick = {},
        onPopupSlide = {}
    )
}