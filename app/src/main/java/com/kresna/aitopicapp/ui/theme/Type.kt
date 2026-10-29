package com.kresna.aitopicapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Base = TextStyle(fontFamily = FontFamily.Default)

/** Big, heavy headlines for one-idea-per-screen cards; roomy body text for reading on a phone. */
val Typography = Typography(
    displaySmall = Base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge = Base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp),
    headlineMedium = Base.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 31.sp),
    headlineSmall = Base.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp),
    titleLarge = Base.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = Base.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    titleSmall = Base.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = Base.copy(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 27.sp),
    bodyMedium = Base.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = Base.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = Base.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = Base.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.25.sp),
    labelSmall = Base.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.sp)
)
