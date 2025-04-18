package com.wheretogo.presentation.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCbrt
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.domain.AuthType
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.model.dummy.getProfileDummy
import com.wheretogo.domain.toAuthProfile
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.DelayLottieAnimation
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.googleAuthOnDevice
import com.wheretogo.presentation.state.LoginScreenState
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.loginScreenState.collectAsState()
    val context = LocalContext.current
    val coroutine  = rememberCoroutineScope()
    BackHandler {} // 로그인창 뒤로가기 막기

    LoginContent(
        state,
        onGoogleLoginClick = {
            coroutine.launch {
                if(BuildConfig.DEBUG)
                    viewModel.signUpAndSignIn(
                        AuthRequest(
                            authType = AuthType.PROFILE,
                            authProfile = getProfileDummy()[0].toAuthProfile()
                        )
                    )
                else
                    viewModel.signUpAndSignIn(googleAuthOnDevice(viewModel.getGoogleIdOption, context))
            }
        },
        onLoginPassClick = {
            viewModel.signInPass()
        }
    )

}

@Preview
@Composable
fun LoginContentPreview() {
    Column(modifier = Modifier.height(800.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LoginContent()
    }
}

@Composable
fun LoginContent(
    state: LoginScreenState = LoginScreenState(),
    onGoogleLoginClick: () -> Unit = {},
    onLoginPassClick: () -> Unit = {}
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Box(
        modifier = Modifier
            .background(Color.White)
            .consumptionEvent()
            .systemBarsPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(horizontal = 15.dp)
                .heightIn(max = 600.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (screenHeight >= 300.dp)
                    Image(
                        modifier = Modifier
                            .size(75.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        painter = painterResource(R.drawable.lg_app),
                        contentDescription = "logo"
                    )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                GoogleLoginButton(state.isLoading) {
                    onGoogleLoginClick()
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
        if (!state.isLoading)
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(alignment = Alignment.BottomCenter)
                    .clickable {
                        onLoginPassClick()
                    },
                text = stringResource(R.string.explore_without_login),
                fontFamily = interFontFamily,
                color = colorResource(R.color.gray_6F6F6F)
            )
    }
}

@Composable
fun GoogleLoginButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (!isLoading)
            Image(
                modifier = Modifier
                    .width(250.dp)
                    .clickable {
                        onClick()
                    },
                painter = painterResource(R.drawable.ic_google_cn_light),
                contentScale = ContentScale.FillWidth,
                contentDescription = ""
            )
        else
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .border(
                        width = 1.2.dp,
                        color = colorResource(R.color.gray_848484),
                        shape = RoundedCornerShape(4.dp)
                    ), contentAlignment = Alignment.Center
            ) {
                DelayLottieAnimation(
                    modifier = Modifier
                        .size(50.dp),
                    ltRes = R.raw.lt_loading,
                    isVisible = true,
                    delay = 300
                )
            }

    }
}