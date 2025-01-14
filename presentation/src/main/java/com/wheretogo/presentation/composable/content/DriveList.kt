package com.wheretogo.presentation.composable.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.domain.CourseDetail
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.R
import com.wheretogo.presentation.state.DriveScreenState.ListState.ListItemState
import com.wheretogo.presentation.theme.White100
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.toStrRes


@Preview
@Composable
fun DriveListPreview() {
    DriveList(
        modifier = Modifier,
        listOf(ListItemState(
            course = Course(
                courseName = "노르테유 스카이웨이"
            )
        )),
        onItemClick = {},
        onBookmarkClick = {}
    )
}

@Composable
fun DriveList(
    modifier: Modifier,
    listItemGroup: List<ListItemState>,
    onItemClick: (ListItemState) -> Unit,
    onBookmarkClick: (ListItemState) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.heightIn(max = 280.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        state = listState
    ) {
        items(listItemGroup) { item ->
            DriveListItem(
                modifier = Modifier
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

@Composable
fun DriveListItem(
    modifier: Modifier,
    listItem: ListItemState,
    onBookmarkClick: (ListItemState) -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(16.dp))
                .background(White100)
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier=Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = listItem.course.courseName,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = hancomSansFontFamily,
                        fontSize = 16.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onBookmarkClick(listItem)
                            }
                    ) {
                        Image(
                            modifier = Modifier
                                .size(26.dp)
                                .padding(5.dp),
                            painter = painterResource(if (listItem.isBookmark) R.drawable.ic_bookmark else R.drawable.ic_bookmark),
                            contentDescription = "",
                        )
                    }

                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    driveItemAttribute(
                        modifier = Modifier.weight(1f),
                        content = stringResource(
                            CourseDetail.fromCode(listItem.course.tag).toStrRes()
                        ),
                        type = "태그"
                    )
                    driveItemAttribute(
                        modifier = Modifier.weight(1f),
                        content = listItem.course.like.toString(),
                        type = "평점"
                    )
                    if (listItem.course.level == "")
                        driveItemAttribute(
                            modifier = Modifier.weight(1f),
                            content = stringResource(
                                CourseDetail.fromCode(listItem.course.relation).toStrRes()
                            ),
                            type = "인원"
                        )
                    else
                        driveItemAttribute(
                            modifier = Modifier.weight(1f),
                            content = stringResource(
                                CourseDetail.fromCode(listItem.course.level).toStrRes()
                            ),
                            type = "난이도"
                        )
                    driveItemAttribute(
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
fun driveItemAttribute(modifier: Modifier, content: String, type: String) {
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
                color = colorResource(R.color.gray_848484),
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