package com.liquid.launcher.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.liquid.launcher.AppInfo
import com.liquid.launcher.AppRepository
import com.liquid.launcher.DockStore
import com.liquid.launcher.R
import com.liquid.launcher.glass.LocalWater
import com.liquid.launcher.glass.WaterState
import com.liquid.launcher.glass.liquidGlass
import com.liquid.launcher.glass.rememberWaterState
import com.liquid.launcher.glass.waterWallpaper
import com.liquid.launcher.key
import com.liquid.launcher.rememberApps
import kotlin.math.ceil
import kotlin.math.max

private const val COLUMNS = 4
private val ROW_HEIGHT = 100.dp

/** Drops a ripple into the water wherever the screen is touched (without consuming the touch). */
private fun Modifier.dropRipples(water: WaterState): Modifier = pointerInput(water) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            event.changes
                .firstOrNull { it.pressed && !it.previousPressed }
                ?.let { water.ripple(it.position.x, it.position.y) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherRoot(homeSignal: Int) {
    val context = LocalContext.current
    val water = rememberWaterState()
    val apps = rememberApps()

    var dockKeys by remember { mutableStateOf(DockStore.loadKeys(context)) }
    val dock = remember(apps, dockKeys) { DockStore.resolve(context, apps, dockKeys) }
    val gridApps = remember(apps, dock) {
        val inDock = dock.map { it.key }.toSet()
        apps.filter { it.key !in inDock }
    }

    var drawerOpen by remember { mutableStateOf(false) }
    var menuApp by remember { mutableStateOf<AppInfo?>(null) }

    LaunchedEffect(homeSignal) {
        drawerOpen = false
        menuApp = null
    }
    BackHandler(enabled = drawerOpen || menuApp != null) {
        if (menuApp != null) menuApp = null else drawerOpen = false
    }

    val launch: (AppInfo) -> Unit = { app ->
        AppRepository.launch(context, app)
        drawerOpen = false
    }
    val showMenu: (AppInfo) -> Unit = { menuApp = it }

    fun toggleDock(app: AppInfo) {
        val current = dock.map { it.key }
        val next = when {
            app.key in current -> current - app.key
            current.size >= DockStore.MAX -> {
                Toast.makeText(context, context.getString(R.string.dock_full), Toast.LENGTH_SHORT).show()
                return
            }
            else -> current + app.key
        }
        dockKeys = next
        DockStore.saveKeys(context, next)
    }

    val homeAlpha by animateFloatAsState(if (drawerOpen) 0f else 1f, label = "homeAlpha")

    CompositionLocalProvider(LocalWater provides water) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .waterWallpaper()
                .dropRipples(water),
        ) {
            // ---------- Home ----------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .graphicsLayer { alpha = homeAlpha },
            ) {
                ClockWidget(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp))

                var pageIndex by remember { mutableStateOf(0) }
                var pageCount by remember { mutableStateOf(1) }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .pointerInput(Unit) {
                            var total = 0f
                            detectVerticalDragGestures(
                                onDragStart = { total = 0f },
                                onVerticalDrag = { _, dy -> total += dy },
                                onDragEnd = { if (total < -90f) drawerOpen = true },
                            )
                        },
                ) {
                    val rows = max(1, (maxHeight / ROW_HEIGHT).toInt())
                    val perPage = rows * COLUMNS
                    val pages = remember(gridApps, perPage) { gridApps.chunked(perPage).ifEmpty { listOf(emptyList()) } }
                    val pagerState = rememberPagerState(pageCount = { pages.size })

                    LaunchedEffect(homeSignal) { pagerState.animateScrollToPage(0) }
                    LaunchedEffect(pagerState.currentPage, pages.size) {
                        pageIndex = pagerState.currentPage
                        pageCount = pages.size
                    }

                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AppGridPage(
                            apps = pages[page],
                            onClick = launch,
                            onLongClick = showMenu,
                        )
                    }
                }

                PageDots(
                    count = pageCount,
                    current = pageIndex,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                GlassPill(
                    label = stringResource(R.string.search),
                    onClick = { drawerOpen = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp, bottom = 12.dp),
                )
                Dock(apps = dock, onClick = launch, onLongClick = showMenu)
                Spacer(Modifier.height(10.dp))
            }

            // ---------- App drawer ----------
            AnimatedVisibility(
                visible = drawerOpen,
                enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut(),
            ) {
                AppDrawer(
                    apps = apps,
                    onLaunch = launch,
                    onLongPress = showMenu,
                    onClose = { drawerOpen = false },
                )
            }

            // ---------- Long-press menu ----------
            menuApp?.let { app ->
                AppMenu(
                    app = app,
                    inDock = dock.any { it.key == app.key },
                    onToggleDock = { toggleDock(app); menuApp = null },
                    onInfo = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        menuApp = null
                    },
                    onUninstall = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        menuApp = null
                    },
                    onDismiss = { menuApp = null },
                )
            }
        }
    }
}

@Composable
private fun AppGridPage(
    apps: List<AppInfo>,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        apps.chunked(COLUMNS).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ROW_HEIGHT),
            ) {
                row.forEach { app ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        GlassAppIcon(
                            app = app,
                            onClick = { onClick(app) },
                            onLongClick = { onLongClick(app) },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Dock(
    apps: List<AppInfo>,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .heightIn(min = 92.dp)
            .liquidGlass(38.dp)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        apps.forEach { app ->
            GlassAppIcon(
                app = app,
                showLabel = false,
                tileSize = 60.dp,
                onClick = { onClick(app) },
                onLongClick = { onLongClick(app) },
            )
        }
    }
}
