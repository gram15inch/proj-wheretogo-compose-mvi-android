package com.wheretogo.presentation.composable.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.report.ReportReason
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.toDisplayName
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.theme.Gray250
import com.wheretogo.presentation.theme.Gray8090
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily

@Composable
fun CommentList(
    modifier: Modifier = Modifier,
    commentItemGroup: List<CommentItemState>?,
    onItemClick: (CommentItemState) -> Unit,
    onItemLongClick: (Comment) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if(commentItemGroup == null){
            Box(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                DelayLottieAnimation(Modifier.width(100.dp), R.raw.lt_loading, true,450,max = 1f)
            }
        }

        if(commentItemGroup.isNullOrEmpty()){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.comment_hint),
                    fontFamily = hancomMalangFontFamily,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
                val focusItem = if (commentItemGroup.isNotEmpty()) {
                    val temp = commentItemGroup.firstOrNull { it.data.isFocus }
                    temp ?: commentItemGroup[0]
                } else null
                if (focusItem != null) {
                    item {
                        CommentFocusItem(
                            modifier= Modifier.animateItem(),
                            comment = focusItem,
                            onItemLongClick = { item ->
                                onItemLongClick(item.data)
                            },
                            onLikeClick = { item ->
                                onLikeClick(item)
                            }
                        )
                    }
                    val sortedCommentGroup =
                        commentItemGroup.filter { it.data.commentId != focusItem.data.commentId }
                            .sortedWith(
                                compareBy<CommentItemState> { it.data.isUserCreated }
                                    .thenByDescending { it.data.createAt }
                            )
                    items(sortedCommentGroup) { item ->
                        CommentListItem(
                            modifier= Modifier.animateItem(),
                            comment = item,
                            onItemClick = {
                                onItemClick(it)
                            },
                            onItemLongClick = {
                                onItemLongClick(it.data)
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
                    if (comment.isFold)
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
                            color = Gray250
                        )
                    else
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
                Column(modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(!comment.isLoading) {
                                onLikeClick(comment)
                            }
                    ) {
                        Image(
                            modifier = Modifier
                                .size(22.dp)
                                .padding(5.dp),
                            painter = painterResource(if (comment.data.isUserLiked) R.drawable.ic_heart_fill else R.drawable.ic_heart_line),
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
                    fontFamily = hancomSansFontFamily,
                    fontSize = 34.sp
                )

                Column {
                    Text(
                        text = comment.data.oneLineReview,
                        fontSize = 14.sp,
                        fontFamily = hancomMalangFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = comment.data.detailedReview,
                        fontSize = 10.sp,
                        fontFamily = hancomMalangFontFamily,
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
                        .clickable(!comment.isLoading) {
                            onLikeClick(comment)
                        }) {
                    Image(
                        modifier = Modifier
                            .size(22.dp)
                            .padding(5.dp)
                            .align(alignment = Alignment.CenterVertically),
                        painter = painterResource(if (comment.data.isUserLiked) R.drawable.ic_heart_fill else R.drawable.ic_heart_line),
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
fun CommentSetting(
    modifier: Modifier = Modifier,
    state: CommentState.CommentSettingState,
    onCommentRemoveClick: (Comment) -> Unit,
    onCommentReportClick: (Comment, ReportReason) -> Unit,
    onBackgroundClick: () -> Unit
) {
    var showReportOptions by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onBackgroundClick() }
            )
            .background(Gray8090),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.White),
        ) {
            // 삭제 or 신고 버튼
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    DelayLottieAnimation(Modifier.size(30.dp), R.raw.lt_loading, true, 0)
                } else {
                    if (state.comment.isUserCreated) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onCommentRemoveClick(state.comment) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "삭제", fontSize = 16.sp, fontFamily = hancomSansFontFamily)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showReportOptions = !showReportOptions },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "신고", fontSize = 16.sp, fontFamily = hancomSansFontFamily)
                                Icon(
                                    imageVector = if (showReportOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 신고 사유 목록
            AnimatedVisibility(
                visible = showReportOptions,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    ReportReason.entries.forEachIndexed { index, reason ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable {
                                    showReportOptions = false
                                    onCommentReportClick(state.comment, reason)
                                }
                                .padding(start = 32.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = reason.toDisplayName(),
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                fontFamily = hancomSansFontFamily
                            )
                        }
                        if (index < ReportReason.entries.lastIndex)
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 500, heightDp = 500)
@Composable
fun CommentSettingPreview() {
    CommentSetting(
        modifier = Modifier.fillMaxSize(),
        state = CommentState.CommentSettingState(
            isVisible = true,
        ),
        {},
        {_,_->},
        {}
    )
}