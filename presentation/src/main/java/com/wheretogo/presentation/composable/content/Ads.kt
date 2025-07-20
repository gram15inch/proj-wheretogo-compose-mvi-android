package com.wheretogo.presentation.composable.content

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.nativead.NativeAd
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.wheretogo.presentation.AdMinSize
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.ad.NativeAdAdvertiserView
import com.wheretogo.presentation.composable.content.ad.NativeAdBodyView
import com.wheretogo.presentation.composable.content.ad.NativeAdCallToActionView
import com.wheretogo.presentation.composable.content.ad.NativeAdChoicesView
import com.wheretogo.presentation.composable.content.ad.NativeAdHeadlineView
import com.wheretogo.presentation.composable.content.ad.NativeAdImageView
import com.wheretogo.presentation.composable.content.ad.NativeAdView
import com.wheretogo.presentation.theme.Blue100
import com.wheretogo.presentation.theme.Blue50
import com.wheretogo.presentation.theme.Blue500
import com.wheretogo.presentation.theme.Gray300
import com.wheretogo.presentation.theme.Green100
import com.wheretogo.presentation.theme.Green50
import com.wheretogo.presentation.theme.White50
import com.wheretogo.presentation.theme.hancomSansFontFamily
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily

data class AdPreview(val headline:String, val image:Int, val body:String, val advertiser:String)

val dummy = AdPreview(
    headline = "헤드라인",
    image = R.drawable.lg_app,
    body = "광고 예시입니다. 여기는 바디가 오는 구간입니다. 이곳은 여러줄의 텍스트가 오는 공간입니다. 여기는 두줄이상의 텍스트가 옵니다",
    advertiser = "광고주",
)

@Preview(widthDp = 800, heightDp = 200, backgroundColor = 0xFF000000L)
@Composable
fun RowAdPreview(){
    Box(modifier = Modifier.padding(5.dp)){
        RowAd()
    }
}

@Preview(widthDp = 400, heightDp = 500)
@Composable
fun CardAdPreview(){
    Box(modifier = Modifier.padding(5.dp)){
        CardAd()
    }
}

// 광고
@Composable
fun AdaptiveAd(modifier: Modifier=Modifier, elevation: Dp= 8.dp, nativeAd: NativeAd?){
    val isPreview = LocalInspectionMode.current
    val adSize = adSize()


    Box(modifier = modifier) {
        when(adSize){
            AdMinSize.Row -> {
                val height = 95.dp
                Box(modifier=Modifier.heightIn(min = height)){
                    when {
                        isPreview -> RowAd(elevation = elevation, nativeAd = nativeAd)
                        nativeAd != null -> RowAd(elevation = elevation, nativeAd = nativeAd)
                        else -> RowAdPlaceholder()
                    }
                }
            }

            AdMinSize.Card -> {
                val height = 335.dp
                Box(modifier=Modifier.heightIn(min = height)){
                    when {
                        isPreview -> CardAd(elevation = elevation, nativeAd = nativeAd)
                        nativeAd != null -> CardAd(elevation = elevation, nativeAd = nativeAd)
                        else -> CardAdPlaceholder()
                    }
                }
            }
            else->{}
        }
    }
}

@Composable
fun CardAd(modifier: Modifier = Modifier, elevation: Dp= 8.dp, nativeAd: NativeAd?=null){
    val isPreview = LocalInspectionMode.current
    val image = if(isPreview) null else nativeAd?.images?.firstOrNull()?.drawable
    val headline = if(isPreview) dummy.headline else nativeAd?.headline
    val body = if(isPreview) dummy.body else nativeAd?.body
    val advertiser = if(isPreview) dummy.advertiser else nativeAd?.advertiser

    Card(
        modifier = modifier
            .widthIn(max = 500.dp),
        colors = CardColors(
            containerColor = White50,
            contentColor = Color.Black,
            disabledContainerColor = Color.Black,
            disabledContentColor = Color.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView(modifier = Modifier.height(IntrinsicSize.Min)) {
            Column {
                Box {
                    // 광고 이미지
                    AdImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f), image
                    )
                    // 광고 마크
                    AdMark(modifier = Modifier.padding(8.dp))
                    // 광고 초이스
                    AdChoice(
                        modifier = Modifier
                            .align(alignment = Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .padding(bottom = 2.dp),
                ) {
                    Spacer(Modifier.height(1.dp))
                    Row {
                        // 헤드라인
                        AdHeadLine(modifier = Modifier.weight(1f), text = headline)
                        // 광고주
                        AdAdvertiser(modifier = Modifier, advertiser)
                    }

                    Spacer(Modifier.height(2.dp))
                    // 바디
                    AdBody(text = body)
                    Spacer(Modifier.height(4.dp))
                    // CTA
                    AdCta(modifier = Modifier.fillMaxWidth(), nativeAd?.callToAction)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun RowAd(modifier: Modifier = Modifier, elevation: Dp = 8.dp, nativeAd: NativeAd? = null){
    val isPreview = LocalInspectionMode.current
    val image = if(isPreview) null else nativeAd?.images?.firstOrNull()?.drawable
    val headline = if(isPreview) dummy.headline else nativeAd?.headline
    val body = if(isPreview) dummy.body else nativeAd?.body
    val advertiser = if(isPreview) dummy.advertiser else nativeAd?.advertiser

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardColors(
            containerColor = White50,
            contentColor = Color.Black,
            disabledContainerColor = Color.Black,
            disabledContentColor = Color.Black,
        ),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        NativeAdView(modifier=Modifier.height(IntrinsicSize.Min)) {
            Row(modifier = Modifier) {
                // 광고 이미지
                AdImage(Modifier
                    .width(160.dp)
                    .aspectRatio(16f / 9f), image)
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .padding(start = 4.dp),
                ) {
                    Row {
                        // 광고 헤드라인
                        AdHeadLine(Modifier.weight(1f), headline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // 광고주
                            AdAdvertiser(text = advertiser)
                            // 광고 마크
                            AdMark()
                            // 광고 초이스
                            AdChoice(modifier = Modifier)
                        }
                    }
                    Row(modifier=Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ){
                        // 광고 바디
                        AdBody(modifier=Modifier
                            .weight(0.7f),
                            text = body)
                        Spacer(Modifier.width(6.dp))
                        // 광고 CTA
                        AdCta(Modifier
                            .weight(0.45f)
                            .fillMaxWidth(), nativeAd?.callToAction)

                    }
                }
            }
        }
    }
}


// 요소
@Composable
fun AdChoice(modifier: Modifier = Modifier){
    NativeAdChoicesView(modifier
        .clip(RoundedCornerShape(4.dp))
        .background(
            brush = Brush.horizontalGradient(
                colors = listOf(Blue100, Green100)
            )
        )
    )
}

@Composable
fun AdAdvertiser(modifier: Modifier=Modifier, text:String?){
    if(text!=null)
        NativeAdAdvertiserView(modifier =modifier,) {
            Text(text = text, fontSize = 10.sp,
                color = colorResource(R.color.gray_6F6F6F), fontFamily = interFontFamily,
                style = TextStyle(fontSize = 10.sp, lineHeight = 10.sp))
        }
}

@Composable
fun AdCta(modifier:Modifier, text: String?){
    Box(modifier=modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        NativeAdCallToActionView(modifier=modifier
            .shadow(1.dp, shape = RoundedCornerShape(10.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Blue50, Green50)
                )
            )
        ) {
            Box(
                modifier= Modifier.clickable {},
                contentAlignment = Alignment.Center){
                Text(modifier= Modifier.padding(8.dp),text= text?:"자세히 보기", color = White50, fontSize = 16.sp, fontFamily = interBoldFontFamily)
            }
        }
    }
}

@Composable
fun AdBody(modifier: Modifier = Modifier, text:String?){
    if(text!=null)
        NativeAdBodyView(modifier = modifier){
            Text(text = text, color = Gray300, fontFamily = hancomSansFontFamily,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp))
        }
}

@Composable
fun AdHeadLine(modifier: Modifier=Modifier, text: String?){
    if(text!=null)
      NativeAdHeadlineView(modifier=modifier) {
          Text(text = text,
              fontSize = 17.sp,
              color = Blue500 ,fontFamily = interBoldFontFamily)
      }
}

@Composable
fun AdImage(modifier: Modifier,image: Drawable?){
    val isPreview = LocalInspectionMode.current

    if(isPreview || image!=null)
        NativeAdImageView(modifier=modifier) {
            GlideImage(
                imageModel = { image },
                previewPlaceholder = painterResource(R.drawable.lg_app),
                imageOptions = ImageOptions(contentScale = ContentScale.Crop)
            )
        }
}

@Composable
fun AdMark(modifier: Modifier = Modifier){
    Box(modifier = modifier){
        Box(Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Blue100, Green100)
                )
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)) {
            Text(text = "Ad", color = Color.White, fontFamily = interFontFamily, fontSize = 12.sp, lineHeight = 12.sp)
        }
    }
}

