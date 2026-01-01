package com.wheretogo.presentation.composable.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.presentation.composable.animation.highlightRoundedCorner
import com.wheretogo.presentation.state.ListState
import com.wheretogo.presentation.theme.Gray250
import com.wheretogo.presentation.theme.White100
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.toStrRes


@Preview
@Composable
fun DriveListPreview() {
    DriveListContent(
        modifier = Modifier,
        state = ListState(
            listItemGroup = listOf(
                ListState.ListItemState(
                    course = Course(
                        courseName = "노르테유 스카이웨이"
                    )
                )
            )
        ),
        onItemClick = {},
        onBookmarkClick = {},
        onHeightChange = {}
    )
}

@Composable
fun DriveListContent(
    modifier: Modifier,
    state: ListState = ListState(),
    onItemClick: (ListState.ListItemState) -> Unit,
    onHeightChange: (Dp) -> Unit = {},
    onBookmarkClick: (ListState.ListItemState) -> Unit = {}
) {
    val density = LocalDensity.current

    val listState = rememberLazyListState()
    Box(modifier.onSizeChanged { size ->
        onHeightChange(with(density) { size.height.toDp() })
    }) {
        LazyColumn(
            modifier = modifier.heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            state = listState
        ) {
            items(state.listItemGroup) { item ->
                DriveListItem(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            onItemClick(item)
                        },
                    listItem = item,
                    onBookmarkClick = onBookmarkClick
                )
            }
            item { Spacer(modifier = Modifier.height(1.dp)) }
        }
    }
}

@Composable
fun DriveListItem(
    modifier: Modifier,
    listItem: ListState.ListItemState,
    onBookmarkClick: (ListState.ListItemState) -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier
            .highlightRoundedCorner(listItem.isHighlight, 5.dp,16.dp)
            .shadow(
            elevation = 1.dp,
            shape = RoundedCornerShape(16.dp),
            clip = false
        ),
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(White100)
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = listItem.course.courseName,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = hancomSansFontFamily,
                        fontSize = 16.5.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DriveItemAttribute(
                        modifier = Modifier.weight(1f),
                        content = stringResource(
                            RouteCategory.fromCode(listItem.course.type)?.item.toStrRes().second
                        ),
                        type = "태그"
                    )

                    if (false) // todo 평점 추가
                        DriveItemAttribute(
                            modifier = Modifier.weight(1f),
                            content = listItem.course.like.toString(),
                            type = "평점"
                        )
                    if (listItem.course.level == "")
                        DriveItemAttribute(
                            modifier = Modifier.weight(1f),
                            content = stringResource(
                                RouteCategory.fromCode(listItem.course.relation)?.item.toStrRes().second
                            ),
                            type = "인원"
                        )
                    else
                        DriveItemAttribute(
                            modifier = Modifier.weight(1f),
                            content = stringResource(
                                RouteCategory.fromCode(listItem.course.level)?.item.toStrRes().second
                            ),
                            type = "난이도"
                        )
                    DriveItemAttribute(
                        modifier = Modifier.weight(1f),
                        content = listItem.course.duration + "분",
                        type = "소요시간"
                    )
                }
            }
        }
    }
}

@Composable
fun DriveItemAttribute(modifier: Modifier, content: String, type: String) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = content,
                fontSize = 13.5.sp,
                fontFamily = hancomSansFontFamily,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = type,
                fontSize = 9.5.sp,
                fontFamily = hancomSansFontFamily,
                color = Gray250,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
            )
        }
    }
}