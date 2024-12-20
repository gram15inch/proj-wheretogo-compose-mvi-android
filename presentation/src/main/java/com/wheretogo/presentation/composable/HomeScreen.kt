package com.wheretogo.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.wheretogo.presentation.R
import com.wheretogo.presentation.theme.Black100
import com.wheretogo.presentation.theme.Blue200
import com.wheretogo.presentation.theme.Gray100
import com.wheretogo.presentation.theme.Gray200
import com.wheretogo.presentation.theme.Gray300
import com.wheretogo.presentation.theme.White100
import com.wheretogo.presentation.theme.hancomMalangFontFamily
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.theme.meslolgsFontFamily
import com.wheretogo.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    displayMaxWidth: Dp,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val outPadding = 12.dp
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxHeight()
            .width(displayMaxWidth)
            .padding(outPadding)
            .background(White100)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopBar(displayMaxWidth, onSettingClick = {
            viewModel.settingClick()
        })
        Body(displayMaxWidth - outPadding * 2) { screen ->
            navController.navigate(screen)
        }
        Spacer(modifier = Modifier.weight(1f))
        BottomBar(displayMaxWidth)
    }
}


@Composable
fun TopBar(maxWidth: Dp, onSettingClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(maxWidth)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.where_to_go),
            fontSize = 24.sp,
            fontFamily = hancomMalangFontFamily,
            color = Gray100
        )
        Image(
            painter = painterResource(id = R.drawable.ic_setting), // 이미지 리소스
            contentDescription = "Background Image",
            modifier = Modifier
                .size(28.dp)
                .clickable {
                    onSettingClick()
                }
        )
    }
}

@Composable
fun Body(bodyMaxWidth: Dp, navigate: (String) -> Unit) {
    val gridGap = 12.dp
    Column(verticalArrangement = Arrangement.spacedBy(gridGap)) {
        GridButton(
            3,
            2,
            maxWidth = bodyMaxWidth,
            content = {
                ContentTextImage(
                    stringResource(R.string.drive_main),
                    stringResource(R.string.drive_sub),
                    130.dp,
                    R.raw.lt_togeter
                )
            },
            click = { navigate("drive") }
        )

        val rowWidth = bodyMaxWidth - gridGap
        Row(horizontalArrangement = Arrangement.spacedBy(gridGap)) {
            GridButton(
                3, 1,
                maxWidth = rowWidth,
                content = {
                    ContentTextImage(
                        stringResource(R.string.ranking_main),
                        stringResource(R.string.ranking_sub),
                        0.dp,
                        null
                    )
                },
                click = {})
            GridButton(
                3, 1,
                maxWidth = rowWidth,
                content = {
                    ContentTextImage(
                        stringResource(R.string.bookmark_main),
                        stringResource(R.string.bookmark_sub),
                        75.dp,
                        R.raw.lt_bike
                    )
                },
                click = { navigate("bookmark") })
        }

        GridButton(
            2, 2,
            maxWidth = bodyMaxWidth,
            content = {
                ContentTextImage(
                    stringResource(R.string.visit_first_main),
                    stringResource(R.string.visit_first_sub), 0.dp, null
                )
            },
            click = {})
        GridButton(
            5, 2,
            maxWidth = bodyMaxWidth,
            content = {
                ContentBanner(
                    stringResource(R.string.banner_main),
                    stringResource(R.string.banner_sub)
                )
            },
            click = {})
    }
}

@Composable
fun GridButton(
    row: Int,
    col: Int,
    maxWidth: Dp,
    content: @Composable () -> Unit,
    click: () -> Unit
) {
    val boxWidth = if (col == 1) maxWidth / 2 else maxWidth
    Surface(
        modifier = Modifier
            .width(boxWidth)
            .height(row * 40.dp)
            .clickable { click.invoke() },
        shape = RoundedCornerShape(10.dp), // 둥근 모서리 적용
        color = Color.White, // 배경색 적용
    ) {
        content()
    }
}

@Composable
fun ContentTextImage(title: String, subTitle: String, size: Dp, rawRes: Int?) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            Text(title, color = Gray300, fontSize = 18.sp, fontFamily = meslolgsFontFamily)
            Text(subTitle, color = Gray200, fontSize = 15.sp, fontFamily = hancomSansFontFamily)
        }
        if (rawRes != null) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
            val progress by animateLottieCompositionAsState(composition)
            LottieAnimation(
                modifier = Modifier
                    .size(size)
                    .fillMaxSize()
                    .align(Alignment.BottomEnd),
                composition = composition,
                progress = { progress },
            )
        }
    }
}

@Composable
fun ContentBanner(bannerMain: String, bannerSub: String) {
    Box(
        modifier = Modifier
            .background(Blue200)
            .padding(15.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = bannerSub,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = hancomSansFontFamily
            )
            Text(
                text = bannerMain,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 38.sp),
                color = Color.White, fontSize = 34.sp, fontFamily = hancomSansFontFamily
            )
        }

        Text(
            modifier = Modifier
                .background(color = Black100, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 7.dp)
                .align(Alignment.BottomEnd),
            text = "1/1",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = meslolgsFontFamily
        )
    }

}

@Composable
fun BottomBar(maxWidth: Dp) {
    Column(
        modifier = Modifier
            .width(maxWidth)
            .padding(5.dp),
    ) {
        Text(
            text = stringResource(R.string.contact_banner),
            fontSize = 15.sp,
            fontFamily = hancomSansFontFamily,
            color = Gray200
        )
        Text(
            text = stringResource(R.string.dev_gmail),
            fontSize = 15.sp,
            fontFamily = hancomSansFontFamily,
            color = Gray200
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BodyPreview() {
    Body(400.dp) {}
}

@Preview
@Composable
fun GridButtonPreview() {
    GridButton(
        2,
        2,
        maxWidth = 400.dp,
        { ContentTextImage("메인", "서브", 0.dp, R.raw.lt_togeter) },
        click = {})
}

@Preview
@Composable
fun GrindBannerPreview() {
    GridButton(4, 2, 400.dp, content = {
        ContentBanner(
            stringResource(R.string.banner_main),
            stringResource(R.string.banner_sub)
        )
    }, click = {})
}


@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar(400.dp) {}
}


@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    BottomBar(400.dp)
}
