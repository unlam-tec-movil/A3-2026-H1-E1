package ar.edu.unlam.mobile.scaffolding.ui.screens.progress

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.mobile.scaffolding.ui.theme.CyanWave
import ar.edu.unlam.mobile.scaffolding.ui.theme.ElectricIndigo
import ar.edu.unlam.mobile.scaffolding.ui.theme.EmeraldIdeal
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted = permissions.filterValues { it }.keys
            viewModel.onHealthConnectPermissionsResult(granted)
        }

    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            animateIn = true
        }
    }

    val entranceProgress by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "ProgressScreenEntrance",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Progreso",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ElectricIndigo,
                )
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "Error al cargar los datos",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Summary cards row
                    StatsSummaryRow(
                        sessions = uiState.sessionsData,
                        entranceProgress = entranceProgress,
                    )

                    // ROM Evolution Line Chart
                    RomEvolutionCard(
                        sessions = uiState.sessionsData,
                        entranceProgress = entranceProgress,
                    )

                    // Heart Rate Evolution Bar Chart
                    HeartRateEvolutionCard(
                        sessions = uiState.sessionsData,
                        entranceProgress = entranceProgress,
                    )

                    // Health Connect Banner
                    HealthConnectSyncCard(
                        isLinked = uiState.isHealthConnectLinked,
                        onLinkClick = {
                            launcher.launch(
                                arrayOf(
                                    "androidx.health.permission.HeartRate.read",
                                    "androidx.health.permission.ActiveCaloriesBurned.read",
                                    "androidx.health.permission.OxygenSaturation.read",
                                ),
                            )
                        },
                        entranceProgress = entranceProgress,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatsSummaryRow(
    sessions: List<SessionProgressItem>,
    entranceProgress: Float,
) {
    val maxRom = sessions.maxOfOrNull { it.averageRom } ?: 0f
    val avgHr = if (sessions.isNotEmpty()) sessions.map { it.averageHeartRate }.average().toFloat() else 0f
    val completedCount = sessions.filter { it.id >= 0 }.size // Real sessions logged in DB are id >= 0

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = ((1f - entranceProgress) * 20).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatBox(
            title = "ROM Máximo",
            value = "${maxRom.roundToInt()}°",
            subtitle = "Meta: 120°",
            icon = Icons.Default.TrendingUp,
            iconBg = ElectricIndigo.copy(alpha = 0.1f),
            iconTint = ElectricIndigo,
            modifier = Modifier.weight(1f),
        )
        StatBox(
            title = "FC Promedio",
            value = "${avgHr.roundToInt()} lpm",
            subtitle = "En ejercicio",
            icon = Icons.Default.Favorite,
            iconBg = Color.Red.copy(alpha = 0.1f),
            iconTint = Color.Red,
            modifier = Modifier.weight(1f),
        )
        StatBox(
            title = "Completados",
            value = "$completedCount / 7",
            subtitle = "Sesiones reales",
            icon = Icons.Default.Timeline,
            iconBg = EmeraldIdeal.copy(alpha = 0.1f),
            iconTint = EmeraldIdeal,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = iconTint,
                )
            }
        }
    }
}

@Composable
fun RomEvolutionCard(
    sessions: List<SessionProgressItem>,
    entranceProgress: Float,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = ((1f - entranceProgress) * 30).dp),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Rango de Movimiento (ROM)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "Gráfico interactivo de evolución",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InteractiveLineChart(
                sessions = sessions,
                entranceProgress = entranceProgress,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
            )
        }
    }
}

@Composable
fun InteractiveLineChart(
    sessions: List<SessionProgressItem>,
    entranceProgress: Float,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    var touchX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textPaintColor =
        MaterialTheme.colorScheme.onSurface
            .copy(alpha = 0.4f)
            .toArgb()

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val paddingLeftPx = with(density) { 40.dp.toPx() }
        val paddingRightPx = with(density) { 16.dp.toPx() }
        val chartWidthPx = widthPx - paddingLeftPx - paddingRightPx
        val intervalPx = chartWidthPx / (sessions.size - 1)

        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(sessions) {
                        detectTapGestures(
                            onPress = {
                                isDragging = true
                                tryAwaitRelease()
                                isDragging = false
                                selectedIndex = -1
                            },
                            onTap = { offset ->
                                val width = size.width
                                val interval = intervalPx
                                val x = offset.x - paddingLeftPx
                                val rawIndex = (x / interval).roundToInt()
                                selectedIndex = rawIndex.coerceIn(0, sessions.size - 1)
                            },
                        )
                    }.pointerInput(sessions) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                touchX = offset.x
                            },
                            onDragEnd = {
                                isDragging = false
                                selectedIndex = -1
                            },
                            onDragCancel = {
                                isDragging = false
                                selectedIndex = -1
                            },
                            onDrag = { change, _ ->
                                touchX = change.position.x
                                val width = size.width
                                val interval = intervalPx
                                val x = touchX - paddingLeftPx
                                val rawIndex = (x / interval).roundToInt()
                                selectedIndex = rawIndex.coerceIn(0, sessions.size - 1)
                            },
                        )
                    },
        ) {
            val width = size.width
            val height = size.height

            val paddingLeft = 40.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 24.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 180f
            val minVal = 0f
            val range = maxVal - minVal

            // Draw Y-axis gridlines and labels
            val yLinesCount = 4
            val paint =
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.RIGHT
                }

            for (i in 0..yLinesCount) {
                val value = minVal + (range / yLinesCount) * i
                val y = height - paddingBottom - (value / maxVal) * chartHeight

                // Grid line
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                )

                // Label
                drawContext.canvas.nativeCanvas.drawText(
                    "${value.toInt()}°",
                    paddingLeft - 10f,
                    y + 4.dp.toPx(),
                    paint,
                )
            }

            // Calculate x and y coordinates for each point
            val interval = chartWidth / (sessions.size - 1)
            val points =
                sessions.mapIndexed { i, session ->
                    val x = paddingLeft + i * interval
                    // Animate Y values based on entranceProgress
                    val animatedRom = session.averageRom * entranceProgress
                    val y = height - paddingBottom - (animatedRom / maxVal) * chartHeight
                    Offset(x, y)
                }

            // Draw area gradient under the curve
            if (points.isNotEmpty()) {
                val areaPath =
                    Path().apply {
                        moveTo(points.first().x, height - paddingBottom)
                        points.forEach { point ->
                            lineTo(point.x, point.y)
                        }
                        lineTo(points.last().x, height - paddingBottom)
                        close()
                    }

                drawPath(
                    path = areaPath,
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(ElectricIndigo.copy(alpha = 0.25f), Color.Transparent),
                            startY = paddingTop,
                            endY = height - paddingBottom,
                        ),
                )

                // Draw line path
                val linePath =
                    Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                drawPath(
                    path = linePath,
                    brush =
                        Brush.horizontalGradient(
                            colors = listOf(ElectricIndigo, CyanWave),
                        ),
                    style =
                        Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                )

                // Draw points and X-axis labels
                val xPaint =
                    android.graphics.Paint().apply {
                        color = textPaintColor
                        textSize = with(density) { 10.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                sessions.forEachIndexed { i, session ->
                    val point = points[i]

                    // Draw outer dot
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = point,
                    )

                    drawCircle(
                        color = ElectricIndigo,
                        radius = 2.5.dp.toPx(),
                        center = point,
                    )

                    // Draw label
                    drawContext.canvas.nativeCanvas.drawText(
                        session.dateLabel,
                        point.x,
                        height - 6.dp.toPx(),
                        xPaint,
                    )
                }

                // Draw interaction indicators if selected
                if (selectedIndex in sessions.indices) {
                    val selectedPoint = points[selectedIndex]

                    // Draw vertical dotted line
                    drawLine(
                        color = ElectricIndigo,
                        start = Offset(selectedPoint.x, paddingTop),
                        end = Offset(selectedPoint.x, height - paddingBottom),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
                    )

                    // Draw highlighted point
                    drawCircle(
                        color = CyanWave,
                        radius = 7.dp.toPx(),
                        center = selectedPoint,
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = selectedPoint,
                    )
                }
            }
        }

        // Display overlay tooltip box
        if (selectedIndex in sessions.indices) {
            val session = sessions[selectedIndex]
            val xOffset = 40.dp + with(density) { (selectedIndex * intervalPx).toDp() }

            Box(
                modifier =
                    Modifier
                        .offset {
                            IntOffset(
                                x = with(density) { (xOffset - 75.dp).toPx().toInt() },
                                y = with(density) { 10.dp.toPx().toInt() },
                            )
                        }.wrapContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .border(
                            width = 1.dp,
                            color = ElectricIndigo.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                        ).padding(8.dp),
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = session.fullDateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = session.exerciseName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "ROM: ${session.averageRom.roundToInt()}°",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ElectricIndigo,
                        )
                        Text(
                            text = "${session.successfulReps} reps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeartRateEvolutionCard(
    sessions: List<SessionProgressItem>,
    entranceProgress: Float,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = ((1f - entranceProgress) * 40).dp),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Frecuencia Cardíaca Media",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "Frecuencia media por sesión",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InteractiveBarChart(
                sessions = sessions,
                entranceProgress = entranceProgress,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
            )
        }
    }
}

@Composable
fun InteractiveBarChart(
    sessions: List<SessionProgressItem>,
    entranceProgress: Float,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    var touchX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textPaintColor =
        MaterialTheme.colorScheme.onSurface
            .copy(alpha = 0.4f)
            .toArgb()

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val paddingLeftPx = with(density) { 40.dp.toPx() }
        val paddingRightPx = with(density) { 16.dp.toPx() }
        val chartWidthPx = widthPx - paddingLeftPx - paddingRightPx
        val barWidthTotalPx = chartWidthPx / sessions.size

        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(sessions) {
                        detectTapGestures(
                            onPress = {
                                isDragging = true
                                tryAwaitRelease()
                                isDragging = false
                                selectedIndex = -1
                            },
                            onTap = { offset ->
                                val barWidthTotal = barWidthTotalPx
                                val x = offset.x - paddingLeftPx
                                val rawIndex = (x / barWidthTotal).toInt()
                                selectedIndex = rawIndex.coerceIn(0, sessions.size - 1)
                            },
                        )
                    }.pointerInput(sessions) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                touchX = offset.x
                            },
                            onDragEnd = {
                                isDragging = false
                                selectedIndex = -1
                            },
                            onDragCancel = {
                                isDragging = false
                                selectedIndex = -1
                            },
                            onDrag = { change, _ ->
                                touchX = change.position.x
                                val barWidthTotal = barWidthTotalPx
                                val x = touchX - paddingLeftPx
                                val rawIndex = (x / barWidthTotal).toInt()
                                selectedIndex = rawIndex.coerceIn(0, sessions.size - 1)
                            },
                        )
                    },
        ) {
            val width = size.width
            val height = size.height

            val paddingLeft = 40.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 24.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 150f
            val minVal = 0f
            val range = maxVal - minVal

            // Draw Y-axis reference gridlines
            val yLinesCount = 3
            val paint =
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.RIGHT
                }

            val references = listOf(60f, 100f, 140f)
            references.forEach { value ->
                val y = height - paddingBottom - (value / maxVal) * chartHeight

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${value.toInt()} bpm",
                    paddingLeft - 10f,
                    y + 4.dp.toPx(),
                    paint,
                )
            }

            // Draw Bars
            val barWidthTotal = chartWidth / sessions.size
            val barSpacing = 12.dp.toPx()
            val barWidth = barWidthTotal - barSpacing

            val xPaint =
                android.graphics.Paint().apply {
                    color = textPaintColor
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                }

            sessions.forEachIndexed { i, session ->
                // Apply a staggered entry animation for each bar
                val delayFactor = i * 0.08f
                val scaleProgress = (entranceProgress * 1.5f - delayFactor).coerceIn(0f, 1f)
                val animatedVal = session.averageHeartRate * scaleProgress

                val x = paddingLeft + (i * barWidthTotal) + (barSpacing / 2)
                val barHeight = (animatedVal / maxVal) * chartHeight
                val y = height - paddingBottom - barHeight

                val isSelected = selectedIndex == i
                val brush =
                    if (isSelected) {
                        Brush.verticalGradient(
                            colors = listOf(Color.Red, Color.Red.copy(alpha = 0.6f)),
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(CyanWave, CyanWave.copy(alpha = 0.5f)),
                        )
                    }

                // Draw rounded bar
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )

                // Draw X label
                drawContext.canvas.nativeCanvas.drawText(
                    session.dateLabel,
                    x + (barWidth / 2),
                    height - 6.dp.toPx(),
                    xPaint,
                )
            }
        }

        // Interactive Tooltip Box
        if (selectedIndex in sessions.indices) {
            val session = sessions[selectedIndex]
            val xOffset = 40.dp + with(density) { ((selectedIndex * barWidthTotalPx) + (barWidthTotalPx / 2)).toDp() }

            Box(
                modifier =
                    Modifier
                        .offset {
                            IntOffset(
                                x = with(density) { (xOffset - 75.dp).toPx().toInt() },
                                y = with(density) { 10.dp.toPx().toInt() },
                            )
                        }.wrapContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .border(
                            width = 1.dp,
                            color = Color.Red.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                        ).padding(8.dp),
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = session.fullDateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = "Ritmo Cardíaco Medio",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${session.averageHeartRate.roundToInt()} bpm",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Red,
                        )
                        Text(
                            text = "Intensidad: ${(session.averageHeartRate / 1.5).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthConnectSyncCard(
    isLinked: Boolean,
    onLinkClick: () -> Unit,
    entranceProgress: Float,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .offset(y = ((1f - entranceProgress) * 50).dp),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isLinked) EmeraldIdeal.copy(alpha = 0.08f) else ElectricIndigo.copy(alpha = 0.08f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLinked) EmeraldIdeal.copy(alpha = 0.2f) else ElectricIndigo.copy(alpha = 0.2f),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isLinked) Icons.Default.Link else Icons.Default.Timeline,
                        contentDescription = null,
                        tint = if (isLinked) EmeraldIdeal else ElectricIndigo,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text =
                            if (isLinked) {
                                "Sincronizado con Health Connect"
                            } else {
                                "Sincronizar frecuencia cardíaca"
                            },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isLinked) EmeraldIdeal else ElectricIndigo,
                    )
                    Text(
                        text =
                            if (isLinked) {
                                "Tus datos reales de ritmo cardíaco están vinculados."
                            } else {
                                "Importa datos de pulseras inteligentes y Google Fit."
                            },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            if (!isLinked) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ElectricIndigo)
                            .clickable { onLinkClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Vincular",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}
