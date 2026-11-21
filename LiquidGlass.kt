package com.liquid.launcher.glass

import android.graphics.RuntimeShader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/** Full-screen animated water wallpaper. Put this at the bottom of the screen. */
@Composable
fun Modifier.waterWallpaper(): Modifier {
    val water = LocalWater.current
    val shader = remember { RuntimeShader(Shaders.WALLPAPER) }
    val brush = remember(shader) { ShaderBrush(shader) }
    return this.onSizeChanged {
        water.screenW = it.width.toFloat()
        water.screenH = it.height.toFloat()
    }.drawBehind {
        shader.setFloatUniform("uRes", size.width, size.height)
        shader.setFloatUniform("uTime", water.time)
        shader.setFloatUniform("uRip", water.rippleX, water.rippleY, water.rippleAge())
        drawRect(brush)
    }
}

/**
 * Turns any composable's background into a liquid-glass surface that refracts
 * the water wallpaper behind it.
 *
 * @param glass lens strength, 1f = normal. Animate it (e.g. 1.4f while pressed).
 */
@Composable
fun Modifier.liquidGlass(cornerRadius: Dp, glass: Float = 1f): Modifier {
    val water = LocalWater.current
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    val shader = remember { RuntimeShader(Shaders.GLASS) }
    val brush = remember(shader) { ShaderBrush(shader) }
    var origin by remember { mutableStateOf(Offset.Zero) }

    return this
        .onGloballyPositioned { origin = it.positionInWindow() }
        .drawBehind {
            shader.setFloatUniform("uRes", water.screenW, water.screenH)
            shader.setFloatUniform("uTime", water.time)
            shader.setFloatUniform("uRip", water.rippleX, water.rippleY, water.rippleAge())
            shader.setFloatUniform("uSize", size.width, size.height)
            shader.setFloatUniform("uOrigin", origin.x, origin.y)
            shader.setFloatUniform("uRadius", radiusPx)
            shader.setFloatUniform("uGlass", glass)
            drawRect(brush)
        }
}
