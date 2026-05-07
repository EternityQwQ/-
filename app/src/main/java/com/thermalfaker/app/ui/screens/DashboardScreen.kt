package com.thermalfaker.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalfaker.app.R
import com.thermalfaker.app.data.model.CpuCoreInfo
import com.thermalfaker.app.data.model.HardwareType
import com.thermalfaker.app.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar("Error: $message")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hardware_dashboard)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.SettingsBackupRestore,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshHardwareInfo() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LastUpdatedCard(
                    timestamp = uiState.lastRefreshTime,
                    onRefresh = { viewModel.refreshHardwareInfo() }
                )
            }

            item {
                GlobalTemperatureControl(
                    globalTemp = uiState.globalTemperature,
                    onSetGlobalTemp = { viewModel.setGlobalTemperature(it) },
                    onResetAll = { viewModel.resetAllTemperatures() }
                )
            }

            item {
                TemperatureCard(
                    title = stringResource(R.string.battery_temperature),
                    icon = Icons.Default.BatteryFull,
                    currentValue = uiState.hardwareInfo.batteryTemp,
                    isSupported = true,
                    isSpoofed = uiState.spoofStatus[HardwareType.BATTERY] ?: false,
                    spoofedValue = viewModel.getSpoofedTemperature(HardwareType.BATTERY),
                    onSetTemperature = { viewModel.setHardwareTemperature(HardwareType.BATTERY, it) },
                    onReset = { viewModel.resetHardwareTemperature(HardwareType.BATTERY) },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                TemperatureCard(
                    title = stringResource(R.string.cpu_temperature),
                    icon = Icons.Default.Memory,
                    currentValue = uiState.hardwareInfo.cpuTemp,
                    isSupported = uiState.hardwareInfo.isCpuSupported,
                    isSpoofed = uiState.spoofStatus[HardwareType.CPU] ?: false,
                    spoofedValue = viewModel.getSpoofedTemperature(HardwareType.CPU),
                    onSetTemperature = { viewModel.setHardwareTemperature(HardwareType.CPU, it) },
                    onReset = { viewModel.resetHardwareTemperature(HardwareType.CPU) },
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                TemperatureCard(
                    title = stringResource(R.string.gpu_temperature),
                    icon = Icons.Default.VideogameAsset,
                    currentValue = uiState.hardwareInfo.gpuTemp,
                    isSupported = uiState.hardwareInfo.isGpuSupported,
                    isSpoofed = uiState.spoofStatus[HardwareType.GPU] ?: false,
                    spoofedValue = viewModel.getSpoofedTemperature(HardwareType.GPU),
                    onSetTemperature = { viewModel.setHardwareTemperature(HardwareType.GPU, it) },
                    onReset = { viewModel.resetHardwareTemperature(HardwareType.GPU) },
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            item {
                TemperatureCard(
                    title = stringResource(R.string.ambient_temperature),
                    icon = Icons.Default.WbSunny,
                    currentValue = uiState.hardwareInfo.ambientTemp,
                    isSupported = uiState.hardwareInfo.isAmbientSupported,
                    isSpoofed = uiState.spoofStatus[HardwareType.AMBIENT] ?: false,
                    spoofedValue = viewModel.getSpoofedTemperature(HardwareType.AMBIENT),
                    onSetTemperature = { viewModel.setHardwareTemperature(HardwareType.AMBIENT, it) },
                    onReset = { viewModel.resetHardwareTemperature(HardwareType.AMBIENT) },
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.hardwareInfo.cpuFrequencies.isNotEmpty()) {
                item {
                    CpuFrequenciesCard(
                        frequencies = uiState.hardwareInfo.cpuFrequencies
                    )
                }
            }

            if (uiState.hardwareInfo.gpuFrequency > 0) {
                item {
                    GpuFrequencyCard(
                        frequency = uiState.hardwareInfo.gpuFrequency
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LastUpdatedCard(
    timestamp: Long,
    onRefresh: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = if (timestamp > 0) {
        dateFormat.format(Date(timestamp))
    } else {
        "--:--:--"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.last_updated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            FilledTonalButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.refresh))
            }
        }
    }
}

@Composable
private fun GlobalTemperatureControl(
    globalTemp: Int?,
    onSetGlobalTemp: (Int) -> Unit,
    onResetAll: () -> Unit
) {
    var sliderValue by remember { mutableStateOf(35f) }
    var inputValue by remember { mutableStateOf("35") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.global_temperature_control),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.set_all_temperatures_at_once),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sliderValue.toInt()}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        inputValue = it.toInt().toString()
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        inputValue = it
                        it.toIntOrNull()?.let { value ->
                            if (value in 0..100) {
                                sliderValue = value.toFloat()
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.temperature_celsius)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = { onSetGlobalTemp(sliderValue.toInt()) },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onResetAll,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsBackupRestore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.reset_all))
            }
        }
    }
}

@Composable
private fun TemperatureCard(
    title: String,
    icon: ImageVector,
    currentValue: Int,
    isSupported: Boolean,
    isSpoofed: Boolean,
    spoofedValue: Int?,
    onSetTemperature: (Int) -> Unit,
    onReset: () -> Unit,
    color: Color
) {
    var sliderValue by remember { mutableStateOf(currentValue.coerceIn(0, 100).toFloat()) }
    var inputValue by remember { mutableStateOf(currentValue.toString()) }

    LaunchedEffect(currentValue) {
        if (!isSpoofed) {
            sliderValue = currentValue.coerceIn(0, 100).toFloat()
            inputValue = currentValue.toString()
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isSpoofed && spoofedValue != null) {
                            Text(
                                text = stringResource(R.string.spoofed_to, spoofedValue),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (isSpoofed) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = stringResource(R.string.spoofed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.real),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isSupported) {
                Text(
                    text = stringResource(R.string.not_supported_on_this_device),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.current),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currentValue°C",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSpoofed) MaterialTheme.colorScheme.error else color
                        )
                    }

                    Column(modifier = Modifier.weight(2f)) {
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                inputValue = it.toInt().toString()
                            },
                            valueRange = 0f..100f,
                            enabled = isSupported
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = {
                            inputValue = it
                            it.toIntOrNull()?.let { value ->
                                if (value in 0..100) {
                                    sliderValue = value.toFloat()
                                }
                            }
                        },
                        label = { Text("°C") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = isSupported
                    )

                    Button(
                        onClick = { onSetTemperature(sliderValue.toInt()) },
                        enabled = isSupported,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(stringResource(R.string.set))
                    }

                    OutlinedButton(
                        onClick = onReset,
                        enabled = isSpoofed,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CpuFrequenciesCard(
    frequencies: List<CpuCoreInfo>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.cpu_frequencies),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                frequencies.forEach { core ->
                    CpuCoreChip(core = core)
                }
            }
        }
    }
}

@Composable
private fun CpuCoreChip(core: CpuCoreInfo) {
    val progress = if (core.maxFrequency > 0) {
        core.currentFrequency.toFloat() / core.maxFrequency.toFloat()
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CPU ${core.coreId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${core.currentFrequency} MHz",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (core.maxFrequency > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = when {
                        progress > 0.8f -> MaterialTheme.colorScheme.error
                        progress > 0.5f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@Composable
private fun GpuFrequencyCard(
    frequency: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VideogameAsset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.gpu_frequency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$frequency MHz",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
