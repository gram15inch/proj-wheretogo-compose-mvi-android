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
                viewModel.addBookmark(1001)
                viewModel.addBookmark(1002)
                viewModel.addBookmark(1003)
                viewModel.addBookmark(1004)
                viewModel.addBookmark(1005)
                viewModel.addBookmark(1006)
                viewModel.addBookmark(1007)
            }) { Text("더미 추가") }
        }

        DriveList(modifier = Modifier,
            data = state.value.data,
            onItemClick = {
                //  viewModel.removeBookmark(it.code)
            },
            onBookmarkClick = { item ->
                if (item.isBookmark)
                    viewModel.removeBookmark(item.code)
                else
                    viewModel.addBookmark(item.code)
            })
    }

}