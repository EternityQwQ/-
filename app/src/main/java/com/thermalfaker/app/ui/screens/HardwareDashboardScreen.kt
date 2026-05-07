package com.thermalfaker.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalfaker.app.R
import com.thermalfaker.app.core.shizuku.ShizukuStatus
import com.thermalfaker.app.ui.theme.ThermalBlue
import com.thermalfaker.app.ui.theme.ThermalRed
import com.thermalfaker.app.ui.theme.ThermalYellow
import com.thermalfaker.app.ui.viewmodel.HardwareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareDashboardScreen(
    onNavigateToControl: () -> Unit,
    onNavigateToInfo: () -> Unit,
    viewModel: HardwareViewModel = hiltViewModel()
) {
    val hardwareInfo = viewModel.hardwareInfo.collectAsState().value
    val shizukuStatus = viewModel.shizukuStatus.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hardware_dashboard)) },
                actions = {
                    IconButton(onClick = { viewModel.refreshHardwareInfo() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = onNavigateToInfo) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.info))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToControl,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.temperature_control))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShizukuStatusIndicator(status = shizukuStatus)

            TemperatureCards(
                cpuTemp = hardwareInfo.cpuTemp,
                gpuTemp = hardwareInfo.gpuTemp,
                batteryTemp = hardwareInfo.batteryTemp,
                batteryLevel = hardwareInfo.batteryLevel
            )

            CpuInfoCard(
                cpuFreq = hardwareInfo.cpuFreq,
                cpuUsage = hardwareInfo.cpuUsage
            )

            GpuInfoCard(gpuFreq = hardwareInfo.gpuFreq)
        }
    }
}

@Composable
fun ShizukuStatusIndicator(status: ShizukuStatus) {
    val isGranted = status == ShizukuStatus.PermissionGranted

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = "Shizuku",
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.shizuku_status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (status) {
                        ShizukuStatus.Unavailable -> stringResource(R.string.shizuku_unavailable)
                        ShizukuStatus.NotInstalled -> stringResource(R.string.shizuku_not_installed)
                        ShizukuStatus.PermissionDenied -> stringResource(R.string.permission_denied)
                        ShizukuStatus.PermissionGranted -> stringResource(R.string.permission_granted)
                        ShizukuStatus.BinderReceived -> stringResource(R.string.binder_received)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun TemperatureCards(
    cpuTemp: com.thermalfaker.app.data.model.TempInfo,
    gpuTemp: com.thermalfaker.app.data.model.TempInfo,
    batteryTemp: com.thermalfaker.app.data.model.TempInfo,
    batteryLevel: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TemperatureCard(
            title = stringResource(R.string.cpu),
            temp = cpuTemp,
            icon = Icons.Default.Cpu,
            modifier = Modifier.weight(1f)
        )
        TemperatureCard(
            title = stringResource(R.string.gpu),
            temp = gpuTemp,
            icon = Icons.Default.Gpu,
            modifier = Modifier.weight(1f)
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.battery),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$batteryLevel%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                BatteryThermometer(temp = batteryTemp.realTemp)
            }
        }
    }
}

@Composable
fun TemperatureCard(
    title: String,
    temp: com.thermalfaker.app.data.model.TempInfo,
    icon: androidx.compose.material.icons.IconVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (temp.supported) {
                Text(
                    text = "${temp.realTemp}°C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        temp.realTemp > 50 -> ThermalRed
                        temp.realTemp > 40 -> ThermalYellow
                        else -> ThermalBlue
                    }
                )
                if (temp.isSpoofed) {
                    Text(
                        text = "${stringResource(R.string.spoofed)}: ${temp.spoofedTemp}°C",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Text(
                    text = temp.error ?: stringResource(R.string.not_supported),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BatteryThermometer(temp: Int) {
    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val strokeWidth = 6.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width - strokeWidth) / 2

            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val progress = (temp / 100f).coerceIn(0f, 1f)
            val sweepAngle = 260f * progress
            val color = when {
                temp > 50 -> ThermalRed
                temp > 40 -> ThermalYellow
                else -> ThermalBlue
            }

            drawArc(
                color = color,
                startAngle = 140f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$temp°",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CpuInfoCard(cpuFreq: List<com.thermalfaker.app.data.model.FreqInfo>, cpuUsage: Float) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Microchip,
                    contentDescription = "CPU",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.cpu_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cpuFreq.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cpuFreq.take(4).forEachIndexed { index, freq ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CPU$index",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${freq.currentFreq} MHz",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Activity,
                    contentDescription = "CPU Usage",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.cpu_usage),
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = cpuUsage / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            cpuUsage > 80 -> ThermalRed
                            cpuUsage > 50 -> ThermalYellow
                            else -> ThermalBlue
                        }
                    )
                }
                Text(
                    text = "${cpuUsage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GpuInfoCard(gpuFreq: com.thermalfaker.app.data.model.FreqInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gpu,
                    contentDescription = "GPU",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.gpu_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (gpuFreq.supported) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.current_frequency),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${gpuFreq.currentFreq} MHz",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = gpuFreq.error ?: stringResource(R.string.not_supported),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
