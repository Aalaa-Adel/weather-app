package com.example.breez.presentation.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.db.entity.AlertType
import com.example.breez.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigateToAddAlert: () -> Unit = {},
    onNavigateToEditAlert: (Long) -> Unit = {},
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateToAddAlert.collect {
            onNavigateToAddAlert()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToEditAlert.collect { alertId ->
            onNavigateToEditAlert(alertId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showToast.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    WeatherScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                BreezTopBar(
                    title = "Weather Alerts",
                    subtitle = "Manage your weather notifications"
                )

                when (val state = uiState) {
                    is AlertsUiState.Loading -> {
                        LoadingContent()
                    }

                    is AlertsUiState.Success -> {
                        PermissionWarnings(
                            hasNotificationPermission = state.hasNotificationPermission,
                            onCheckPermissions = {
                                viewModel.checkNotificationPermission()
                            }
                        )

                        if (state.alerts.isEmpty()) {
                            EmptyAlertsContent(
                                onAddAlert = { viewModel.onAddAlertClick() }
                            )
                        } else {
                            AlertsContent(
                                alerts = state.alerts,
                                onAlertClick = { alert -> viewModel.onEditAlertClick(alert.id) },
                                onToggleAlert = { alert -> viewModel.toggleAlertStatus(alert) },
                                onDeleteAlert = { alert -> viewModel.deleteAlert(alert) },
                                onTestAlert = { alert -> viewModel.testNotification(alert) }
                            )
                        }
                    }

                    is AlertsUiState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.refreshAlerts() }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .padding(bottom = 120.dp)
            ) {
                FAB(
                    onClick = { viewModel.onAddAlertClick() },
                    icon = Icons.Outlined.Add,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .padding(bottom = 120.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        CompactSnackbar(snackbarData = snackbarData)
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cornerRadius = 32.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Loading alerts...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun PermissionWarnings(
    hasNotificationPermission: Boolean,
    onCheckPermissions: () -> Unit
) {
    if (!hasNotificationPermission) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "• Notification permission is required to show weather alerts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Button(
                    onClick = onCheckPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check Permissions")
                }
            }
        }
    }
}

@Composable
private fun EmptyAlertsContent(
    onAddAlert: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center,
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "No alerts yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Create weather alerts to get notified about weather conditions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAddAlert,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Create alert",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsContent(
    alerts: List<AlertEntity>,
    onAlertClick: (AlertEntity) -> Unit,
    onToggleAlert: (AlertEntity) -> Unit,
    onDeleteAlert: (AlertEntity) -> Unit,
    onTestAlert: (AlertEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = alerts,
            key = { it.id }
        ) { alert ->
            SwipeToDeleteCard(
                onSwipeDelete = { onDeleteAlert(alert) },
                onIconDelete = { onDeleteAlert(alert) },
                onClick = { onAlertClick(alert) }
            ) {
                AlertCardContent(
                    alert = alert,
                    onToggleAlert = { onToggleAlert(alert) },
                    onTestAlert = { onTestAlert(alert) }
                )
            }
        }
    }
}

@Composable
private fun AlertCardContent(
    alert: AlertEntity,
    onToggleAlert: () -> Unit,
    onTestAlert: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (alert.alertType == AlertType.ALARM) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.20f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                            },
                            if (alert.alertType == AlertType.ALARM) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            }
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (alert.alertType == AlertType.ALARM) Icons.Outlined.Alarm else Icons.Outlined.Notifications,
                contentDescription = null,
                tint = if (alert.alertType == AlertType.ALARM) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val timeText = if (alert.isOneTime()) {
                "Once at ${dateFormat.format(alert.startTime)}"
            } else {
                "Daily ${timeFormat.format(alert.startTime)} - ${timeFormat.format(alert.endTime)}"
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.70f)
            )

            alert.cityName?.let { cityName ->
                Text(
                    text = if (alert.useCurrentLocation) "📍 Current location" else "📍 $cityName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!alert.isActive) {
                Text(
                    text = "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        IconButton(
            onClick = onTestAlert,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Outlined.PlayArrow,
                contentDescription = "Test notification",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Switch(
            checked = alert.isActive,
            onCheckedChange = { onToggleAlert() },
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 32.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun CompactSnackbar(snackbarData: SnackbarData) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = glassSurfaceColor(),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    glassBorderColor(),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}