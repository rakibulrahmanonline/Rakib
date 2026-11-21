package com.liquid.launcher.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Shared "water" state. Every glass surface reads this inside its draw phase,
 * so the animation updates every frame without triggering recomposition.
 */
class WaterState {
    var time by mutableFloatStateOf(0f)
    var rippleX by mutableFloatStateOf(0f)
    var rippleY by mutableFloatStateOf(0f)
    var rippleStart by mutableFloatStateOf(-100f)
    var screenW by mutableFloatStateOf(1080f)
    var screenH by mutableFloatStateOf(2400f)

    /** Drop a ripple into the water at a window position (px). */
    fun ripple(x: Float, y: Float) {
        rippleX = x
        rippleY = y
        rippleStart = time
    }

    /** Age of the current ripple in seconds, or -1 when no ripple is active. */
    fun rippleAge(): Float {
        val age = time - rippleStart
        return if (age in 0f..3f) age else -1f
    }
}

val LocalWater = staticCompositionLocalOf<WaterState> {
    error("WaterState not provided")
}

@Composable
fun rememberWaterState(): WaterState {
    val state = remember { WaterState() }
    LaunchedEffect(state) {
        val start = androidx.compose.runtime.withFrameNanos { it }
        while (true) {
            androidx.compose.runtime.withFrameNanos { now ->
                state.time = (now - start) / 1_000_000_000f
            }
        }
    }
    return state
}
