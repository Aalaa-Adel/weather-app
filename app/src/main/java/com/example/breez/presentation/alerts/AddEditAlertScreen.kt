package com.example.breez.presentation.alerts

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.breez.WeatherScreenBackground
import com.example.breez.data.db.entity.AlertType
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(Unit) { viewModel.navigateBack.collect { onBackClick() } }
    LaunchedEffect(Unit) { viewModel.showToast.collect { msg -> snackbarHostState.showSnackbar(msg) } }

    WeatherScreenBackground {
        Box(Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is AddEditAlertUiState.Loading -> AlertLoadingScreen()
                is AddEditAlertUiState.Form -> AlertFormScreen(
                    state = state,
                    onBackClick = onBackClick,
                    onTitleChange = viewModel::updateTitle,
                    onDescriptionChange = viewModel::updateDescription,
                    onAlertTypeChange = viewModel::updateAlertType,
                    onStartTimeChange = viewModel::updateStartTime,
                    onEndTimeChange = viewModel::updateEndTime,
                    onToggleAlertMode = viewModel::toggleAlertMode,
                    onToggleLocationSource = viewModel::toggleLocationSource,
                    onSave = viewModel::saveAlert
                )

                is AddEditAlertUiState.Success -> LaunchedEffect(Unit) { onAlertSaved() }
                is AddEditAlertUiState.Error -> AlertErrorScreen(state.message, onBackClick)
            }
            Box(Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)) {
                SnackbarHost(snackbarHostState)
            }
        }
    }
}


@Composable
private fun AlertLoadingScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading alert…", style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Composable
private fun AlertFormScreen(
    state: AddEditAlertUiState.Form,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAlertTypeChange: (AlertType) -> Unit,
    onStartTimeChange: (Date) -> Unit,
    onEndTimeChange: (Date) -> Unit,
    onToggleAlertMode: (Boolean) -> Unit,
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
        Spacer(Modifier.height(12.dp))

        // Top bar
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    if (state.isEditing) "Edit Alert" else "New Alert",
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Configure your weather notification",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

        AlertCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AlertLabel("Alert Title")
                OutlinedTextField(
                    value = state.title, onValueChange = onTitleChange,
                    placeholder = { Text("e.g. Morning rain check") },
                    singleLine = true, isError = state.titleError != null,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                )
                state.titleError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                AlertLabel("Description (optional)")
                OutlinedTextField(
                    value = state.description, onValueChange = onDescriptionChange,
                    placeholder = { Text("Add a note…") }, singleLine = false, maxLines = 3,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                )
            }
        }

        AlertCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AlertLabel("Alert Type")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AlertTypeChip(
                        Modifier.weight(1f),
                        Icons.Outlined.Notifications,
                        "Notification",
                        "Silent",
                        state.alertType == AlertType.NOTIFICATION
                    ) { onAlertTypeChange(AlertType.NOTIFICATION) }
                    AlertTypeChip(
                        Modifier.weight(1f),
                        Icons.Outlined.Alarm,
                        "Alarm",
                        "High priority",
                        state.alertType == AlertType.ALARM
                    ) { onAlertTypeChange(AlertType.ALARM) }
                }
            }
        }

        AlertCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AlertLabel("Alert Mode")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AlertModeChip(
                        Modifier.weight(1f),
                        "One Time",
                        "At specific time",
                        state.isOneTimeAlert
                    ) { onToggleAlertMode(true) }
                    AlertModeChip(
                        Modifier.weight(1f),
                        "Time Window",
                        "Repeated in range",
                        !state.isOneTimeAlert
                    ) { onToggleAlertMode(false) }
                }
            }
        }

        AlertCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AlertLabel(if (state.isOneTimeAlert) "Alert Time" else "Time Window")
                AlertDateTimePicker(
                    label = if (state.isOneTimeAlert) "Date & Time" else "Start Date & Time",
                    date = state.startTime,
                    onChange = onStartTimeChange
                )
                if (!state.isOneTimeAlert) {
                    AlertDateTimePicker("End Date & Time", state.endTime, onEndTimeChange)
                }
                state.timeError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        AlertCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AlertLabel("Location")
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(
                        checked = state.useCurrentLocation,
                        onCheckedChange = onToggleLocationSource
                    )
                    Text(
                        if (state.useCurrentLocation) "Use current GPS location" else "Use specific location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (state.cityName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            state.cityName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.80f)
                        )
                    }
                }
                state.locationError?.let {
                    Text(
                        it,
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
                    Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Outlined.Save, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.isEditing) "Update Alert" else "Create Alert",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertDateTimePicker(label: String, date: Date, onChange: (Date) -> Unit) {
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("EEE, d MMM yyyy  HH:mm", Locale.getDefault()) }
    val cal = remember(date) { Calendar.getInstance().also { it.time = date } }

    fun openPickers() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onChange(Calendar.getInstance().also {
                            it.set(year, month, day, hour, minute, 0)
                            it.set(Calendar.MILLISECOND, 0)
                        }.time)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).also {
            it.datePicker.minDate = System.currentTimeMillis() - 1_000
            it.show()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
        OutlinedTextField(
            value = formatter.format(date),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = ::openPickers) {
                    Icon(Icons.Outlined.Schedule, "Pick time")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openPickers() },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun AlertErrorScreen(message: String, onBackClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        Arrangement.Center, Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Error,
            null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Error", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBackClick) { Text("Go Back") }
    }
}

@Composable
private fun AlertCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.22f else 0.74f),
        tonalElevation = 0.dp, shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.16f
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun AlertLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun AlertTypeChip(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, subLabel: String,
    selected: Boolean, onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.30f
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            subLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun AlertModeChip(
    modifier: Modifier,
    label: String,
    subLabel: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.30f
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            subLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
    }
}

@Composable
internal fun glassSurfaceColor(): Color =
    MaterialTheme.colorScheme.surface.copy(alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.22f else 0.72f)

@Composable
internal fun glassBorderColor(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White.copy(alpha = 0.08f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)