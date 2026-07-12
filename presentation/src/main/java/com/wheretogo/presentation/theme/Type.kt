package com.wheretogo.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R

val hancomSansFontFamily = FontFamily(
    Font(R.font.hancom_sans_semibold_0, FontWeight.SemiBold)
)


val interBoldFontFamily = FontFamily(
    Font(R.font.inter_black, FontWeight.Bold)
)

val interFontFamily = FontFamily(
    Font(R.font.inter_variable_fontt, FontWeight.Medium)
)

val hancomMalangFontFamily = FontFamily(
    Font(R.font.hancom_malangmalang_bold, FontWeight.Bold)
)

val meslolgsFontFamily = FontFamily(
    Font(R.font.meslolgs_nf_regular, FontWeight.Bold)
)


val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

)