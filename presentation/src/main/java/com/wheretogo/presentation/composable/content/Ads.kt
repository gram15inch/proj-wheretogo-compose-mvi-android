package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.nativead.NativeAd
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.presentation.AD_MAX_FONT_SCALE
import com.wheretogo.presentation.AdMinSize
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.ad.NativeAdAdvertiserView
import com.wheretogo.presentation.composable.content.ad.NativeAdBodyView
import com.wheretogo.presentation.composable.content.ad.NativeAdCallToActionView
import com.wheretogo.presentation.composable.content.ad.NativeAdChoicesView
import com.wheretogo.presentation.composable.content.ad.NativeAdHeadlineView
import com.wheretogo.presentation.composable.content.ad.NativeAdMediaView
import com.wheretogo.presentation.composable.content.ad.NativeAdView
import com.wheretogo.presentation.composable.effect.CardAdEffect
import com.wheretogo.presentation.composable.effect.RowAdEffect
import com.wheretogo.presentation.feature.FontMaxScale
import com.wheretogo.presentation.theme.Blue100
import com.wheretogo.presentation.theme.Blue50
import com.wheretogo.presentation.theme.Blue500
import com.wheretogo.presentation.theme.Gray280
import com.wheretogo.presentation.theme.Gray300
import com.wheretogo.presentation.theme.Green100
import com.wheretogo.presentation.theme.Green50
import com.wheretogo.presentation.theme.White50
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily

data class AdPreview(val headline: String, val image: Int, val body: String, val advertiser: String)

val dummy = AdPreview(
    headline = "헤드라인",
    image = R.drawable.lg_app,
    body = "광고 예시입니다. 여기는 바디가 오는 구간입니다. 이곳은 여러줄의 텍스트가 오는 공간입니다. 여기는 두줄이상의 텍스트가 옵니다",
    advertiser = "광고주",
)

@Preview(widthDp = 800, heightDp = 200, backgroundColor = 0xFF000000L)
@Composable
fun RowAdPreview() {
    Box(modifier = Modifier.padding(5.dp)) {
        RowAd()
    }
}

@Preview(widthDp = 400, heightDp = 500)
@Composable
fun CardAdPreview() {
    Box(modifier = Modifier.padding(5.dp)) {
        CardAd()
    }
}

// 광고
@Composable
fun AdaptiveAd(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    isCompact: Boolean = false,
    nativeAd: NativeAd?
) {
    val adSize = adSize()

    Box(modifier = modifier) {
        when (adSize) {
            AdMinSize.Row -> {
                when {
                    isCompact -> { }
                    nativeAd != null -> {
                        RowAd(elevation = elevation, nativeAd = nativeAd)
                    }
                    else -> {
                        RowAdPlaceholder()
                    }
                }
            }

            AdMinSize.Card -> {
                when {
                    nativeAd != null -> {
                        CardAd(
                            elevation = elevation,
                            isCompact = isCompact,
                            nativeAd = nativeAd
                        )
                    }

                    else -> {
                        CardAdPlaceholder()
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
fun CardAd(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    isCompact: Boolean = false,
    nativeAd: NativeAd? = null
) {
    val isPreview = LocalInspectionMode.current
    val headline = if (isPreview) dummy.headline else nativeAd?.headline
    val body = if (isPreview) dummy.body else nativeAd?.body
    val advertiser = if (isPreview) dummy.advertiser else nativeAd?.advertiser

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardColors(
            containerColor = White50,
            contentColor = Color.Black,
            disabledContainerColor = Color.Black,
            disabledContentColor = Color.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView {
            var bodyMaxLines by remember { mutableIntStateOf(if (isCompact) 0 else 3) }
            var tiltLines by remember { mutableIntStateOf(-1) }

            CardAdEffect(nativeAd, isCompact, tiltLines) { maxLine ->
                bodyMaxLines = maxLine
            }

            Column {
                val contentRatio = nativeAd?.mediaContent?.aspectRatio ?: (16f / 9f)
                val imageRatio = if (contentRatio > 1.5f) contentRatio else 16f / 9f
                Box {
                    // 광고 이미지
                    AdImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageRatio)
                    )
                    // 광고 마크
                    AdMark(modifier = Modifier.padding(4.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    val hp = 12.dp
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.padding(horizontal = hp)) {
                        // 헤드라인
                        AdHeadLine(
                            modifier = Modifier.weight(1f),
                            text = headline
                        ) { layout ->
                            tiltLines = layout.lineCount
                        }
                    }

                    Spacer(Modifier.height(2.dp))
                    // 바디
                    if (bodyMaxLines > 0) {
                        AdBody(
                            modifier = Modifier
                                .padding(horizontal = hp),
                            maxLine = bodyMaxLines,
                            text = body
                        )
                    }
                    // 광고주
                    AdAdvertiser(
                        modifier = Modifier
                            .padding(horizontal = hp)
                            .align(Alignment.End), advertiser
                    )
                    // CTA
                    AdCta(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(), nativeAd?.callToAction
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

    }
}

@Composable
fun RowAd(modifier: Modifier = Modifier, elevation: Dp = 8.dp, nativeAd: NativeAd? = null) {
    val isPreview = LocalInspectionMode.current
    val headline = if (isPreview) dummy.headline else nativeAd?.headline
    val body = if (isPreview) dummy.body else nativeAd?.body
    val advertiser = if (isPreview) dummy.advertiser else nativeAd?.advertiser

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardColors(
            containerColor = White50,
            contentColor = Color.Black,
            disabledContainerColor = Color.Black,
            disabledContentColor = Color.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView {
            val contentRatio = nativeAd?.mediaContent?.aspectRatio ?: (16f / 9f)
            val imageRatio = if (contentRatio > 1.5f) contentRatio else 16f / 9f
            RowAdEffect(nativeAd)
            Row(
                modifier = Modifier
                    .height(155.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 광고 이미지
                AdImage(
                    Modifier
                        .aspectRatio(imageRatio)
                )
                Column(
                    modifier = Modifier.padding(6.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 광고 헤드라인
                            AdHeadLine(Modifier.weight(1f), headline) {}
                            Spacer(modifier = Modifier.height(2.dp))
                            // 광고 마크
                            AdMark()
                            Spacer(Modifier.width(6.dp))
                        }
                        // 광고 바디
                        AdBody(modifier = Modifier.weight(1f), text = body)
                    }
                    // 광고주
                    AdAdvertiser(modifier = Modifier.align(Alignment.End), text = advertiser)
                    // 광고 CTA
                    AdCta(modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp), nativeAd?.callToAction)
                }
            }
        }
    }
}


// 요소
@Composable
fun AdChoice(modifier: Modifier = Modifier) {
    NativeAdChoicesView(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Blue100, Green100)
                )
            )
    )
}

@Composable
fun AdAdvertiser(modifier: Modifier = Modifier, text: String?) {
    NativeAdAdvertiserView(modifier = modifier) {
        FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
            Text(
                text = text ?: "", fontSize = 10.sp,
                color = Gray280, fontFamily = interFontFamily,
                style = TextStyle(fontSize = 10.sp, lineHeight = 10.sp)
            )

        }
    }
}

@Composable
fun AdCta(modifier: Modifier, text: String?) {
    NativeAdCallToActionView(modifier = modifier) {
        Box(
            modifier = Modifier
                .shadow(1.dp, shape = RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Blue50, Green50)
                    )
                )
                .heightIn(min = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                Text(
                    text = text ?: "자세히 보기",
                    textAlign = TextAlign.Center,
                    color = White50,
                    fontSize = 16.sp,
                    fontFamily = interBoldFontFamily
                )
            }
        }
    }
}

@Composable
fun AdBody(modifier: Modifier = Modifier, maxLine: Int = 3, text: String?) {
    if (text != null)
        NativeAdBodyView(modifier = modifier) {
            Box(contentAlignment = Alignment.CenterStart) {
                FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                    Text(
                        modifier = Modifier,
                        text = text,
                        color = Gray300,
                        fontFamily = hancomSansFontFamily,
                        maxLines = maxLine,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
                    )

                }
            }
        }
}

@Composable
fun AdHeadLine(
    modifier: Modifier = Modifier,
    text: String?,
    textLayout: (TextLayoutResult) -> Unit
) {
    if (text != null)
        NativeAdHeadlineView(modifier = modifier) {
            FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                Text(
                    text = text,
                    fontSize = 17.sp,
                    color = Blue500, fontFamily = interBoldFontFamily,
                    onTextLayout = textLayout
                )
            }
        }
}

@Composable
fun AdImage(modifier: Modifier) {
    val isPreview = LocalInspectionMode.current
    if (isPreview)
        GlideImage(
            modifier = modifier,
            imageModel = { },
            previewPlaceholder = painterResource(R.drawable.lg_app),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
        )
    else
        NativeAdMediaView(modifier)
}

@Composable
fun AdMark(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Blue100, Green100)
                    )
                )
                .padding(horizontal = 2.5.dp, vertical = 1.5.dp)
        ) {
            FontMaxScale {
                Text(
                    text = "Ad",
                    color = Color.White,
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

