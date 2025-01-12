package com.wheretogo.presentation.composable

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePublic
import com.wheretogo.presentation.InfoType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.parseLogoImgRes
import com.wheretogo.presentation.state.SettingScreenState
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.viewmodel.SettingViewModel

@Composable
fun SettingScreen(viewModel: SettingViewModel = hiltViewModel()) {
    val state by viewModel.settingScreenState.collectAsState()
    SettingContent(
        state,
        onNickNameChangeButtonClick = {},
        onUserDeleteButtonClick = {},
        onWebInfoButtonClick = { },
        onLogoutButtonClick = { }
    )
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
            settingState = SettingScreenState().copy(
                profile = Profile(
                    public = ProfilePublic(name = "닉네임")
                )
            )
        )
    }
}

@Composable
fun SettingContent(
    settingState: SettingScreenState = SettingScreenState(),
    onNickNameChangeButtonClick: () -> Unit = {},
    onLogoutButtonClick: () -> Unit = {},
    onWebInfoButtonClick: (InfoType) -> Unit = {},
    onUserDeleteButtonClick: () -> Unit = {},

    ) {
    Column {
        Box(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.clickable {
                        onNickNameChangeButtonClick()
                    }) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = settingState.profile.public.name,
                                fontFamily = hancomMalangFontFamily,
                                fontSize = 20.sp
                            )
                            Image(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(start = 3.dp),
                                painter = painterResource(R.drawable.ic_marker_add),
                                contentDescription = ""
                            )
                        }
                    }

                    Box(modifier = Modifier.padding(start = 3.dp, top = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                modifier = Modifier.size(17.dp),
                                painter = painterResource(parseLogoImgRes(settingState.profile.private.authCompany)),
                                contentDescription = ""
                            )
                            Text(
                                modifier = Modifier.padding(start = 3.dp),
                                fontFamily = hancomMalangFontFamily,
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
                        fontFamily = hancomMalangFontFamily
                    )
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
                    R.drawable.ic_picture,
                    InfoType.TERMS,
                    onWebInfoButtonClick
                )
                InfoButton(
                    R.string.privacy,
                    R.drawable.ic_picture,
                    InfoType.PRIVACY,
                    onWebInfoButtonClick
                )
                InfoButton(
                    R.string.open_licence,
                    R.drawable.ic_picture,
                    InfoType.LICENCE,
                    onWebInfoButtonClick
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 2.dp,
            color = colorResource(R.color.gray_C7C7C7_80)
        )
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
fun InfoButton(text: Int, icon: Int, type: InfoType, onInfoButtonClick: (InfoType) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                onInfoButtonClick(type)
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
fun WebViewScreen(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}