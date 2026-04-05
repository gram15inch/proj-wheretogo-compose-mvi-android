package com.dhkim139.admin.wheretogo.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 라이트
private val LightColorScheme = lightColorScheme(
    primary            = Blue40,
    onPrimary          = Grey99,
    primaryContainer   = Blue90,
    onPrimaryContainer = Blue10,

    error              = Red40,
    onError            = Grey99,
    errorContainer     = Red90,
    onErrorContainer   = Red10,

    background         = Grey99,
    onBackground       = Grey10,
    surface            = Grey99,
    onSurface          = Grey10,
    surfaceVariant     = BlueGrey90,
    onSurfaceVariant   = BlueGrey30,
    outline            = BlueGrey50,
    outlineVariant     = BlueGrey80,
)

// 다크
private val DarkColorScheme = darkColorScheme(
    primary            = Blue80,
    onPrimary          = Blue20,
    primaryContainer   = Blue10,
    onPrimaryContainer = Blue90,

    error              = Red80,
    onError            = Red10,
    errorContainer     = Red40,
    onErrorContainer   = Red90,

    background         = Grey10,
    onBackground       = Grey90,
    surface            = Grey10,
    onSurface          = Grey90,
    surfaceVariant     = BlueGrey30,
    onSurfaceVariant   = BlueGrey80,
    outline            = BlueGrey60,
    outlineVariant     = BlueGrey30,
)

@Composable
fun AdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {

    // 다크 테마 지원
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // 상태바 테마 맞춤
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AdminTypography,
        content     = content,
    )
}
