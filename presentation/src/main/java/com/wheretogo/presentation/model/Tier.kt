package com.wheretogo.presentation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.wheretogo.presentation.R
import com.wheretogo.presentation.theme.Palette

data class Tier(
    @StringRes val nameRes: Int,
    val requiredCount: Int,
    val icon: ImageVector,
    val color: Color
){
    companion object{
        val default = listOf(
            Tier(R.string.tier_sprout, 0, Icons.Outlined.Spa, Palette.TierGreen),
            Tier(R.string.tier_rookie, 1, Icons.Outlined.EmojiEvents, Palette.TierAmber),
            Tier(R.string.tier_wanderer, 3, Icons.Outlined.Hiking, Palette.TierBlue),
            Tier(R.string.tier_cartographer, 7, Icons.Outlined.Map, Palette.TierPurple),
            Tier(R.string.tier_pioneer, 15, Icons.Outlined.Explore, Palette.TierGold),
        )
    }
}