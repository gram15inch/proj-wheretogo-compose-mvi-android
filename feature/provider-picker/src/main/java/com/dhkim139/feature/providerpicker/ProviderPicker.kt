package com.dhkim139.feature.providerpicker

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dhkim139.core.ui.theme.Palette
import com.dhkim139.core.ui.theme.WhereTogoTheme
import com.dhkim139.feature.providerpicker.model.PickerImage
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPicker(
    onPicked: (List<PickerImage>) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: ProviderPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pagingItems = viewModel.images.collectAsLazyPagingItems()

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {
            when (it) {
                ProviderPickerUiEvent.RefreshPage -> {
                    pagingItems.refresh()
                }
            }
        }
    }

    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is LoadState.NotLoading) {
            val ids = (0 until pagingItems.itemCount)
                .mapNotNull { pagingItems[it]?.id }
                .toSet()
            viewModel.refreshSelection(ids)
        }
    }

    LaunchedEffect(pagingItems.loadState) {
        val refresh = pagingItems.loadState.refresh
        val append = pagingItems.loadState.append
        val error = when {
            refresh is LoadState.Error -> refresh.error
            append is LoadState.Error -> append.error
            else -> null
        }
        error?.let { viewModel.handleError(it) }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.photo_select)) }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        })
    }, bottomBar = {
        SelectBottomBar(
            selectedSize = state.selected.size, onConfirm = {
                val picked = (0 until pagingItems.itemCount).mapNotNull { pagingItems[it] }
                    .filter { it.id in state.selected }
                onPicked(picked)
                onNavigateBack()
            })
    }) { padding ->
        Box(Modifier.Companion.padding(padding)) {
            GalleryView(
                pagingItems = pagingItems,
                selected = state.selected,
                onToggle = viewModel::toggle
            )
        }
    }
}

@Composable
private fun GalleryView(
    pagingItems: LazyPagingItems<PickerImage>,
    selected: Set<Long>,
    onToggle: (Long) -> Unit
) {
    Column {
        PickPhotoBanner()
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.Companion.weight(1f),
            contentPadding = PaddingValues(2.dp),
        ) {
            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems[index]?.id ?: index },
            ) { index ->
                val image = pagingItems[index]
                if (image != null) {
                    PhotoCell(
                        image = image,
                        isSelected = image.id in selected,
                        onClick = { onToggle(image.id) },
                    )
                } else {
                    Box(
                        Modifier.Companion
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }
}

@Composable
private fun PickPhotoBanner() {
    val tint =
        Palette.TealBanner
    Surface(
        color = tint.copy(alpha = 0.12f),
        modifier = Modifier.Companion.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.Companion
                    .clip(RoundedCornerShape(20.dp))
                    .size(15.dp),
                contentAlignment = Alignment.Companion.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = tint
                )
            }
            Spacer(modifier = Modifier.Companion.width(7.dp))
            Text(
                stringResource(R.string.course_found_app),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.Companion.weight(1f),
                color = tint
            )
        }
    }
}

@Composable
private fun PhotoCell(
    image: PickerImage,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.Companion
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(0.dp)),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image.uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Companion.Crop,
            modifier = Modifier.Companion
                .fillMaxSize()
                .clickableNoRipple(onClick),
        )
        if (isSelected) {
            Box(
                Modifier.Companion
                    .fillMaxSize()
                    .background(Palette.Black.copy(alpha = 0.25f)),
            )
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.TopEnd)
                    .padding(5.dp)
                    .size(20.dp),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.button_selected),
                    tint = Palette.White,
                    modifier = Modifier.Companion.padding(2.dp),
                )
            }
        }
    }
}

@Composable
fun SelectBottomBar(selectedSize: Int, onConfirm: () -> Unit) {
    Surface(tonalElevation = 2.dp) {
        Button(
            onClick = onConfirm,
            enabled = selectedSize > 0,
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(stringResource(R.string.selected_count, selectedSize))
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.Companion.clickable(
        interactionSource = mutableStateOf(MutableInteractionSource()).value,
        indication = null,
        onClick = onClick,
    )
)

@Composable
private fun fakePagingItems(count: Int): LazyPagingItems<PickerImage> {
    val items = (0 until count).map { PickerImage(id = it.toLong(), uri = Uri.EMPTY) }
    return flowOf(PagingData.from(items))
        .collectAsLazyPagingItems()
}

@Preview(showBackground = true, name = "FULL")
@Composable
private fun GalleryViewFullPreview() {
    WhereTogoTheme {
        GalleryView(
            pagingItems = fakePagingItems(9),
            selected = setOf(0L, 2L, 5L),
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "PARTIAL")
@Composable
private fun GalleryViewPartialPreview() {
    WhereTogoTheme {
        GalleryView(
            pagingItems = fakePagingItems(6),
            selected = setOf(0L, 3L),
            onToggle = {},
        )
    }
}
