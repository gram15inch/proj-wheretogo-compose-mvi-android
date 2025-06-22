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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.BANNER_URL
import com.wheretogo.presentation.R
import com.wheretogo.presentation.SettingInfoType
import com.wheretogo.presentation.feature.openWeb
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
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val outPadding = 12.dp
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(outPadding)
            .background(White100)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopBar(onSettingClick = {
            navController.navigate(AppScreen.Setting.toString())
        })
        Body { screen ->
            navController.navigate(screen)
        }
        Spacer(modifier = Modifier.weight(1f))
        BottomBar()
    }
}


@Composable
fun TopBar(onSettingClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 3.dp),
            text = stringResource(R.string.where_to_go),
            fontSize = 24.sp,
            fontFamily = hancomMalangFontFamily,
            color = Gray100
        )
        Image(
            painter = painterResource(id = R.drawable.ic_menu_burger), // 이미지 리소스
            contentDescription = "Background Image",
            modifier = Modifier
                .padding(end = 8.dp)
                .size(26.dp)
                .clickable {
                    onSettingClick()
                }
        )
    }
}

@Composable
fun Body(navigate: (String) -> Unit) {
    val context = LocalContext.current
    val gridGap = 12.dp
    Column(verticalArrangement = Arrangement.spacedBy(gridGap)) {
        GridButton(
            3,
            2,
            content = {
                ContentTextImage(
                    stringResource(R.string.drive_main),
                    stringResource(R.string.drive_sub),
                    130.dp,
                    R.raw.lt_togeter
                )
            },
            click = { navigate(AppScreen.Drive.toString()) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(gridGap)) {
            GridButton(
                3, 2,
                content = {
                    ContentTextImage(
                        stringResource(R.string.course_add_main),
                        stringResource(R.string.course_add_sub),
                        0.dp,
                        null
                    )
                },
                click = {
                    navigate(AppScreen.CourseAdd.toString())
                })
            /*   GridButton(
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
                   click = { navigate("bookmark") })*/
        }

        GridButton(
            2, 2,
            content = {
                ContentTextImage(
                    stringResource(R.string.visit_first_main),
                    stringResource(R.string.visit_first_sub), 0.dp, null
                )
            },
            click = {
                openWeb(context, SettingInfoType.GUIDE.url)
            })
        GridButton(
            5, 2,
            content = {
                ContentBanner(
                    stringResource(R.string.banner_main),
                    stringResource(R.string.banner_sub)
                )
            },
            click = {
                openWeb(context, BANNER_URL)
            })
    }
}

@Composable
fun GridButton(
    row: Int,
    col: Int,
    content: @Composable () -> Unit,
    click: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(row * 40.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable { click.invoke() },
        color = Color.White,
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
fun BottomBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
    Surface(modifier = Modifier.width(400.dp)) {
        Body {}
    }
}

@Preview
@Composable
fun GridButtonPreview() {
    Surface(modifier = Modifier.width(400.dp)) {
        GridButton(
            2,
            2,
            { ContentTextImage("메인", "서브", 0.dp, R.raw.lt_togeter) },
            click = {})
    }
}

@Preview
@Composable
fun GrindBannerPreview() {
    Surface(modifier = Modifier.width(400.dp)) {
        GridButton(4, 2, content = {
            ContentBanner(
                stringResource(R.string.banner_main),
                stringResource(R.string.banner_sub)
            )
        }, click = {})
    }
}


@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    Surface(modifier = Modifier.width(400.dp)) {

        TopBar {}
    }
}


@Preview(showBackground = true)
@Composable
fun BottomBarPreview() {
    Surface(modifier = Modifier.width(400.dp)) {
        BottomBar()
    }
}
