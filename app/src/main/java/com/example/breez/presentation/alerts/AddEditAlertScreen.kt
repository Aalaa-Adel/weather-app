package com.example.breez.presentation.alerts

import AlertCard
import AlertLabel
import AlertTypeChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.R
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.db.entity.AlertType
import com.example.breez.presentation.alerts.components.AlertDateTimePicker
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlertScreen(
    alertId: Long? = null,
    onBackClick: () -> Unit = {},
    onAlertSaved: () -> Unit = {},
    viewModel: AddEditAlertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showToast.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    WeatherScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is AddEditAlertUiState.Loading -> {
                    AlertLoadingScreen()
                }

                is AddEditAlertUiState.Form -> {
                    AlertFormScreen(
                        state = state,
                        onBackClick = onBackClick,
                        onAlertTypeChange = viewModel::updateAlertType,
                        onStartTimeChange = viewModel::updateStartTime,
                        onEndTimeChange = viewModel::updateEndTime,
                        onToggleLocationSource = viewModel::toggleLocationSource,
                        onSave = viewModel::saveAlert
                    )
                }

                is AddEditAlertUiState.Success -> {
                    LaunchedEffect(Unit) {
                        onAlertSaved()
                    }
                }

                is AddEditAlertUiState.Error -> {
                    AlertErrorScreen(
                        message = state.message,
                        onBackClick = onBackClick
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            ) {
                SnackbarHost(snackbarHostState)
            }
        }
    }
}

@Composable
private fun AlertLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()

            Text(
                text = stringResource(R.string.alerts_loading),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun AlertFormScreen(
    state: AddEditAlertUiState.Form,
    onBackClick: () -> Unit,
    onAlertTypeChange: (AlertType) -> Unit,
    onStartTimeChange: (Date) -> Unit,
    onEndTimeChange: (Date) -> Unit,
    onToggleLocationSource: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Column {
                Text(
                    text = if (state.isEditing) {
                        stringResource(R.string.alert_edit_title)
                    } else {
                        stringResource(R.string.alert_add_title)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = stringResource(R.string.alert_form_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

        AlertCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlertLabel(
                    text = stringResource(R.string.alert_type_label)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AlertTypeChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Notifications,
                        label = stringResource(R.string.alert_type_notification),
                        subLabel = stringResource(R.string.alert_type_notification_desc),
                        selected = state.alertType == AlertType.NOTIFICATION
                    ) {
                        onAlertTypeChange(AlertType.NOTIFICATION)
                    }

                    AlertTypeChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Alarm,
                        label = stringResource(R.string.alert_type_alarm),
                        subLabel = stringResource(R.string.alert_type_alarm_desc),
                        selected = state.alertType == AlertType.ALARM
                    ) {
                        onAlertTypeChange(AlertType.ALARM)
                    }
                }
            }
        }

        AlertCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AlertLabel(
                    text = if (state.isOneTimeAlert) {
                        stringResource(R.string.alert_time_label)
                    } else {
                        stringResource(R.string.alert_time_window_label)
                    }
                )

                AlertDateTimePicker(
                    label = if (state.isOneTimeAlert) {
                        stringResource(R.string.alert_datetime_label)
                    } else {
                        stringResource(R.string.alert_start_datetime_label)
                    },
                    date = state.startTime,
                    onChange = onStartTimeChange
                )

                if (!state.isOneTimeAlert) {
                    AlertDateTimePicker(
                        label = stringResource(R.string.alert_end_datetime_label),
                        date = state.endTime,
                        onChange = onEndTimeChange
                    )
                }

                state.timeError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        AlertCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlertLabel(
                    text = stringResource(R.string.alert_location_label)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(
                        checked = state.useCurrentLocation,
                        onCheckedChange = onToggleLocationSource
                    )

                    Text(
                        text = if (state.useCurrentLocation) {
                            stringResource(R.string.alert_use_current_location)
                        } else {
                            stringResource(R.string.alert_use_specific_location)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                state.cityName?.let { city ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.80f)
                        )
                    }
                }

                state.locationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Button(
            onClick = onSave,
            enabled = state.isValid() && !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = if (state.isEditing) {
                        stringResource(R.string.alert_update)
                    } else {
                        stringResource(R.string.alert_create)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(100.dp)
        )
    }
}

@Composable
private fun AlertErrorScreen(
    message: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = stringResource(R.string.alerts_error_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onBackClick
        ) {
            Text(
                text = stringResource(R.string.alert_go_back)
            )
        }
    }
}