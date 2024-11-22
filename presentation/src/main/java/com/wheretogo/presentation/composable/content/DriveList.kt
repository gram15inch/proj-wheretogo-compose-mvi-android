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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.getJourneyDummy
import com.wheretogo.presentation.theme.White100
import com.wheretogo.presentation.theme.hancomSansFontFamily


@Preview
@Composable
fun DriveListPreview() {
    DriveList(
        modifier = Modifier,
        data = getJourneyDummy(),
        onItemClick = {}
    )
}

@Composable
fun DriveList(
    modifier: Modifier,
    data: List<Journey>,
    onItemClick: (Journey) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        state = listState
    ) {
        items(data) { item ->
            DriveListItem(
                modifier = Modifier
                    .widthIn(650.dp)
                    .clickable {
                        onItemClick(item)
                    },
                item = item
            )
        }
        item { Spacer(modifier = Modifier.height(1.dp)) }
    }
}

@Composable
fun DriveListItem(modifier: Modifier, item: Journey) {
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
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "운전연수 코스 1001",
                    textAlign = TextAlign.Center,
                    fontFamily = hancomSansFontFamily,
                    fontSize = 16.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    driveItemAttribute(
                        modifier = Modifier.weight(1f),
                        content = "도로연수",
                        type = "태그"
                    )
                    driveItemAttribute(modifier = Modifier.weight(1f), content = "4.5", type = "평점")
                    driveItemAttribute(modifier = Modifier.weight(1f), content = "초보", type = "난이도")
                    driveItemAttribute(
                        modifier = Modifier.weight(1f),
                        content = "20분",
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