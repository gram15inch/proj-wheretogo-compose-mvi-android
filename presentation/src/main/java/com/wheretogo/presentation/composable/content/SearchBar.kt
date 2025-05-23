package com.wheretogo.presentation.composable.content

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.R
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.theme.hancomSansFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isEmptyVisible:Boolean = false,
    searchBarItemGroup: List<SearchBarItem> = emptyList(),
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarToggleClick: (Boolean) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val focusRequester = remember { FocusRequester() }
    var isInputMode by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    val fieldWidth by animateDpAsState(
        targetValue = if (isInputMode) 240.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 1.5.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(16.dp))
                .height(40.dp)
                .background(Color.White)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = hancomSansFontFamily,
                    color = colorResource(R.color.gray_474747)
                )
                Box(
                    modifier = Modifier
                        .width(fieldWidth)
                        .padding(start = 14.dp, end = 5.dp)
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = textStyle,
                        value = editText,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onSearchSubmit(editText)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if(editText.isBlank())
                                    isInputMode = false
                            }
                        ),
                        readOnly = fieldWidth == 0.dp,
                        onValueChange = { editText = it })
                }
                Box(modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (isInputMode) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            editText = ""
                            isInputMode = false
                        } else {
                            focusRequester.requestFocus()
                            isInputMode = true
                        }
                        onSearchBarToggleClick(isInputMode)
                    }
                    .size(40.dp)
                    .padding(10.dp)
                ) {
                    if (isLoading)
                        DelayLottieAnimation(
                            Modifier,
                            ltRes = R.raw.lt_loading,
                            isVisible = true,
                            0
                        )
                    else
                        Image(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = ""
                        )
                }
            }
        }
        LazyColumn(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(searchBarItemGroup, key = { Math.random() }) { item ->
                SearchBarListItem(
                    searchBarItem = item,
                    onSearchBarItemClick = {
                        onSearchBarItemClick(it)
                    }
                )
            }

            if(isEmptyVisible)
                item{
                    SearchBarListItem(
                        searchBarItem = SearchBarItem(
                            label = stringResource(R.string.no_search_data),
                            address = "",
                        )
                    ) {}
                }
            if(searchBarItemGroup.isNotEmpty() || isEmptyVisible)
                item{
                    ClearItem(onSearchBarItemClick = {
                        editText=""
                        isInputMode = false
                        onSearchBarItemClick(it)
                    })
                }
        }

    }
}

@Composable
fun SearchBarListItem(searchBarItem: SearchBarItem, onSearchBarItemClick: (SearchBarItem) -> Unit) {
    val isCourse = searchBarItem.address.isBlank()
    val textColor = if(isCourse) R.color.white else R.color.gray_474747
    val backgroundColor = if(isCourse) R.color.blue else R.color.white
    val textStyle = TextStyle(
        fontFamily = hancomSansFontFamily,
        color = colorResource(textColor)
    )
    Box(
        Modifier
            .clickable {
                onSearchBarItemClick(searchBarItem)
            }
            .shadow(
                elevation = 1.5.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(backgroundColor))
    ) {
        Text(
            modifier = Modifier.padding(8.dp), text = searchBarItem.label, style = textStyle
        )
    }
}

@Composable
fun ClearItem(onSearchBarItemClick: (SearchBarItem) -> Unit){
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = 1.5.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .clickable {
                onSearchBarItemClick(SearchBarItem(CLEAR_ADDRESS, ""))
            }
            .background(colorResource(R.color.gray_B9B9B9))
    ) {
        Image(
            modifier = Modifier
                .size(34.dp)
                .padding(8.dp),
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = "delete"
        )
    }
}

@Preview
@Composable
fun SearchBarPreview() {
    var simpleAddressGroups by remember {
        mutableStateOf<List<SearchBarItem>>(
            listOf(
                SearchBarItem(
                    "기흥호수공원 순환",
                    "",
                ),
                SearchBarItem(
                    "기흥역 ak플라자",
                    "경기도 용인시 기흥구 120",
                )
            )
        )
    }
    var isLoading by remember { mutableStateOf<Boolean>(false) }
    Box(
        modifier = Modifier
            .height(300.dp)
            .fillMaxSize()
            .background(Color.LightGray), contentAlignment = Alignment.TopEnd
    ) {
        SearchBar(
            modifier = Modifier.padding(top = 15.dp, bottom = 20.dp, end = 15.dp),
            isLoading = isLoading,
            isEmptyVisible = false,
            searchBarItemGroup = simpleAddressGroups,
            onSearchBarItemClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    isLoading = true
                    delay(2000)
                    isLoading = false
                    simpleAddressGroups = emptyList()
                }
            },
            onSearchBarToggleClick = {
                if (!it)
                    simpleAddressGroups = emptyList()
            },
            onSearchSubmit = {
                simpleAddressGroups += SearchBarItem(it, "경기도 용인시 기흥구 120",)
            }
        )
    }
}