package com.wheretogo.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val tint: Color,
    val container: Color,
)
object Palette {

    // Brand / Blue
    val Blue30 = Color(0xFFD4E3FF)
    val Blue50 = Color(0xFFBAD2FA)
    val Blue100 = Color(0xFFA4C8FF)
    val Blue200 = Color(0xFF6895C2)
    val Blue300 = Color(0xFF0065BD)
    val Blue400 = Color(0xFF0046A7)
    val Blue500 = Color(0xFF182A56)
    val Blue600 = Color(0xFF214876)

    val PrimeBlue = Color(0xFF509BDC)
    val MutedBlue = Color(0xFF3A6EA5)

    // Purple / Pink
    val Purple200 = Color(0xFFBB86FC)
    val Pink80 = Color(0xFFEFB8C8)

    // Teal
    val Teal200 = Color(0xFF03DAC5)
    val Teal700 = Color(0xFF018786)

    // Green
    val Green50 = Color(0xFFCCE8CC)
    val Green100 = Color(0xFFB9D7B9)

    // Wood
    val Warning = ExtendedColors(
        tint =  Color(0xFF633806),
        container = Color(0xFFFAEEDA)
    )

    // Neutral - White
    val White = Color(0xFFFFFFFF)
    val White50 = Color(0xFFFAFAFA)
    val White100 = Color(0xFFF9F9F9)

    // Neutral - Gray
    val Gray50 = Color(0xFFF6F6F6)
    val Gray100 = Color(0xFFC7C7C7)
    val Gray150 = Color(0xFFB9B9B9)
    val Gray200 = Color(0xFF999999)
    val Gray250 = Color(0xFF848484)
    val Gray280 = Color(0xFF6F6F6F)
    val Gray300 = Color(0xFF525252)
    val Gray320 = Color(0xFF474747)

    val OutlineGray = Color(0xFF9A958A)


    // Neutral - Black
    val Black = Color(0xFF000000)
    val Black50 = Color(0xFF2A2A28)
    val Black100 = Color(0xFF202020)


    //Cream
    val Cream = Color(0xFFF7F5F0)


    //Tier
    val TierGreen  = Color(0xFF6BA05C) // 새싹
    val TierAmber  = Color(0xFFCC8438) // 루키
    val TierBlue   = Color(0xFF5A8FBF) // 방랑자
    val TierPurple = Color(0xFF8A6FBE) // 지도제작자
    val TierGold   = Color(0xFFC29429) // 개척자
}