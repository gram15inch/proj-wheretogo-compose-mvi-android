package com.wheretogo.presentation.composable

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.getGoogleCredential
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.theme.interFontFamily
import com.wheretogo.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.loginScreenState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(state.isToast) {
        if (state.isToast) {
            Toast.makeText(context.applicationContext, "${state.toastMsg}", Toast.LENGTH_LONG)
                .show()
        }
    }

    LoginContent(
        onGoogleLoginClick = {
            coroutineScope.launch {
                viewModel.signUpAndSignIn(getGoogleCredential(context))
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
                LoginButton(R.drawable.ic_google, R.string.login) {
                    onGoogleLoginClick()
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
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
fun LoginButton(icon: Int, text: Int, onClick: () -> Unit) {
    val round = 20.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(
                border = BorderStroke(1.2.dp, color = colorResource(R.color.gray_848484)),
                shape = RoundedCornerShape(round)
            )
            .clip(RoundedCornerShape(round))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(50.dp),
                painter = painterResource(icon),
                contentDescription = stringResource(R.string.google_logo)
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 30.dp),
                text = stringResource(text),
                fontFamily = hancomSansFontFamily,
                color = colorResource(R.color.gray_6F6F6F),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }

    }
}