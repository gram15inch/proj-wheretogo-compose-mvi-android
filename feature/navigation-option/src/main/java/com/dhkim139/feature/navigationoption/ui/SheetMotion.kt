package com.dhkim139.feature.navigationoption.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

internal object SheetMotion {

    const val DURATION_MS = 250

    const val EXIT_MS = 90

    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    const val PARTIAL_MS = 180

    private fun depth(mode: SheetContent): Int = when (mode) {
        is SheetContent.Overview -> 0
        is SheetContent.ParkingPicker -> 1
        is SheetContent.MapAppPicker -> 1
    }

    private fun isMapApps(mode: SheetContent): Boolean = mode is SheetContent.MapAppPicker

    fun transitionFor(from: SheetContent, to: SheetContent): ContentTransform = when {
        isMapApps(from) || isMapApps(to) -> fadeThrough()
        depth(to) > depth(from) -> sharedAxisZ(forward = true)
        else -> sharedAxisZ(forward = false)
    }

    private fun sharedAxisZ(forward: Boolean): ContentTransform {
        val enterScale = if (forward) 0.92f else 1.06f
        val exitScale = if (forward) 1.06f else 0.92f
        val spec = tween<Float>(DURATION_MS, easing = Emphasized)

        return ContentTransform(
            targetContentEnter = fadeIn(spec) +
                    scaleIn(initialScale = enterScale, animationSpec = spec),
            initialContentExit = fadeOut(tween(EXIT_MS, easing = LinearEasing)) +
                    scaleOut(targetScale = exitScale, animationSpec = spec),
            sizeTransform = sizeSpec(),
        )
    }

    private fun fadeThrough(): ContentTransform {
        val spec = tween<Float>(
            durationMillis = DURATION_MS - EXIT_MS,
            delayMillis = EXIT_MS,
            easing = Emphasized,
        )
        return ContentTransform(
            targetContentEnter = fadeIn(spec) +
                    scaleIn(initialScale = 0.94f, animationSpec = spec),
            initialContentExit = fadeOut(tween(EXIT_MS, easing = LinearEasing)),
            sizeTransform = sizeSpec(),
        )
    }

    private fun sizeSpec() = SizeTransform(clip = false) { _, _ ->
        tween(DURATION_MS, easing = Emphasized)
    }
}
