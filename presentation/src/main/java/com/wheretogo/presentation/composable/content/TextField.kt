package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.topShadow


@Composable
fun CommentTextField(
    editText: TextFieldValue,
    isEmoji: Boolean,
    emoji: String,
    onValueChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
) {
    Row {
        Box(
            modifier = Modifier
                .run { if (isEmoji) this else this.width(0.dp) }
                .height(50.dp)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                modifier = Modifier,
                text = emoji,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 28.sp,
                    lineHeight = 28.sp
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(30.dp))
                    .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                val keyboard = LocalSoftwareKeyboardController.current
                val focuse = LocalFocusManager.current
                var isDone by remember { mutableStateOf(false) }
                BasicTextField(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    value = editText,
                    onValueChange = { newText ->
                        if (!isDone) // 키보드 완료시 업데이트 막기
                            onValueChange(newText)
                        else
                            isDone = false
                    },
                    cursorBrush = SolidColor(Color.Black),
                    maxLines = Int.MAX_VALUE,
                    textStyle = TextStyle(
                        fontSize = 11.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboard?.hide()
                            focuse.clearFocus()
                            isDone = true
                            onDone()
                        }
                    ),
                )
            }
        }
    }
}


@Composable
fun DescriptionTextField(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    focusRequester: FocusRequester,
    text: String,
    onTextChange: (String) -> Unit,
    onEnterClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = modifier.wrapContentHeight()) {
        Row(
            modifier = Modifier
                .run {
                    if (!isVisible)
                        this.height(0.dp)
                    else
                        this
                }
                .topShadow()
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorResource(R.color.gray_B9B9B9))
                        .padding(10.dp)
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .focusRequester(focusRequester),
                        value = text,
                        onValueChange = {
                            onTextChange(it)
                        }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.Bottom)
                    .padding(bottom = 5.dp, start = 5.dp)
            ) {
                EnterButton(onClick = {
                    onEnterClick()
                    focusRequester.freeFocus()
                    keyboardController?.hide()
                })
            }

        }
    }
}


@Composable
fun EnterButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.blue))
            .clickable { onClick() }
            .padding(vertical = 1.dp, horizontal = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(width = 28.dp, height = 32.dp),
            painter = painterResource(R.drawable.ic_enter),
            contentDescription = ""
        )
    }
}
