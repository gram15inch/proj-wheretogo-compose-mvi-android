package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.state.InfoState


@Composable
@Preview
private fun InfoContentPreview(){
    Box(modifier = Modifier.width(400.dp)){
        InfoContent(
            state = InfoState(
                isRemoveButton = true,
                isReportButton = true,
            ),
            onRemoveClick = {},
            onReportClick = {}
        )
    }
}

@Composable
fun InfoContent(
    state: InfoState,
    onReportClick: (InfoState) -> Unit,
    onRemoveClick: (InfoState) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.isRemoveButton)
                    CircleButton(R.drawable.ic_delete, onClick = {
                        onRemoveClick(state)
                    })
                if (state.isReportButton)
                    CircleButton(R.drawable.ic_block, onClick = {
                        onReportClick(state)
                    })
            }
        }
    }
}

@Composable
private fun CircleButton(icon: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .border(width = 1.dp, shape = CircleShape, color = Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(icon), contentDescription = "")
    }
}

