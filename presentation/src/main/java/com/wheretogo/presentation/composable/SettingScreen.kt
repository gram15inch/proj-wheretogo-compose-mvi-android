package com.wheretogo.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SettingInfoType
import com.wheretogo.presentation.composable.content.AdaptiveAd
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.composable.effect.AppEventReceiveEffect
import com.wheretogo.presentation.composable.effect.LifecycleDisposer
import com.wheretogo.presentation.feature.openActivity
import com.wheretogo.presentation.feature.openWeb
import com.wheretogo.presentation.intent.SettingIntent
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.parseLogoImgRes
import com.wheretogo.presentation.state.SettingScreenState
import com.wheretogo.presentation.theme.Gray280
import com.wheretogo.presentation.theme.Gray6080
import com.wheretogo.presentation.theme.PrimeBlue
import com.wheretogo.presentation.theme.White50
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.viewmodel.SettingViewModel

@Composable
fun SettingScreen(navController: NavController, viewModel: SettingViewModel = hiltViewModel()) {
    val state by viewModel.settingScreenState.collectAsState()
    Scaffold(
        modifier = Modifier,
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    ) { systemBar ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White50)
                .padding(systemBar)
        )
        {
            SettingTopBar(navController)
            SettingContent(
                settingState = state,
                onEmptyProfileClick = { viewModel.handleIntent(SettingIntent.EmptyProfileClick) },
                onUserNameChangeButtonClick = { viewModel.handleIntent(SettingIntent.UsernameChangeClick) },
                onUserDeleteButtonClick = { viewModel.handleIntent(SettingIntent.UserDeleteClick) },
                onWebInfoButtonClick = { viewModel.handleIntent(SettingIntent.InfoClick(it)) },
                onLogoutButtonClick = { viewModel.handleIntent(SettingIntent.LogoutClick) },
            )
        }
    }
    if (state.isDialog)
        SettingDialog(
            isLoading = state.isLoading,
            onDialogAnswer = { viewModel.handleIntent(SettingIntent.DialogAnswer(it)) },
        )
    LifecycleDisposer(
        onEventChange = { viewModel.handleIntent(SettingIntent.LifecycleChange(it)) }
    )

    AppEventReceiveEffect(
        onReceive = { event, result -> viewModel.handleIntent(SettingIntent.EventReceive(event, result)) }
    )
}

@Composable
fun SettingTopBar(navController: NavController) {
    Box(
        modifier = Modifier
            .padding(start = 15.dp, top = 10.dp, bottom = 10.dp)
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable {
                    navController.navigateUp()
                }, contentAlignment = Alignment.CenterStart
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(R.drawable.ic_left),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun SettingContent(
    settingState: SettingScreenState = SettingScreenState(),
    onEmptyProfileClick: () -> Unit = {},
    onUserNameChangeButtonClick: () -> Unit = {},
    onLogoutButtonClick: () -> Unit = {},
    onWebInfoButtonClick: (SettingInfoType) -> Unit = {},
    onUserDeleteButtonClick: () -> Unit = {}

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ProfileSection(
            isProfile = settingState.isProfile,
            isAdminIcon = settingState.profile.private.isAdmin,
            name = settingState.profile.name,
            authCompany = settingState.profile.private.authCompany,
            mail = settingState.profile.private.mail,
            onEmptyProfileClick = onEmptyProfileClick,
            onUserNameChangeButtonClick = onUserNameChangeButtonClick,
            onLogoutButtonClick = onLogoutButtonClick
        )
        AdSection(settingState.adItemGroup)
        InfoSection(onWebInfoButtonClick)
        if (settingState.isProfile) {
            UserDeleteSection(onUserDeleteButtonClick)
        }
    }
}

@Composable
fun ProfileSection(
    isProfile: Boolean,
    isAdminIcon: Boolean,
    name: String,
    authCompany: String,
    mail: String,
    onEmptyProfileClick: () -> Unit = {},
    onUserNameChangeButtonClick: () -> Unit,
    onLogoutButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, bottom = 10.dp, end = 20.dp)
            .heightIn(min = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isProfile) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.clickable {
                        onUserNameChangeButtonClick()
                    }) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontFamily = hancomMalangFontFamily,
                                fontSize = 24.sp
                            )
                            if(isAdminIcon)
                                Image(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 3.dp),
                                    painter = painterResource(R.drawable.ic_edit),
                                    contentDescription = ""
                                )
                        }
                    }

                    Box(modifier = Modifier.padding(start = 3.dp, top = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                modifier = Modifier
                                    .size(17.dp)
                                    .clip(CircleShape),
                                painter = painterResource(parseLogoImgRes(authCompany)),
                                contentDescription = ""
                            )
                            Text(
                                modifier = Modifier.padding(start = 5.dp),
                                fontFamily = hancomSansFontFamily,
                                fontSize = 15.sp,
                                text = mail
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .clickable {
                            onLogoutButtonClick()
                        },
                ) {
                    Text(
                        modifier = Modifier.padding(5.dp),
                        text = stringResource(R.string.logout),
                        fontFamily = hancomSansFontFamily
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.3.dp,
                        shape = RoundedCornerShape(16.dp),
                        color = Gray6080
                    )
                    .clickable {
                        onEmptyProfileClick()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.need_login),
                        fontFamily = hancomSansFontFamily,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdSection(adItemGroup: List<AdItem> = emptyList()) {
    val nativeAd = adItemGroup.firstOrNull()?.nativeAd

    Box(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()) {
        AdaptiveAd(elevation = 4.dp, nativeAd = nativeAd)
    }

}


@Composable
fun InfoSection(
    onWebInfoButtonClick: (SettingInfoType) -> Unit = {},
) {
    Column {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = stringResource(R.string.info),
                fontFamily = hancomSansFontFamily,
                color = Gray280
            )
        }
        Box(
            modifier = Modifier
                .padding(bottom = 15.dp, start = 18.dp, end = 18.dp)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoButton(
                    R.string.terms,
                    R.drawable.ic_terms,
                    SettingInfoType.TERMS,
                    onWebInfoButtonClick
                )
                InfoButton(
                    R.string.privacy,
                    R.drawable.ic_privacy,
                    SettingInfoType.PRIVACY,
                    onWebInfoButtonClick
                )
                InfoButton(
                    R.string.open_licence,
                    R.drawable.ic_licence,
                    SettingInfoType.LICENCE,
                    onWebInfoButtonClick
                )
                InfoButton(
                    R.string.map_legal_notice,
                    R.drawable.ic_book,
                    SettingInfoType.LegalNotice
                )
                InfoButton(
                    R.string.map_open_source,
                    R.drawable.ic_explore,
                    SettingInfoType.OpenSourceLicense
                )
            }
        }
    }
}


@Composable
fun UserDeleteSection(onUserDeleteButtonClick: () -> Unit = {}) {
    Column (modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionDivider()
        Box(
            modifier = Modifier
                .padding(5.dp)
                .clickable {
                    onUserDeleteButtonClick()
                }) {
            Text(
                modifier = Modifier.padding(5.dp),
                text = stringResource(R.string.delete_user),
                fontFamily = hancomSansFontFamily,
                color = Color.Red
            )
        }
    }
}


@Composable
fun InfoButton(
    text: Int,
    icon: Int,
    type: SettingInfoType,
    onInfoButtonClick: (SettingInfoType) -> Unit = {}
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                onInfoButtonClick(type)
                when (type) {
                    SettingInfoType.LegalNotice -> {
                        openActivity(context, type.url)
                    }

                    SettingInfoType.OpenSourceLicense -> {
                        openActivity(context, type.url)
                    }

                    else -> {
                        openWeb(context, type.url)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(30.dp),
                painter = painterResource(icon),
                contentDescription = stringResource(text)
            )
            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                text = stringResource(text),
                fontFamily = hancomSansFontFamily,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 2.dp,
        color = Gray6080
    )
}

@Composable
fun SettingDialog(
    isLoading: Boolean,
    onDialogAnswer: (Boolean) -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = {
            onDialogAnswer(false)
        },
        title = { Text(stringResource(R.string.user_delete_request)) },
        text = {
            if (isLoading) {
                DelayLottieAnimation(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    ltRes = R.raw.lt_loading,
                    isVisible = true
                )
            } else
                Text(stringResource(R.string.user_delete_confirm))
        },
        containerColor = Color.White,
        confirmButton = {
            if (!isLoading)
                TextButton(onClick = {
                    onDialogAnswer(false)
                }) {
                    Text(
                        color = PrimeBlue,
                        text = stringResource(R.string.turn_around)
                    )
                }
        },
        dismissButton = {
            if (!isLoading)
                TextButton(onClick = {
                    onDialogAnswer(true)
                }) {
                    Text(color = Color.Gray, text = stringResource(R.string.keep_going))
                }
        }
    )
}

@Preview("landscape", widthDp = 800, heightDp = 380)
@Preview(name = "portrait", widthDp = 380, heightDp = 800)
@Composable
fun SettingContentPreview() {
    SettingContent(
        settingState = SettingScreenState().copy(
            isProfile = true,
            profile = Profile(
                name = "어디갈까",
                private = ProfilePrivate(
                    isAdmin = true,
                    mail = "wheretogohelp@gmail.com"
                )
            ),
            isLoading = false,
            isDialog = false
        )
    )
}