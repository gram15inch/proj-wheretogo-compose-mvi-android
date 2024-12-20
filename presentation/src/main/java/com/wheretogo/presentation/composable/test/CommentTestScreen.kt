package com.wheretogo.presentation.composable.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.domain.model.dummy.getCommentDummy
import com.wheretogo.presentation.composable.content.MapPopup
import com.wheretogo.presentation.intent.DriveScreenIntent
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState
import com.wheretogo.presentation.state.DriveScreenState.PopUpState.CommentState.CommentItemState
import com.wheretogo.presentation.viewmodel.DriveViewModel


@Composable
fun CommentTestScreen(viewModel: DriveViewModel = hiltViewModel()) {
    val state by viewModel.driveScreenState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        MapPopup(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            commentState = state.popUpState.commentState.copy(
                commentItemGroup = getCommentDummy().map {
                    CommentItemState(
                        data = it,
                        isFold = if (it.like % 2 == 0) true else false,
                        isLike = if (it.like % 2 == 0) false else true
                    )
                }
            ),
            imageUrl = "",
            isWideSize = false,
            isCommentVisible = true,
            onCommentFloatingButtonClick = {},
            onCommentListItemClick = {},
            onCommentLikeClick = {},
            onCommentAddClick = { viewModel.handleIntent(DriveScreenIntent.CommentAddClick(it)) },
            onCommentEditValueChange = { viewModel.handleIntent(DriveScreenIntent.CommentEditValueChange(it))
            },
            onCommentEmogiPress = { viewModel.handleIntent(DriveScreenIntent.CommentEmogiPress(it)) },
            onCommentTypePress = { viewModel.handleIntent(DriveScreenIntent.CommentTypePress(it)) }
        )
    }
}