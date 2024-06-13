package sc.windom.sofill.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CascadeMaterialTheme(
    content: @Composable () -> Unit
) {
    val LightColorScheme = lightColorScheme(
        primary = Color(0xF050A283), // 主要颜色，用于最重要的界面元素，如按钮、选中状态和主要文本
        onPrimary = Color(0xFF405573), // 主要颜色上的文字颜色
        background = Color(0xFFB5D8C3), // 背景色，用于整个屏幕或面板的背景
        onBackground = Color(0xFF495842), // 背景色上的文字颜色
        surface = Color(0xFFE5F0EB), // 表面颜色，用于卡片、菜单和对话框等表面元素的背景
        onSurface = Color(0xFFb8d2bf), // 表面颜色上的文字颜色
        surfaceVariant = Color(0xFF356859), // 表面变体颜色，用于表面元素的不同状态或层次
        onSurfaceVariant = Color(0xFF356859), // 表面变体颜色上的文字颜色
        primaryContainer = Color(0xFFBBAAD8), // 主要颜色容器背景
        onPrimaryContainer = Color(0xFF050223), // 主要颜色容器上的文字颜色
        secondary = Color(0xFFA5A2A3), // 次要颜色
        onSecondary = Color(0xFF05A283), // 次要颜色上的文字颜色
        secondaryContainer = Color(0xFFB592C3), // 次要颜色容器背景
        onSecondaryContainer = Color(0xFFB5D203), // 次要颜色容器上的文字颜色
        tertiary = Color(0xFFBCD2A3), // 第三颜色
        onTertiary = Color(0xFF353253), // 第三颜色上的文字颜色
        tertiaryContainer = Color(0xCFB5D2C3), // 第三颜色容器背景
        onTertiaryContainer = Color(0xFF253933), // 第三颜色容器上的文字颜色
        surfaceTint = Color(0xFF8572A3), // 表面色调颜色，通常用于按钮和图标
        inverseSurface = Color(0xF0A5F2A3), // 倒置表面颜色，用于深色背景上的表面元素
        inverseOnSurface = Color(0xFF6592C3), // 倒置表面上的文字颜色
        inversePrimary = Color(0xFFA57253), // 倒置主要颜色
        error = Color(0xFFB54253), // 错误颜色
        onError = Color(0xFFA5B2C3), // 错误颜色上的文字颜色
        errorContainer = Color(0xFFF54243), // 错误颜色容器背景
        onErrorContainer = Color(0xFFB5D2C3), // 错误颜色容器上的文字颜色
        outline = Color(0xFF555253), // 轮廓颜色，用于分隔线和边框
    )
    val DarkColorScheme = darkColorScheme(
        primary = Color(0xFF1F1B1D), // 主要颜色，用于最重要的界面元素，如按钮、选中状态和主要文本
        onPrimary = Color(0xFFB5D2C3), // 主要颜色上的文字颜色
        background = Color(0xFF000000), // 背景色，用于整个屏幕或面板的背景
        onBackground = Color(0xFFB5D2C3), // 背景色上的文字颜色
        surface = Color(0xFF12121B), // 表面颜色，用于卡片、菜单和对话框等表面元素的背景
        onSurface = Color(0xFF639241), // 表面颜色上的文字颜色
        surfaceVariant = Color(0xFF356859), // 表面变体颜色，用于表面元素的不同状态或层次
        onSurfaceVariant = Color(0xFF8F9D69), // 表面变体颜色上的文字颜色
        primaryContainer = Color(0xCF554263), // 主要颜色容器背景
        onPrimaryContainer = Color(0xFF655203), // 主要颜色容器上的文字颜色
        secondary = Color(0xFF958283), // 次要颜色
        onSecondary = Color(0xFF05A283), // 次要颜色上的文字颜色
        secondaryContainer = Color(0xFF855263), // 次要颜色容器背景
        onSecondaryContainer = Color(0xFFB5C203), // 次要颜色容器上的文字颜色
        tertiary = Color(0xFF9CA283), // 第三颜色
        onTertiary = Color(0xFF8592A3), // 第三颜色上的文字颜色
        tertiaryContainer = Color(0xCF759283), // 第三颜色容器背景
        onTertiaryContainer = Color(0xFFC58993), // 第三颜色容器上的文字颜色
        surfaceTint = Color(0xFF253243), // 表面色调颜色，通常用于按钮和图标
        inverseSurface = Color(0xF095A263), // 倒置表面颜色，用于深色背景上的表面元素
        inverseOnSurface = Color(0xFF6592C3), // 倒置表面上的文字颜色
        inversePrimary = Color(0xFFA57253), // 倒置主要颜色
        error = Color(0xFFC54233), // 错误颜色
        onError = Color(0xFFA5B2C3), // 错误颜色上的文字颜色
        errorContainer = Color(0xFFC54243), // 错误颜色容器背景
        onErrorContainer = Color(0xFFB5D2C3), // 错误颜色容器上的文字颜色
        outline = Color(0xFF656263), // 轮廓颜色，用于分隔线和边框
    )
    // Dynamic color 有点麻烦，没弄懂
//    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
//    val colorScheme = when {
//        dynamicColor && isSystemInDarkTheme() -> dynamicDarkColorScheme(LocalContext.current)
//        dynamicColor && !isSystemInDarkTheme() -> dynamicLightColorScheme(LocalContext.current)
//        isSystemInDarkTheme() -> DarkColorScheme
//        else -> LightColorScheme
//    }
    val colorScheme =
        if (isSystemInDarkTheme()) {
            DarkColorScheme
        } else {
            LightColorScheme
        }
    val typography = Typography(
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        ),
    )
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(12.dp)
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes
    ) {
        content()
    }
}