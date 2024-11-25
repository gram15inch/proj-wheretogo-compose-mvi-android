package com.wheretogo.presentation.composable

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheretogo.presentation.feature.getGoogleCredential
import com.wheretogo.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.loginScreenState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(state.isToast) {
        if (state.isToast) {
            Toast.makeText(context.applicationContext, "${state.toastMsg}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state.isExit) {
        if (state.isExit) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(alignment = Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LoginButton("구글 로그인") {
                coroutineScope.launch {
                    viewModel.signUpAndSignIn(getGoogleCredential(context))
                }
            }
        }

        Text(modifier = Modifier
            .padding(bottom = 10.dp)
            .align(alignment = Alignment.BottomCenter)
            .clickable {
                viewModel.signInPass()
            }, text = "로그인 없이 앱 둘러보기"
        )
    }

}

@Preview
@Composable
fun LoginButtonPreview() {
    Column(modifier = Modifier.width(500.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LoginButton("카카오 로그인") {}
        LoginButton("네이버 로그인") {}
        LoginButton("구글 로그인") {}
    }
}

@Composable
fun LoginButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(
            modifier = Modifier
                .width(300.dp)
                .height(50.dp), text = text
        )
    }
}