package com.example.breez.presentation.alerts.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.breez.R
import com.example.breez.presentation.alerts.components.formatAlertDateTime
import java.util.Calendar
import java.util.Date

@Composable
fun AlertDateTimePicker(
    label: String,
    date: Date,
    onChange: (Date) -> Unit
) {
    val context = LocalContext.current
    val cal = remember(date) { Calendar.getInstance().also { it.time = date } }

    fun openPickers() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onChange(
                            Calendar.getInstance().also {
                                it.set(year, month, day, hour, minute, 0)
                                it.set(Calendar.MILLISECOND, 0)
                            }.time
                        )
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
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )

        OutlinedTextField(
            value = formatAlertDateTime(date),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = ::openPickers) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = stringResource(R.string.cd_pick_time)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openPickers() },
            shape = RoundedCornerShape(16.dp)
        )
    }
}