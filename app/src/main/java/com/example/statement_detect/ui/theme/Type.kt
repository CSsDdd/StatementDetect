package com.example.statement_detect.ui.theme

import android.R.attr.fontFamily
import com.example.statement_detect.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DigitalFontFamily = FontFamily(
    Font(R.font.digital7, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.digital7_mono, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.digital7_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.digital7_mono_italic, FontWeight.Normal, FontStyle.Italic)
)
val DigitalMono = FontFamily(
    Font(R.font.digital7_mono)  // 清爽！不需要反引号
)
// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    displayLarge = TextStyle(
        fontFamily = DigitalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)