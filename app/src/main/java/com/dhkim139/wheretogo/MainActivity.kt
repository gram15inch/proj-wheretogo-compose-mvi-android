package com.dhkim139.wheretogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.dhkim139.wheretogo.ui.composable.DriveContent
import com.dhkim139.wheretogo.ui.composable.HomeContent
import com.dhkim139.wheretogo.ui.theme.WhereTogoTheme
import com.dhkim139.wheretogo.ui.theme.White100
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WhereTogoTheme {
                Box(
                    modifier = Modifier
                        .background(Color.Gray)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(White100)
                            .padding(16.dp)
                    ) {
                        val displayMaxWidth = min(400.dp, maxWidth)
                        var contentIdx by remember { mutableIntStateOf(0) }
                        when (contentIdx) {
                            0 -> HomeContent(displayMaxWidth) { contentIdx = 1 }
                            1 -> DriveContent(displayMaxWidth) { contentIdx = 0 }
                        }
                    }
                }
            }
        }
    }
}




