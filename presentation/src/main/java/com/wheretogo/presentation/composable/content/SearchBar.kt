package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
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
    isEmptyVisible: Boolean = false,
    searchBarItemGroup: List<SearchBarItem> = emptyList(),
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarClick: (Boolean) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    isInputModeDefault: Boolean = false
) {
    val isPreview = LocalInspectionMode.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isInputMode by remember { mutableStateOf(isInputModeDefault) }
    var editText by remember { mutableStateOf("") }
    var alpha by remember { mutableFloatStateOf(0.75f) }

    fun closeKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun clearBar() {
        isInputMode = false
        alpha = 0.75f
        editText = ""
    }

    fun toggleInputMode(): Boolean {
        if (isInputMode) {
            clearBar()
            closeKeyboard()
        } else {
            isInputMode = true
            alpha = 1f
        }
        return isInputMode
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(250.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(16.dp))
                .shadow(elevation = 1.5.dp, shape = RoundedCornerShape(16.dp), clip = false)
                .height(40.dp)
                .background(Color.White)

        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BarTextField(
                    Modifier.weight(1f), editText, isInputMode, focusRequester,
                    onTextValueChange = { editText = it },
                    onSearchSubmit = {
                        if (editText == "")
                            toggleInputMode()
                        else
                            closeKeyboard()
                        onSearchSubmit(it)
                    })
                BarIcon(isLoading)
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (editText.isNotBlank() || toggleInputMode()) {
                                    focusRequester.requestFocus()
                                }
                                onSearchBarClick(isInputMode)
                            },
                        )
                    }) { }
        }
        //todo replace 1
        if (isPreview || isInputMode && searchBarItemGroup.isNotEmpty()) {
            BarDropList(isEmptyVisible, searchBarItemGroup, onSearchBarItemClick = {
                if (it.label == CLEAR_ADDRESS)
                    toggleInputMode()
                else
                    closeKeyboard()
                onSearchBarItemClick(it)
            })
        }


    }
}

//todo replace 2
@Composable
fun BarTextField(
    modifier: Modifier,
    textValue: String,
    isInputMode: Boolean,
    focusRequester: FocusRequester,
    onTextValueChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit = {}
) {
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = hancomSansFontFamily,
        color = colorResource(R.color.gray_474747)
    )
    Box(
        modifier = modifier
            .padding(start = 16.dp, end = 5.dp)
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = textStyle,
            value = textValue,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSearchSubmit(textValue)
                }
            ),
            readOnly = !isInputMode,
            onValueChange = { onTextValueChange(it) })
    }
}

@Composable
fun BarIcon(isLoading: Boolean) {
    Box(
        modifier = Modifier
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

@Composable
fun BarDropList(
    isEmptyVisible: Boolean,
    searchBarItemGroup: List<SearchBarItem>,
    onSearchBarItemClick: (SearchBarItem) -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(searchBarItemGroup, key = { Math.random() }) { item ->
                BarListItem(
                    searchBarItem = item,
                    onSearchBarItemClick = {
                        onSearchBarItemClick(it)
                    }
                )
            }

            if (isEmptyVisible)
                item {
                    BarListItem(
                        searchBarItem = SearchBarItem(
                            label = stringResource(R.string.no_search_data),
                            address = "",
                        )
                    ) {}
                }
            if (searchBarItemGroup.isNotEmpty() || isEmptyVisible)
                item {
                    BarClearItem(onSearchBarItemClick = {
                        onSearchBarItemClick(it)
                    })
                }
        }
    }

}

@Composable
fun BarListItem(searchBarItem: SearchBarItem, onSearchBarItemClick: (SearchBarItem) -> Unit) {
    val isCourse = searchBarItem.address.isBlank()
    val textColor = if (isCourse) R.color.white else R.color.gray_474747
    val backgroundColor = if (isCourse) R.color.blue else R.color.white
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
                elevation = 1.5.dp, shape = RoundedCornerShape(16.dp), clip = false
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
fun BarClearItem(onSearchBarItemClick: (SearchBarItem) -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = 1.5.dp, shape = RoundedCornerShape(16.dp), clip = false
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
    SearchBar(
        modifier = Modifier.padding(15.dp),
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
        onSearchBarClick = {
            if (!it)
                simpleAddressGroups = emptyList()
        },
        onSearchSubmit = {
            simpleAddressGroups += SearchBarItem(it, "경기도 용인시 기흥구 120")
        },
        isInputModeDefault = true,
    )
}