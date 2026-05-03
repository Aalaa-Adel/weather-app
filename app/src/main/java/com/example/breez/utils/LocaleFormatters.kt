package com.example.breez.utils

import java.text.NumberFormat
import java.util.Locale

private fun currentAppLocale(): Locale {
    val locale = Locale.getDefault()
    return if (locale.language == "ar") {
        Locale.forLanguageTag("ar-EG-u-nu-arab")
    } else {
        locale
    }
}

fun formatIntLocalized(value: Int): String {
    return NumberFormat.getIntegerInstance(currentAppLocale()).format(value)
}

fun formatDoubleLocalized(
    value: Double,
    maxFractionDigits: Int = 0
): String {
    return NumberFormat.getNumberInstance(currentAppLocale()).apply {
        maximumFractionDigits = maxFractionDigits
        minimumFractionDigits = 0
    }.format(value)
}