package com.liquid.launcher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liquid.launcher.glass.liquidGlass
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Large liquid-glass clock + date card. Follows the device language (Bengali digits included). */
@Composable
fun ClockWidget(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
        }
    }

    val locale = Locale.getDefault()
    val time = remember(now, locale) { now.format(DateTimeFormatter.ofPattern("hh:mm", locale)) }
    val period = remember(now, locale) { now.format(DateTimeFormatter.ofPattern("a", locale)) }
    val date = remember(now, locale) { now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(36.dp)
            .padding(horizontal = 26.dp, vertical = 18.dp),
    ) {
        BasicText(text = date, style = glassText(15f, FontWeight.Medium, 0.92f))
        Row(verticalAlignment = Alignment.Bottom) {
            BasicText(text = time, style = glassText(68f, FontWeight.Light))
            BasicText(
                text = period,
                style = glassText(18f, FontWeight.Medium, 0.9f),
                modifier = Modifier.padding(start = 8.dp, bottom = 14.dp),
            )
        }
    }
}
