package com.thermalfaker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Monospace
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermalfaker.app.core.util.LogEntry
import com.thermalfaker.app.core.util.LogLevel
import com.thermalfaker.app.ui.viewmodel.LogViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: LogViewModel = hiltViewModel()
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var exportMessage by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear logs")
                    }
                    IconButton(onClick = {
                        isExporting = true
                        coroutineScope.launch {
                            val result = viewModel.exportLogs()
                            result.onSuccess { path ->
                                exportMessage = "Log exported to: $path"
                            }.onFailure {
                                exportMessage = "Export failed: ${it.message}"
                            }
                            isExporting = false
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export logs")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isExporting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                exportMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = if (message.contains("failed", ignoreCase = true)) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (message.contains("failed", ignoreCase = true)) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    ) {
                        Text(message)
                    }
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(5000)
                        exportMessage = null
                    }
                }

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No logs yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            LogItem(log)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun LogItem(log: LogEntry) {
    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))

    Surface(
        shape = MaterialTheme.shapes.small,
        color = when (log.level) {
            LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            LogLevel.WARN -> MaterialTheme.colorScheme.warningContainer.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "[${log.level.name}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (log.level) {
                        LogLevel.ERROR -> MaterialTheme.colorScheme.error
                        LogLevel.WARN -> MaterialTheme.colorScheme.warning
                        LogLevel.INFO -> MaterialTheme.colorScheme.primary
                        LogLevel.DEBUG -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = Monospace,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
