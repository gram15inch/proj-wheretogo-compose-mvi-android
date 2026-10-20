package com.wheretogo.presentation.composable.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhkim139.core.ui.model.ContentPadding
import com.dhkim139.core.ui.screen.FontMaxScale
import com.dhkim139.core.ui.theme.Palette
import com.dhkim139.core.ui.theme.hancomSansFontFamily
import com.dhkim139.core.ui.theme.interBoldFontFamily
import com.dhkim139.core.ui.theme.interFontFamily
import com.google.android.gms.ads.nativead.NativeAd
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.presentation.AD_MAX_FONT_SCALE
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
import com.wheretogo.presentation.model.AdItem

private const val MIN_CHARS = 90
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

@Preview(widthDp = 300, heightDp = 300, backgroundColor = 0xFF000000L, fontScale = 1f)
@Composable
fun MiniAdPreview() {
    Box(modifier = Modifier.padding(5.dp)) {
        MiniAd()
    }
}


@Preview(widthDp = 400, heightDp = 500)
@Composable
fun CardAdPreview() {
    Box(modifier = Modifier.padding(5.dp)) {
        CardAd()
    }
}

@Composable
fun AdaptiveAd(
    elevation: Dp = 8.dp,
    adItem: AdItem?,
    contentPadding: ContentPadding = ContentPadding(),
    cardFirst: Boolean = true
) {
    val adSize = adSize(contentPadding, cardFirst)
    val nativeAd = adItem?.nativeAd as? NativeAd
    Box(modifier = Modifier.padding(
        start = contentPadding.start,
        top = contentPadding.top,
        end = contentPadding.end,
        bottom = contentPadding.bottom,
    )){
        when(adSize)  {
            AdMinSize.Card ->
                CardContainer(elevation, nativeAd)
            AdMinSize.Row ->
                RowContainer(elevation, nativeAd)
            AdMinSize.Mini ->
                MiniContainer(elevation, nativeAd)
            else -> {}
        }
    }
}

@Composable
private fun MiniContainer(elevation:Dp, nativeAd: NativeAd?){
    when {
        nativeAd != null -> {
            MiniAd(
                modifier = Modifier.sizeIn(
                    maxWidth = AdMinSize.Mini.widthDp.dp,
                    maxHeight = AdMinSize.Mini.heightDp.dp
                ),
                elevation = elevation, nativeAd = nativeAd
            )
        }
        else -> {
            MiniAdPlaceholder()
        }
    }
}

@Composable
private fun RowContainer(elevation:Dp, nativeAd: NativeAd?){
    when {
        nativeAd != null -> {
            RowAd(
                modifier = Modifier.sizeIn(
                    maxWidth = AdMinSize.Row.widthDp.dp,
                    maxHeight = AdMinSize.Row.heightDp.dp
                ),
                elevation = elevation, nativeAd = nativeAd
            )
        }
        else -> {
            RowAdPlaceholder()
        }
    }
}

@Composable
private fun CardContainer(elevation:Dp, nativeAd: NativeAd?){
    when {
        nativeAd != null -> {
            CardAd(
                modifier = Modifier.sizeIn(
                    maxWidth = AdMinSize.Card.widthDp.dp,
                    maxHeight = AdMinSize.Card.heightDp.dp
                ),
                elevation = elevation,
                nativeAd = nativeAd
            )
        }

        else -> {
            CardAdPlaceholder()
        }
    }
}


@Composable
private fun CardAd(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
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
            containerColor = Palette.White50,
            contentColor = Palette.Black,
            disabledContainerColor = Palette.Black,
            disabledContentColor = Palette.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView {
            CardAdEffect(nativeAd)

            Column {
                Box {
                    // 광고 이미지
                    AdImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
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
                            text = headline
                        )
                    }

                    Spacer(Modifier.height(2.dp))
                    // 바디
                    FittedNativeBody(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = hp),
                        text = body
                    )
                    // 광고주
                    AdAdvertiser(
                        modifier = Modifier
                            .padding(horizontal = hp)
                            .align(Alignment.End), advertiser
                    )
                    // CTA
                    AdCta(
                        modifier = Modifier
                            .height(48.dp)
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
private fun RowAd(modifier: Modifier = Modifier, elevation: Dp = 8.dp, nativeAd: NativeAd? = null) {
    val isPreview = LocalInspectionMode.current
    val headline = if (isPreview) dummy.headline else nativeAd?.headline
    val body = if (isPreview) dummy.body else nativeAd?.body
    val advertiser = if (isPreview) dummy.advertiser else nativeAd?.advertiser

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardColors(
            containerColor = Palette.White50,
            contentColor = Palette.Black,
            disabledContainerColor = Palette.Black,
            disabledContentColor = Palette.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView {
            val contentRatio = nativeAd?.mediaContent?.aspectRatio ?: (16f / 9f)
            val imageRatio = if (contentRatio > 1.5f) contentRatio else 16f / 9f
            RowAdEffect(nativeAd)
            Row(
                modifier = Modifier
                    .heightIn(max= 160.dp)
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
                            AdHeadLine(Modifier.weight(1f), headline)
                            Spacer(modifier = Modifier.height(2.dp))
                            // 광고 마크
                            AdMark()
                            Spacer(Modifier.width(6.dp))
                        }
                        // 광고 바디
                        FittedNativeBody(
                            modifier = Modifier
                                .weight(1f),
                            text = body
                        )
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


@Composable
private fun MiniAd(modifier: Modifier = Modifier, elevation: Dp = 8.dp, nativeAd: NativeAd? = null) {
    val isPreview = LocalInspectionMode.current
    val headline = if (isPreview) dummy.headline else nativeAd?.headline
    val advertiser = if (isPreview) dummy.advertiser else nativeAd?.advertiser

    Card(
        modifier = modifier.sizeIn(maxWidth = AdMinSize.Mini.widthDp.dp, maxHeight = AdMinSize.Mini.heightDp.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardColors(
            containerColor = Palette.White50,
            contentColor = Palette.Black,
            disabledContainerColor = Palette.Black,
            disabledContentColor = Palette.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView {
            RowAdEffect(nativeAd)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                // 광고 이미지
                AdImage(Modifier.size(120.dp))
                Column(
                    modifier = Modifier.padding(top = 6.dp, start = 6.dp, end = 6.dp, bottom = 4.dp),
                ) {
                    Column (
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // 마크
                            AdMark()

                            // 광고주
                            AdAdvertiser(text = advertiser)
                        }

                        // 광고 헤드라인
                        AdHeadLine(Modifier.fillMaxWidth().heightIn(min= 48.dp), headline)
                    }

                    // 광고 CTA
                    AdCta(modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                        nativeAd?.callToAction)
                }
            }
        }
    }
}

// 요소
@Composable
private fun AdChoice(modifier: Modifier = Modifier) {
    NativeAdChoicesView(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Palette.Blue50, Palette.Green100)
                )
            )
    )
}

@Composable
private fun AdAdvertiser(modifier: Modifier = Modifier, text: String?) {
    if(text==null)
        Spacer(Modifier.height(4.dp))
    else
        NativeAdAdvertiserView(modifier = modifier) {
            FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                Text(
                    text = text, fontSize = 10.sp,
                    color = Palette.Gray280, fontFamily = interFontFamily,
                    style = TextStyle(fontSize = 10.sp, lineHeight = 10.sp)
                )

            }
        }
}

@Composable
private fun AdCta(modifier: Modifier, text: String?) {
    NativeAdCallToActionView(modifier = modifier) {
        Box(
            modifier = Modifier
                .shadow(1.dp, shape = RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Palette.Blue50, Palette.Green50)
                    )
                )
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                Text(
                    text = text ?: "자세히 보기",
                    textAlign = TextAlign.Center,
                    color = Palette.White50,
                    fontSize = 16.sp,
                    fontFamily = interBoldFontFamily
                )
            }
        }
    }
}

@Composable
private fun AdHeadLine(
    modifier: Modifier = Modifier,
    text: String?,
) {
    if (text != null)
        NativeAdHeadlineView(modifier = modifier) {
            FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                Text(
                    text = text,
                    fontSize = 17.sp,
                    color = Palette.Blue500,
                    fontFamily = interBoldFontFamily,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
}

@Composable
private fun AdImage(modifier: Modifier) {
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
private fun AdMark(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Palette.Blue50, Palette.Green100)
                    )
                )
                .padding(horizontal = 2.5.dp, vertical = 1.5.dp)
        ) {
            FontMaxScale {
                Text(
                    text = "Ad",
                    color = Palette.White,
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun FittedNativeBody(
    text: String?,
    style: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )),
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(Modifier.heightIn(max=70.dp)) {
        if (text.isNullOrBlank()) return@BoxWithConstraints

        val measurer = rememberTextMeasurer()
        val w = constraints.maxWidth
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE

        val show = remember(text, style, w, h) {
            shouldShowText(measurer, text, style, w, h)
        }
        if (show) {
            NativeAdBodyView(modifier = modifier) {
                FontMaxScale(maxScale = AD_MAX_FONT_SCALE) {
                    Text(
                        text = text,
                        style = style,
                        fontFamily = hancomSansFontFamily,
                        textAlign = TextAlign.Left
                    )
                }
            }
        }
    }
}

private fun shouldShowText(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    w: Int,
    h: Int,
): Boolean {
    if (w <= 0 || h <= 0 || text.isEmpty()) return false

    val target = if (text.length > MIN_CHARS) {
        text.take(MIN_CHARS) + "…"
    } else {
        text
    }

    val result = measurer.measure(
        target, style, constraints = Constraints(maxWidth = w)
    )

    return result.getLineBottom(result.lineCount - 1) <= h
}