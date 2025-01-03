package com.wheretogo.presentation.composable.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.wheretogo.presentation.composable.content.CheckpointAddBottomSheet
import com.wheretogo.presentation.composable.content.DescriptionTextField
import com.wheretogo.presentation.feature.ImeStickyBox
import com.wheretogo.presentation.state.BottomSheetState

@Composable
fun CheckPointAddScreen() {
    val state = BottomSheetState(isVisible = true)
    Box(modifier = Modifier.navigationBarsPadding()) {
        Box(modifier = Modifier.size(height = 600.dp, width = 300.dp))
        CheckpointAddBottomSheet(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = state,
            onBottomSheetClose = {},
            onSliderChange = {},
            onImageChange = {}
        )

        ImeStickyBox(modifier = Modifier.align(alignment = Alignment.BottomCenter)) {
            DescriptionTextField(
                modifier = Modifier.heightIn(min = 60.dp),
                isVisible = it > 30.dp,
                focusRequester = state.focusRequester,
                text = state.description,
                onTextChange = {},
                onEnterClick = {}
            )
        }
    }
}


@Preview
@Composable
fun BottomSheetPreview() {
    CheckPointAddScreen()
}


@Composable
fun CheckPermissions(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permission = Manifest.permission.READ_EXTERNAL_STORAGE

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        onPermissionGranted()
    } else {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) onPermissionGranted()
        }

        SideEffect {
            launcher.launch(permission)
        }
    }
}
