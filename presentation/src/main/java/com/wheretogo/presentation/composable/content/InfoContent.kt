package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.theme.hancomSansFontFamily


@Composable
@Preview
private fun InfoContentPreview() {
    Box(modifier = Modifier.width(400.dp)) {
        InfoContent(
            state = InfoState(
                isRemoveButton = true,
                isReportButton = true,
                createdBy = "고독한 여행가"
            ),
            onRemoveClick = {},
            onReportClick = {}
        )
    }
}

@Composable
fun InfoContent(
    state: InfoState,
    onReportClick: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .padding(top = 20.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading)
                DelayLottieAnimation(Modifier.size(40.dp), R.raw.lt_loading, true, 0)
            else
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.isRemoveButton)
                            CircleBorderButton(R.drawable.ic_delete, onClick = {
                                onRemoveClick()
                            })
                        if (state.isReportButton)
                            CircleBorderButton(R.drawable.ic_block, onClick = {
                                onReportClick(state.reason)
                            })
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontSize = 12.sp, text = context.getString(R.string.created_by,state.createdBy), fontFamily = hancomSansFontFamily)
                }

        }
    }
}

@Composable
private fun CircleBorderButton(icon: Int, onClick: () -> Unit) {
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

