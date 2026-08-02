package com.dhkim139.feature.camerapicker


import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.core.ui.model.AppLifecycle
import com.dhkim139.core.ui.permission.AppPermission
import com.dhkim139.core.ui.permission.checkFalseOrData
import com.dhkim139.core.ui.permission.requestPermission
import com.dhkim139.core.ui.screen.LifecycleDisposer
import com.dhkim139.feature.camerapicker.model.Capture
import com.dhkim139.feature.camerapicker.model.Location
import com.dhkim139.feature.camerapicker.model.VerifiedImageGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


@Composable
fun CameraPicker(
    onPicked: (VerifiedImageGroup) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: CameraPickerViewModel = hiltViewModel(),
) {
    val locationTimeout = 5000L
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }
    var lastLaunchUri by remember { mutableStateOf<Uri?>(null) }

    var locationLoading by remember { mutableStateOf(false) }
    val locationJob = remember { mutableStateOf<Job?>(null) }

    suspend fun refreshLocation(): Location? {
        locationLoading = true
        return try {
            withTimeoutOrNull(locationTimeout) {
                getCurrentLocation(context)
            }.also {
                viewModel.onIntent(
                    CameraPickerIntent.LocationStatusChanged(
                        if (it != null) LocationStatus.Available(it.accuracy)
                        else LocationStatus.Unavailable
                    )
                )
            }
        } finally {
            locationLoading = false
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = lastLaunchUri ?: return@rememberLauncherForActivityResult
        if (!success) {
            viewModel.onIntent(CameraPickerIntent.PhotoReturned(null))
            return@rememberLauncherForActivityResult
        }
        val returnedAt = System.currentTimeMillis()
        scope.launch {
            locationJob.value?.cancel()
            val loc = refreshLocation()
            val capture = Capture(
                uri = uri,
                location = loc,
                returnedAt = returnedAt
            )
            viewModel.onIntent(CameraPickerIntent.PhotoReturned(capture))
        }
    }

    fun takePicture(uri:Uri){
        lastLaunchUri = uri
        takePictureLauncher.launch(uri)
    }

    BackHandler {
        viewModel.onIntent(CameraPickerIntent.ExitCheck)
    }

    LifecycleDisposer {
        when(it){
            AppLifecycle.onResume -> {
                val check = checkFalseOrData(context, AppPermission.LOCATION)
                val lp = if(check != false) LocationPermission.GRANTED else LocationPermission.DENIED
                viewModel.onIntent(CameraPickerIntent.PermissionChanged(lp))
            }
            else -> {}
        }
    }

    LaunchedEffect(state.permission) {
        if(state.permission == LocationPermission.GRANTED){
            locationJob.value?.cancel()
            locationJob.value = scope.launch { refreshLocation() }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CameraPickerEffect.LaunchCamera -> takePicture(effect.uri)
                is CameraPickerEffect.RequestPermission ->{
                    requestPermission(context, AppPermission.LOCATION)
                }
                is CameraPickerEffect.NavigateBackWithGroup -> onPicked(effect.group)
                is CameraPickerEffect.ShowExitDialog -> showExitDialog = true
                is CameraPickerEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold { padding ->
        CameraPickerContent(
            state = state,
            locationLoading = locationLoading,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(padding),
        )

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(stringResource(R.string.camerapicker_exit_dialog_title)) },
                text = { Text(stringResource(R.string.camerapicker_exit_dialog_message, state.images.size)) },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        viewModel.onIntent(CameraPickerIntent.DialogAllow)
                    }) {
                        Text(stringResource(R.string.camerapicker_exit_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    ) {
                        Text(stringResource(R.string.camerapicker_exit_dismiss))
                    }
                },
            )
        }
    }
}