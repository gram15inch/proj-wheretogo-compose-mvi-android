package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.state.CommentState.CommentItemState
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily

@Composable
fun CommentList(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    commentItemGroup: List<CommentItemState>,
    onItemClick: (CommentItemState) -> Unit,
    onItemLongClick: (CommentItemState) -> Unit,
    onLikeClick: (CommentItemState) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        if (commentItemGroup.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading)
                    DelayLottieAnimation(Modifier.width(50.dp), R.raw.lt_loading, true, 0)
                else
                    Text(
                        text = "첫 발자국을 남겨보세요.",
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
                val focusItem = commentItemGroup.firstOrNull { it.isFocus }
                if (focusItem != null) {
                    item {
                        CommentFocusItem(
                            comment = focusItem,
                            onItemLongClick = { item ->
                                onItemLongClick(item)
                            },
                            onLikeClick = { item ->
                                onLikeClick(item)
                            }
                        )
                    }
                    items(commentItemGroup.filter { it.data.commentId != focusItem.data.commentId }) { item ->
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
                            painter = painterResource(if (comment.isLike) R.drawable.ic_heart_fill else R.drawable.ic_heart_line),
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
                        .clickable {
                            onLikeClick(comment)
                        }) {
                    Image(
                        modifier = Modifier
                            .size(22.dp)
                            .padding(5.dp)
                            .align(alignment = Alignment.CenterVertically),
                        painter = painterResource(if (comment.isLike) R.drawable.ic_heart_fill else R.drawable.ic_heart_line),
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
            } else {
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
}