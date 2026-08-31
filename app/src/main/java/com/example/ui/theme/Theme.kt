package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val YapLightColorScheme = lightColorScheme(
    primary = YapPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = YapWindowHeader,
    onPrimaryContainer = YapPrimaryDark,
    
    secondary = YapOrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = YapOrangeBg,
    onSecondaryContainer = YapOrangeDark,
    
    tertiary = AvatarTealBase,
    onTertiary = Color.White,
    
    background = YapCreamBackground,
    onBackground = YapHeadingBlack,
    
    surface = YapCardBackground,
    onSurface = YapHeadingBlack,
    surfaceVariant = YapWindowHeader,
    onSurfaceVariant = YapSubtextGray,
    
    outline = YapBorderColor,
    outlineVariant = YapBorderSubtle,
    
    error = DotRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YapLightColorScheme,
        typography = Typography,
        content = content
    )
}
