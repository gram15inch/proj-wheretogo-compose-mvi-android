package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.AdMinSize
import com.wheretogo.presentation.CLEAR_ADDRESS
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.intervalTab
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.SearchBarState
import com.wheretogo.presentation.theme.Gray150
import com.wheretogo.presentation.theme.Gray320
import com.wheretogo.presentation.theme.PrimeBlue
import com.wheretogo.presentation.theme.White
import com.wheretogo.presentation.theme.hancomSansFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: SearchBarState = SearchBarState(),
    onSearchBarItemClick: (SearchBarItem) -> Unit = {},
    onSearchBarClick: (Boolean) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSearchBarClose: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    var alpha by remember { mutableFloatStateOf(0.75f) }
    var isAdSkip by remember { mutableStateOf(false) }
    val adSize = adSize(true)
    val outDp = 12.dp
    LaunchedEffect(state.isActive) {
        if (state.isActive) {
            alpha = 1f
        } else {
            alpha = 0.75f
            editText = ""
        }
    }
    LaunchedEffect(adSize) {
        if(adSize == AdMinSize.INVISIBLE)
            isAdSkip = true
        else
            isAdSkip = false
    }

    fun clearFocus() {
        focusManager.clearFocus()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = outDp)
                .width(250.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(16.dp))
                .shadow(elevation = 1.5.dp, shape = RoundedCornerShape(16.dp), clip = false)
                .height(40.dp)
                .background(Color.White)

        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BarTextField(
                    modifier = Modifier.weight(1f),
                    textValue = editText,
                    readOnly = !state.isActive,
                    focusRequester = focusRequester,
                    onTextValueChange = { editText = it },
                    onSearchSubmit = {
                        clearFocus()
                        onSearchSubmit(it)
                    },
                    onFocusChanged = {
                        isFocused = it.isFocused
                        when {
                            it.isFocused && editText.isBlank() -> {
                                onSearchBarClick(isAdSkip)
                            }

                            !it.isFocused && editText.isBlank() -> {
                                onSearchBarClose()
                            }
                        }
                    }
                )
                BarIcon(state.isLoading)
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .intervalTab {
                        when {
                            isFocused && editText.isBlank() -> {
                                focusManager.clearFocus()
                            }

                            isFocused && editText.isNotBlank() -> {
                                editText = ""
                                focusRequester.requestFocus()
                            }

                            else -> {
                                focusRequester.requestFocus()
                            }

                        }
                    }
            ) { }
        }

        KeyboardTrack(
            onKeyboardClose = {
                if (state.isActive && state.searchBarItemGroup.isEmpty() && !state.isEmptyVisible) {
                    clearFocus()
                }
            })

        Box {
            val isAd = if (isPreview) {
                state.isAdVisible
            } else {
                state.isAdVisible && state.isActive && state.searchBarItemGroup.isEmpty() && !state.isEmptyVisible
            }

            if (!isAd)
                BarDropList(
                    modifier = modifier.padding(horizontal = outDp),
                    isEmptyVisible = state.isEmptyVisible,
                    searchBarItemGroup = state.searchBarItemGroup,
                    onSearchBarItemClick = {
                        if (it.label == CLEAR_ADDRESS) {
                            onSearchBarClose()
                            clearFocus()
                        } else {
                            clearFocus()
                        }
                        onSearchBarItemClick(it)
                    })

            SlideAnimation(
                visible = isAd,
                direction = AnimationDirection.CenterRight
            ) {
                AdaptiveAd(
                    modifier = Modifier.padding(bottom = outDp, start = outDp, end = outDp),
                    isCompact = true,
                    nativeAd = state.adItemGroup.firstOrNull()?.nativeAd
                )
            }
        }
    }
}

@Composable
fun KeyboardTrack(onKeyboardClose: () -> Unit) {
    val density = LocalDensity.current
    val height = WindowInsets.ime.getBottom(density)
    var latestHeight by remember { mutableIntStateOf(0) }
    var direction by remember { mutableIntStateOf(0) }

    val diff = height - latestHeight
    when {
        diff > 0 -> {
            if (direction <= 0) direction = 1
        }

        diff < 0 -> {
            if (direction > 0) direction = -1
        }
    }

    if (direction < 0 && height == 0 && latestHeight != height) {
        onKeyboardClose()
    }
    latestHeight = height
}

@Composable
fun BarTextField(
    modifier: Modifier,
    textValue: String,
    readOnly: Boolean,
    focusRequester: FocusRequester,
    onTextValueChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit = {},
    onFocusChanged: (FocusState) -> Unit = {}
) {
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = hancomSansFontFamily,
        color = Gray320
    )
    Box(
        modifier = modifier
            .padding(start = 16.dp, end = 5.dp)
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    onFocusChanged(it)
                },
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
            readOnly = readOnly,
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
    modifier: Modifier = Modifier,
    isEmptyVisible: Boolean,
    searchBarItemGroup: List<SearchBarItem>,
    onSearchBarItemClick: (SearchBarItem) -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
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
    val textColor = if (searchBarItem.isCourse) White else Gray320
    val backgroundColor = if (searchBarItem.isCourse) PrimeBlue else White
    val textStyle = TextStyle(
        fontFamily = hancomSansFontFamily,
        color = textColor
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
            .background(backgroundColor)
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
            .background(Gray150)
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

@Preview("s10", widthDp = 670, heightDp = 336)
@Preview("s24+", widthDp = 832, heightDp = 384)
@Preview("s6l", widthDp = 1333, heightDp = 728)
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
        state = SearchBarState(
            isActive = true,
            isLoading = isLoading,
            isEmptyVisible = false,
            isAdVisible = true,
            searchBarItemGroup = emptyList()
        ),
        onSearchBarItemClick = {
            CoroutineScope(Dispatchers.Main).launch {
                isLoading = true
                delay(2000)
                isLoading = false
                simpleAddressGroups = emptyList()
            }
        },
        onSearchBarClick = {},
        onSearchSubmit = {
            simpleAddressGroups += SearchBarItem(it, "경기도 용인시 기흥구 120")
        },
    )
}