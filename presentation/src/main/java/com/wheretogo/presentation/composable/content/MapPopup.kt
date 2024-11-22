package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.BlurEffect
import com.wheretogo.presentation.composable.ExtendArea
import com.wheretogo.presentation.model.getCommentDummy
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily


@Preview
@Composable
fun PopupPreview() {
    Box(modifier = Modifier) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            getCommentDummy(),
            imageUrl = "",
            isWideSize = false,
            isCommentVisible = true,
            onCommentFloatingButtonClick = {},
            onCommentListItemClick = {},
            onCommentLikeClick = {}
        )
    }
}


@Composable
fun MapPopup(
    modifier: Modifier,
    data: List<Comment>,
    imageUrl: String,
    isWideSize: Boolean,
    isCommentVisible: Boolean,
    onCommentFloatingButtonClick: () -> Unit,
    onCommentListItemClick: (Comment) -> Unit,
    onCommentLikeClick: (Comment) -> Unit
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
                PopupCommentList(
                    modifier = Modifier,
                    isCompact = !isWideSize,
                    data,
                    onLongItemClick = { item ->
                        onCommentListItemClick(item)
                    },
                    onLikeClick = { item ->
                        onCommentLikeClick(item)
                    }
                )
            }
        }
    )
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
    data: List<Comment>,
    onLongItemClick: (Comment) -> Unit,
    onLikeClick: (Comment) -> Unit
) {
    Box(
        modifier = modifier
            .sizeIn(maxWidth = 260.dp, maxHeight = if (isCompact) 350.dp else 500.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.white))
            .fillMaxSize(),
    ) {

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(2.dp)) {
            item {
                CommentFocusItem(
                    comment = data.maxBy { it.like },
                    onItemClick = { item ->
                        onLongItemClick(item)
                    },
                    onLikeClick = { item ->
                        onLikeClick(item)
                    }
                )
            }
            items(data) { item ->
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

@Composable
fun CommentListItem(
    modifier: Modifier = Modifier,
    comment: Comment,
    onItemClick: (Comment) -> Unit,
    onLikeClick: (Comment) -> Unit
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .clickable {
            onItemClick(comment)
        }
        .padding(10.dp)) {
        Row {
            Column {
                Text(
                    modifier = Modifier.padding(),
                    text = comment.imoge,
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
                    text = comment.singleLineReview,
                    fontSize = 13.sp,
                    fontFamily = hancomMalangFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!comment.isFold)
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = comment.detailedReview,
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
                Text(
                    modifier = Modifier
                        .align(alignment = Alignment.End)
                        .padding(start = 5.dp),
                    text = "하니팜하니",
                    fontSize = 9.sp,
                    fontFamily = hancomSansFontFamily
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
                    text = "${comment.like}",
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
fun CommentFocusItem(
    modifier: Modifier = Modifier,
    comment: Comment,
    onItemClick: (Comment) -> Unit,
    onLikeClick: (Comment) -> Unit
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
                    text = comment.imoge,
                    textAlign = TextAlign.Center,
                    fontSize = 34.sp,
                    fontFamily = hancomSansFontFamily
                )

                Column {
                    Text(
                        text = comment.singleLineReview,
                        fontSize = 14.sp,
                        fontFamily = hancomSansFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = comment.detailedReview,
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
                    text = "고독한여행가",
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
                            .padding(start = 0.dp), text = "${comment.like}", fontSize = 10.sp
                    )
                }
            }
        }

    }
}
