package com.example.breez.presentation.alerts

import AlertCardContent
import EmptyAlertsContent
import ErrorContent
import LoadingContent
import PermissionWarnings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.R
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.presentation.components.*
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LocalContextGetResourceValueCall")
@Composable
fun AlertsScreen(
    onNavigateToAddAlert: () -> Unit = {},
    onNavigateToEditAlert: (Long) -> Unit = {},
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var recentlyDeleted by remember { mutableStateOf<AlertEntity?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                    title = stringResource(R.string.alerts_title),
                    subtitle = stringResource(R.string.alerts_subtitle)
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
                                onDeleteAlert = { alert ->
                                    recentlyDeleted = alert
                                    viewModel.deleteAlert(alert)

                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(
                                                R.string.alert_snackbar_removed,
                                                alert.cityName ?: alert.title
                                            ),
                                            actionLabel = context.getString(R.string.undo),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )

                                        if (result == SnackbarResult.ActionPerformed) {
                                            recentlyDeleted?.let { deletedAlert ->
                                                viewModel.addAlert(deletedAlert)
                                            }
                                        }

                                        recentlyDeleted = null
                                    }
                                },
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
                    .padding(end = 20.dp, bottom = 130.dp)
            ) {
                FAB(
                    onClick = { viewModel.onAddAlertClick() },
                    icon = Icons.Outlined.Add,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 190.dp)
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    onTestAlert = { onTestAlert(alert) },
                    onDeleteAlert = { onDeleteAlert(alert) }
                )
            }
        }
    }
}