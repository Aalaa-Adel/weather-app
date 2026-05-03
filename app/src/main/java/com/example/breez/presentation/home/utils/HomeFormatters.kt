package com.example.breez.presentation.home.utils

import com.example.breez.utils.toLocalizedDigits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

private fun timeZoneFromOffset(offsetSeconds: Int): TimeZone {
    val sign = if (offsetSeconds >= 0) "+" else "-"
    val absOffset = abs(offsetSeconds)
    val hours = absOffset / 3600
    val minutes = (absOffset % 3600) / 60
    return TimeZone.getTimeZone("GMT$sign%02d:%02d".format(hours, minutes))
}

fun formatDateTime(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("EEEE, d MMMM | HH:mm", Locale.getDefault())
    formatter.timeZone = timeZoneFromOffset(timezoneOffsetSeconds)
    return formatter.format(Date(timestamp * 1000L)).toLocalizedDigits()
}

fun formatHour(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("ha", Locale.getDefault())
    formatter.timeZone = timeZoneFromOffset(timezoneOffsetSeconds)
    return formatter.format(Date(timestamp * 1000L)).toLocalizedDigits()
}

fun formatDayName(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
    formatter.timeZone = timeZoneFromOffset(timezoneOffsetSeconds)
    return formatter.format(Date(timestamp * 1000L)).toLocalizedDigits()
}

fun formatShortDate(timestamp: Long, timezoneOffsetSeconds: Int): String {
    val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
    formatter.timeZone = timeZoneFromOffset(timezoneOffsetSeconds)
    return formatter.format(Date(timestamp * 1000L)).toLocalizedDigits()
}