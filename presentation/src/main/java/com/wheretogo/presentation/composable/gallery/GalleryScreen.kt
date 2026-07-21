package com.wheretogo.presentation.composable.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.MediaPicker
import com.wheretogo.presentation.feature.GroupingStrategy
import com.wheretogo.presentation.model.PhotoSection
import com.wheretogo.presentation.state.GalleryState
import com.wheretogo.presentation.viewmodel.GalleryFlowViewModel
import com.wheretogo.presentation.intent.GalleryIntent


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun GalleryScreen(
    sharedScope:SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    gridState: LazyGridState,
    onPhotoOpen: (GalleryPhoto) -> Unit,
    viewModel: GalleryFlowViewModel,
) {
    val uiState by viewModel.galleryState.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var showGroupingSheet by rememberSaveable { mutableStateOf(false) }

    if (showPicker) {
        MediaPicker(
            onPicked = { picked ->
                showPicker = false
                viewModel.handleIntent(GalleryIntent.MediaPicked(picked))
            },
            onNavigateBack = { showPicker = false },
        )
        return
    }

    val state = uiState
    val isSelectionMode = state is GalleryState.Success && state.isSelectionMode

    BackHandler(enabled = isSelectionMode) { viewModel.handleIntent(GalleryIntent.ClearSelection) }

    Scaffold(
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { showPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.photo_add))
                }
            }
        },
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)) {
            Column(Modifier.fillMaxSize()) {

                if (state is GalleryState.Success) {
                    GalleryTopArea(
                        selectionMode = isSelectionMode,
                        groupingLabel = state.groupingLabel,
                        sheetExpanded = showGroupingSheet,
                        allSelected = state.allPhotos.isNotEmpty() &&
                                state.selectedIds.size == state.allPhotos.size,
                        onGroupingButtonClick = { showGroupingSheet = true },
                        onToggleSelectAll = {
                            if (state.selectedIds.size == state.allPhotos.size)
                                viewModel.handleIntent(GalleryIntent.ClearSelection)
                            else
                                viewModel.handleIntent(GalleryIntent.SelectAll)
                        },
                    )
                }

                Box(Modifier.fillMaxSize()) {
                    when (state) {
                        is GalleryState.Loading ->
                            LoadingMessageScreen()
                        is GalleryState.Error ->
                            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message)
                                TextButton(
                                    onClick = { viewModel.handleIntent(GalleryIntent.Refresh) }
                                ) { Text(stringResource(R.string.retry)) }
                            }
                        is GalleryState.Empty -> EmptyGalleryScreen(
                            onFindInGalleryClick = { showPicker = true }
                        )
                        is GalleryState.Success ->
                            PhotoGrid(
                                sections = state.sections,
                                selectedIds = state.selectedIds,
                                selectionMode = state.isSelectionMode,
                                gridState = gridState,
                                sharedScope = sharedScope,
                                animatedScope = animatedScope,
                                onPhotoClick = { photo ->
                                    val s = uiState as? GalleryState.Success
                                    if (s?.isSelectionMode == true)
                                        viewModel.handleIntent(GalleryIntent.PhotoClick(photo.id))
                                    else
                                        onPhotoOpen(photo)
                                },
                                onPhotoLongClick = {
                                    viewModel.handleIntent(GalleryIntent.PhotoLongClick(it.id))
                                },
                            )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                val s = state as? GalleryState.Success
                SelectionActionBar(
                    selectedCount = s?.selectedIds?.size ?: 0,
                    onDelete = { viewModel.handleIntent(GalleryIntent.DeleteSelected) },
                )
            }
        }
    }

    if (showGroupingSheet) {
        GroupingBottomSheet(
            options = viewModel.groupings,
            currentLabel = viewModel.currentGroupingLabel,
            onSelect = {
                viewModel.handleIntent(GalleryIntent.ChangeGrouping(it))
                showGroupingSheet = false
            },
            onDismiss = { showGroupingSheet = false },
        )
    }
}

@Composable
private fun GalleryTopArea(
    selectionMode: Boolean,
    groupingLabel: String,
    sheetExpanded: Boolean,
    allSelected: Boolean,
    onGroupingButtonClick: () -> Unit,
    onToggleSelectAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fadeAlpha by animateFloatAsState(if (selectionMode) 1f else 0f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        GroupHeader(
            modifier = Modifier.graphicsLayer { alpha = 1f - fadeAlpha },
            groupingLabel = groupingLabel,
            selectionMode = selectionMode,
            sheetExpanded = sheetExpanded,
            onGroupingButtonClick = onGroupingButtonClick
        )
        SelectHeader(
            modifier = Modifier.graphicsLayer { alpha = fadeAlpha },
            selectionMode = selectionMode,
            allSelected = allSelected,
            onToggleSelectAll = onToggleSelectAll
        )
    }
}

@Composable
private fun GroupHeader(
    modifier: Modifier,
    groupingLabel: String,
    selectionMode: Boolean,
    sheetExpanded: Boolean,
    onGroupingButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(if (!selectionMode) Modifier.clickable(onClick = onGroupingButtonClick) else Modifier)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(groupingLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val direction by animateFloatAsState(if (sheetExpanded) 180f else 0f)
        Icon(
            Icons.Default.KeyboardArrowDown, contentDescription = null,
            modifier = Modifier
                .padding(start = 2.dp)
                .graphicsLayer { rotationZ = direction },
        )
    }
}

@Composable
private fun SelectHeader(modifier: Modifier, selectionMode: Boolean, allSelected: Boolean, onToggleSelectAll:()->Unit){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.select_ing), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onToggleSelectAll, enabled = selectionMode) {
            Text(if (allSelected) stringResource(R.string.release_all) else stringResource(R.string.select_all))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PhotoGrid(
    modifier: Modifier = Modifier,
    sections: List<PhotoSection>,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    gridState: LazyGridState,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onPhotoClick: (GalleryPhoto) -> Unit,
    onPhotoLongClick: (GalleryPhoto) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        sections.forEach { section ->
            item(key = "header_${section.key}", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(section.title,section.hasStamp)
            }
            items(section.photos, key = { it.id }) { photo ->
                PhotoCell(
                    modifier = Modifier.animateItem(),
                    photo = photo,
                    selected = photo.id in selectedIds,
                    selectionMode = selectionMode,
                    sharedScope = sharedScope,
                    animatedScope = animatedScope,
                    onClick = { onPhotoClick(photo) },
                    onLongClick = { onPhotoLongClick(photo) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, hasStamp: Boolean, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 4.dp, top = 16.dp, bottom = 6.dp)
    ) {
        Text(
            title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(3.dp))
        if(hasStamp)
            Icon(
                Icons.Rounded.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PhotoCell(
    modifier: Modifier = Modifier,
    photo: GalleryPhoto,
    selected: Boolean,
    selectionMode: Boolean,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (selected) 0.88f else 1f)
    val isLocationBadge=
        photo.exif.location != null && !selectionMode && photo.courseId.isNullOrBlank()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        with(sharedScope) {
            AsyncImage(
                model = photo.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = photo.id),
                        animatedVisibilityScope = animatedScope,
                    )
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(if (selected) 10.dp else 4.dp)),
            )
        }

        if (selected) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }
        if (isLocationBadge) {
            LocationBadge(Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp))
        }
        if (selectionMode) {
            SelectionCheck(selected, Modifier
                .align(Alignment.TopStart)
                .padding(4.dp))
        }
    }
}

@Composable
private fun LocationBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SelectionCheck(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SelectionActionBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showConfirm by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxWidth(), shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.count_selected, selectedCount), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { showConfirm = true },
                enabled = selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.delete))
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.request_count_delete, selectedCount)) },
            text = { Text(stringResource(R.string.no_restore_photo_delete)) },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupingBottomSheet(
    options: List<GroupingStrategy>,
    currentLabel: Int,
    onSelect: (GroupingStrategy) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)) {
            Text(stringResource(R.string.how_show), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            options.forEach { strategy ->
                val isSelect = strategy.label == currentLabel
                GroupingStrategyItem(strategy = strategy, isSelect = isSelect, onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun GroupingStrategyItem(
    strategy: GroupingStrategy,
    isSelect: Boolean,
    onSelect: (GroupingStrategy) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(strategy) }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(strategy.label, style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelect) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        if (isSelect) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
    }
}