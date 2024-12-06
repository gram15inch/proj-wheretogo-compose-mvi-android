package com.wheretogo.presentation.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheretogo.presentation.composable.content.DriveList
import com.wheretogo.presentation.viewmodel.BookmarkViewModel

@Composable
fun BookmarkScreen(navController: NavController, viewModel: BookmarkViewModel = hiltViewModel()) {
    val state = viewModel.bookMarkScreenState.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Column() {
            Button(onClick = {
            }) { Text("더미 추가") }
        }

        DriveList(modifier = Modifier,
            listItemGroup = emptyList(),
            onItemClick = {
                viewModel.removeBookmark(it.course.courseId)
            },
            onBookmarkClick = { item ->
                if (item.isBookmark)
                    viewModel.removeBookmark(item.course.courseId)
                else
                    viewModel.addBookmark(item.course.courseId)
            })
    }

}