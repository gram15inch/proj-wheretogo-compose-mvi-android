package com.wheretogo.presentation.composable.photoviewer.badge

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.theme.Palette
import com.wheretogo.presentation.theme.WhereTogoTheme

@Composable
fun StatusBadge(text: String, icon: ImageVector) {
    val warning = Palette.Warning.tint
    Surface(
        color = Palette.Warning.container,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = warning, modifier = Modifier.
            size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(text, color = warning, fontSize = 12.sp)
        }
    }
}

@Composable
@Preview
private fun Preview(){
    WhereTogoTheme {
        StatusBadge(
            "위치 정보 없음",
            icon = Icons.Outlined.Explore
        )
    }
}