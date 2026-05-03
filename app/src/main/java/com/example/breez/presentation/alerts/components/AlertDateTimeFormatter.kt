package com.example.breez.presentation.alerts.components


import com.example.breez.utils.toLocalizedDigits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatAlertDateTime(date: Date): String {
    return SimpleDateFormat("EEE, d MMM yyyy  HH:mm", Locale.getDefault())
        .format(date)
        .toLocalizedDigits()
}

fun formatAlertDate(date: Date): String {
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        .format(date)
        .toLocalizedDigits()
}

fun formatAlertTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(date)
        .toLocalizedDigits()
}