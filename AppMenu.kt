package com.liquid.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liquid.launcher.AppInfo
import com.liquid.launcher.R
import com.liquid.launcher.glass.liquidGlass

/** Long-press menu: a floating liquid-glass card with a few actions. */
@Composable
fun AppMenu(
    app: AppInfo,
    inDock: Boolean,
    onToggleDock: () -> Unit,
    onInfo: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(272.dp)
                // Swallow taps inside the card so they don't dismiss the menu.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .liquidGlass(32.dp)
                .padding(vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
                BasicText(
                    text = app.label,
                    style = glassText(17f, FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 14.dp),
                    maxLines = 1,
                )
            }
            MenuRow(
                text = stringResource(if (inDock) R.string.dock_remove else R.string.dock_add),
                onClick = onToggleDock,
            )
            MenuRow(text = stringResource(R.string.app_info), onClick = onInfo)
            MenuRow(text = stringResource(R.string.uninstall), onClick = onUninstall)
        }
    }
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
    ) {
        BasicText(text = text, style = glassText(16f, FontWeight.Medium, 0.95f))
    }
}
