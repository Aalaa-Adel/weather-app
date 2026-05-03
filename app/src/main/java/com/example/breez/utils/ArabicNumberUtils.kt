package com.example.breez.utils

import java.util.Locale

fun String.toLocalizedDigits(): String {
    return if (Locale.getDefault().language == "ar") {
        this
            .replace('0', '٠')
            .replace('1', '١')
            .replace('2', '٢')
            .replace('3', '٣')
            .replace('4', '٤')
            .replace('5', '٥')
            .replace('6', '٦')
            .replace('7', '٧')
            .replace('8', '٨')
            .replace('9', '٩')
    } else {
        this
    }
}