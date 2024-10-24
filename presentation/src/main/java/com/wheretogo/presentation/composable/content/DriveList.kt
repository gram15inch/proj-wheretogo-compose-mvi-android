package com.wheretogo.presentation.composable.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.domain.model.Journey
import com.wheretogo.presentation.R
import com.wheretogo.presentation.theme.White100


@Composable
fun DriveList(
    data: State<List<Journey>>,
    listState: LazyListState,
    onItemClick: (Journey) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {
        items(data.value) { item ->
            DriveListItem(
                modifier = Modifier
                    .clickable {
                        onItemClick(item)
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(White100),
                item= item
            )
        }
    }
}

@Composable
fun DriveListItem(modifier: Modifier, item: Journey) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        Row(modifier = modifier.fillMaxWidth()) {
            GlideImage(
                modifier = Modifier.size(80.dp),
                imageModel = { R.drawable.ic_setting },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    modifier =
                    Modifier
                        .wrapContentSize(),
                    text = item.course.waypoints.size.toString(),
                    fontSize = 24.sp
                )
                Text(
                    modifier = Modifier.wrapContentSize(),
                    text = item.code.toString(),
                    fontSize = 24.sp
                )
            }
        }
    }
}
