package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wheretogo.presentation.AppPermission
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.effect.LifecycleDisposer
import com.wheretogo.presentation.feature.MediaAccess
import com.wheretogo.presentation.feature.checkFalseOrData
import com.wheretogo.presentation.feature.openSetting
import com.wheretogo.presentation.feature.requestPermission
import com.wheretogo.presentation.model.PickerImage
import com.wheretogo.presentation.viewmodel.MediaPickerUiEvent
import com.wheretogo.presentation.viewmodel.MediaPickerViewModel
import com.wheretogo.presentation.theme.Gray50
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.theme.White100
import kotlinx.coroutines.launch

@Composable
fun MediaPicker(
    onPicked: (List<PickerImage>) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: MediaPickerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val pagingItems = viewModel.images.collectAsLazyPagingItems()
    val coroutine = rememberCoroutineScope()

    BackHandler {
        onNavigateBack()
    }

    LifecycleDisposer { viewModel.lifecycleChange(it) }

    LaunchedEffect(Unit) {
        viewModel.setAccess(
            checkFalseOrData(context, AppPermission.MEDIA) as? MediaAccess
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {
            when (it) {
                MediaPickerUiEvent.RefreshPage -> {
                    pagingItems.refresh()
                }

                MediaPickerUiEvent.RefreshAccess -> {
                    viewModel.setAccess(
                        checkFalseOrData(context, AppPermission.MEDIA) as? MediaAccess
                    )
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
        val error= when {
            refresh is LoadState.Error -> refresh.error
            append is LoadState.Error -> append.error
            else -> null
        }
        error?.let { viewModel.handleError(it) }
    }

    when (state.access) {
        null -> DeniedView(
            {
                coroutine.launch {
                    requestPermission(context, AppPermission.MEDIA)
                }
            },
        )

        MediaAccess.FULL,
        MediaAccess.PARTIAL -> GalleryView(
            access = state.access!!,
            pagingItems = pagingItems,
            selected = state.selected,
            onToggle = viewModel::toggle,
            onOpenPicker = {
                coroutine.launch { requestPermission(context, AppPermission.MEDIA) }
            },
            onOpenSettings = {
                coroutine.launch { openSetting(context, AppPermission.MEDIA) }
            },
            onConfirm = {
                onPicked(it)
                onNavigateBack()
            },
        )
    }
}

@Composable
private fun DeniedView(onRequestPermission: () -> Unit) {
    LaunchedEffect(Unit) {
        onRequestPermission()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.need_photo_permission),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestPermission) { Text(stringResource(R.string.need_permission)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryView(
    access: MediaAccess,
    pagingItems: LazyPagingItems<PickerImage>,
    selected: Set<Long>,
    onToggle: (Long) -> Unit,
    onOpenPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onConfirm: (List<PickerImage>) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.photo_select)) })
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Button(
                    onClick = {
                        val picked = (0 until pagingItems.itemCount)
                            .mapNotNull { pagingItems[it] }
                            .filter { it.id in selected }
                        onConfirm(picked)
                    },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(stringResource(R.string.selected_count, selected.size))
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).background(
            color = White100
        )) {
            when (access) {
                MediaAccess.PARTIAL -> {
                    AllowAllBanner(onOpenSettings)
                    PartialBanner(onOpenPicker)
                }
                else -> {}
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
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
                            Modifier
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllowAllBanner(onOpenSettings: () -> Unit) {
    Surface(
        color = Gray50,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.need_all_photo_permission),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 58.dp, minHeight = 36.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onOpenSettings() }
                    .padding(ButtonDefaults.TextButtonContentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.button_setting),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PartialBanner(onRequestMore: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.show_photo_select),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(R.string.show_photo_select_desc),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onRequestMore) { Text(stringResource(R.string.more_add)) }
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
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(RoundedCornerShape(0.dp)),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image.uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clickableNoRipple(onClick),
        )
        if (isSelected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .size(20.dp),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.button_selected),
                    tint = Color.White,
                    modifier = Modifier.padding(2.dp),
                )
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            interactionSource = mutableStateOf(
                androidx.compose.foundation.interaction.MutableInteractionSource()
            ).value,
            indication = null,
            onClick = onClick,
        )
    )

@Composable
private fun fakePagingItems(count: Int): LazyPagingItems<PickerImage> {
    val items = (0 until count).map { PickerImage(id = it.toLong(), uri = android.net.Uri.EMPTY) }
    return kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.from(items))
        .collectAsLazyPagingItems()
}

@Preview(showBackground = true, name = "FULL")
@Composable
private fun GalleryViewFullPreview() {
    WhereTogoTheme {
        GalleryView(
            access = MediaAccess.FULL,
            pagingItems = fakePagingItems(9),
            selected = setOf(0L, 2L, 5L),
            onToggle = {},
            onOpenPicker = {},
            onOpenSettings = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true, name = "PARTIAL")
@Composable
private fun GalleryViewPartialPreview() {
    WhereTogoTheme {
        GalleryView(
            access = MediaAccess.PARTIAL,
            pagingItems = fakePagingItems(6),
            selected = setOf(0L, 3L),
            onToggle = {},
            onOpenPicker = {},
            onOpenSettings = {},
            onConfirm = {},
        )
    }
}