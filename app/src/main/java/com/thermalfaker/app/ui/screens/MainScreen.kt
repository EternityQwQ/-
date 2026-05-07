package com.thermalfaker.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalfaker.app.R
import com.thermalfaker.app.core.shizuku.ShizukuStatus
import com.thermalfaker.app.ui.theme.ThermalBlue
import com.thermalfaker.app.ui.theme.ThermalRed
import com.thermalfaker.app.ui.theme.ThermalYellow
import com.thermalfaker.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToInfo: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val currentTemp by viewModel.currentTemp.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.Dashboard, contentDescription = stringResource(R.string.hardware_dashboard))
                    }
                    IconButton(onClick = onNavigateToInfo) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.about))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shizuku Status Card
            ShizukuStatusCard(
                status = shizukuStatus,
                onRequestPermission = viewModel::requestShizukuPermission
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard Navigation Card
            DashboardNavigationCard(onNavigateToDashboard = onNavigateToDashboard)

            Spacer(modifier = Modifier.height(24.dp))

            // Current Temperature Display
            TemperatureDisplay(
                currentTemp = currentTemp,
                isSpoofingActive = settings.isBatterySpoofingActive,
                onRefresh = viewModel::refreshTemperature
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Target Temperature Input
            TargetTemperatureInput(
                targetTemp = settings.targetBatteryTemp,
                onUpdateTemp = viewModel::updateTargetTemp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            ActionButtons(
                isLoading = uiState.isLoading,
                isSpoofingActive = settings.isBatterySpoofingActive,
                onApply = viewModel::applySpoofing,
                onReset = viewModel::resetTemperature
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Snackbar for messages
            uiState.errorMessage?.let { errorMsg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMsg)
                    }
                }
                LaunchedEffect(errorMsg) {
                    delay(3000)
                    viewModel.clearMessages()
                }
            }

            uiState.successMessage?.let { successMsg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMsg)
                    }
                }
                LaunchedEffect(successMsg) {
                    delay(3000)
                    viewModel.clearMessages()
                }
            }
        }
    }
}

@Composable
fun DashboardNavigationCard(
    onNavigateToDashboard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        onClick = onNavigateToDashboard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.hardware_dashboard),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.monitor_and_spoof_all_hardware),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ShizukuStatusCard(
    status: ShizukuStatus,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                ShizukuStatus.PermissionGranted, ShizukuStatus.Connected ->
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = "Shizuku",
                    tint = when (status) {
                        ShizukuStatus.PermissionGranted, ShizukuStatus.Connected ->
                            MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
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
                            ShizukuStatus.Connected -> stringResource(R.string.connected)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (status == ShizukuStatus.PermissionDenied || status == ShizukuStatus.Unavailable) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.grant_permission))
                }
            }
        }
    }
}

@Composable
fun TemperatureDisplay(
    currentTemp: Int,
    isSpoofingActive: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                currentTemp > 40 -> ThermalRed.copy(alpha = 0.1f)
                currentTemp > 30 -> ThermalYellow.copy(alpha = 0.1f)
                else -> ThermalBlue.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSpoofingActive) stringResource(R.string.current_spoofed) else stringResource(R.string.current_temperature),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThermometerArc(temperature = currentTemp)
                Spacer(modifier = Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentTemp°",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            currentTemp > 40 -> ThermalRed
                            currentTemp > 30 -> ThermalYellow
                            else -> ThermalBlue
                        }
                    )
                    Text(
                        text = stringResource(R.string.celsius),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.refresh))
            }
        }
    }
}

@Composable
fun ThermometerArc(temperature: Int) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension / 2) - strokeWidth / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background arc
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Temperature gradient
            val progress = (temperature / 100f).coerceIn(0f, 1f)
            val sweepAngle = 260f * progress
            val gradient = Brush.sweepGradient(
                0.2f to ThermalBlue,
                0.5f to ThermalYellow,
                0.8f to ThermalRed,
                center = center
            )
            drawArc(
                brush = gradient,
                startAngle = 140f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun TargetTemperatureInput(
    targetTemp: Int,
    onUpdateTemp: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.target_temperature),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = targetTemp.toString(),
                onValueChange = { value ->
                    val temp = value.toIntOrNull() ?: 0
                    onUpdateTemp(temp.coerceIn(-20, 100))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.temperature_celsius)) },
                leadingIcon = { Icon(Icons.Default.Thermostat, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = targetTemp.toFloat(),
                onValueChange = { onUpdateTemp(it.toInt()) },
                valueRange = -20f..100f,
                steps = 119
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-20°C", style = MaterialTheme.typography.labelSmall)
                Text("100°C", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ActionButtons(
    isLoading: Boolean,
    isSpoofingActive: Boolean,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onApply,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.apply_spoof), style = MaterialTheme.typography.titleMedium)
            }
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            enabled = !isLoading && isSpoofingActive,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(stringResource(R.string.reset), style = MaterialTheme.typography.titleMedium)
        }
    }
}
