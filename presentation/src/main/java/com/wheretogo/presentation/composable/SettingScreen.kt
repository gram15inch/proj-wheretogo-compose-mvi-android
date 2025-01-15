package com.wheretogo.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePublic
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SettingInfoType
import com.wheretogo.presentation.feature.openWeb
import com.wheretogo.presentation.intent.SettingIntent
import com.wheretogo.presentation.parseLogoImgRes
import com.wheretogo.presentation.state.SettingScreenState
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.viewmodel.SettingViewModel

@Composable
fun SettingScreen(navController: NavController, viewModel: SettingViewModel = hiltViewModel()) {
    val state by viewModel.settingScreenState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .navigationBarsPadding()
    ) {
        SettingContent(
            navController = navController,
            settingState = state,
            onUserNameChangeButtonClick = { viewModel.handleIntent(SettingIntent.UsernameChangeClick) },
            onUserDeleteButtonClick = { viewModel.handleIntent(SettingIntent.UserDeleteClick) },
            onWebInfoButtonClick = { viewModel.handleIntent(SettingIntent.InfoClick(it)) },
            onLogoutButtonClick = { viewModel.handleIntent(SettingIntent.LogoutClick) }
        )
    }
}


@Preview
@Composable
fun SettingContentPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        SettingContent(
            navController = null,
            settingState = SettingScreenState().copy(
                isProfile = true,
                profile = Profile(
                    public = ProfilePublic(name = "닉네임")
                )
            )
        )
    }
}

@Composable
fun SettingContent(
    navController: NavController?,
    settingState: SettingScreenState = SettingScreenState(),
    onUserNameChangeButtonClick: () -> Unit = {},
    onLogoutButtonClick: () -> Unit = {},
    onWebInfoButtonClick: (SettingInfoType) -> Unit = {},
    onUserDeleteButtonClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .padding(start = 15.dp, top = 20.dp, bottom = 10.dp)
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController?.navigateUp()
                    }, contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.ic_left),
                    contentDescription = ""
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 20.dp, end = 20.dp)
                .heightIn(min = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            val profile = settingState.profile
            if (settingState.isProfile)
                ProfileSection(
                    name = profile.public.name,
                    authCompany = profile.private.authCompany,
                    onUserNameChangeButtonClick = onUserNameChangeButtonClick,
                    onLogoutButtonClick = onLogoutButtonClick
                )
            else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.3.dp,
                            shape = RoundedCornerShape(16.dp),
                            color = colorResource(R.color.gray_C7C7C7_80)
                        )
                        .clickable {
                            onLogoutButtonClick()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "로그인이 필요합니다",
                            fontFamily = hancomSansFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = colorResource(R.color.gray_C7C7C7_80)
        )
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "정보",
                fontFamily = hancomSansFontFamily,
                color = colorResource(R.color.gray_6F6F6F)
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
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = colorResource(R.color.gray_C7C7C7_80)
        )
        if (settingState.isProfile)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Box(modifier = Modifier
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

}

@Composable
fun ProfileSection(
    name: String,
    authCompany: String,
    onUserNameChangeButtonClick: () -> Unit,
    onLogoutButtonClick: () -> Unit
) {
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
                    Image(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 3.dp),
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = ""
                    )
                }
            }

            Box(modifier = Modifier.padding(start = 3.dp, top = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        modifier = Modifier.size(17.dp),
                        painter = painterResource(parseLogoImgRes(authCompany)),
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier.padding(start = 3.dp),
                        fontFamily = hancomSansFontFamily,
                        fontSize = 15.sp,
                        text = "칭호"
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
}

@Composable
fun InfoButton(
    text: Int,
    icon: Int,
    type: SettingInfoType,
    onInfoButtonClick: (SettingInfoType) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                onInfoButtonClick(type)
                openWeb(context, type.url)
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