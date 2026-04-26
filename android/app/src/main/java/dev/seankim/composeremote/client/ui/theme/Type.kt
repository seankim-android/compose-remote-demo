package dev.seankim.composeremote.client.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
// Brief Editorial type scale. See docs/design/ for the source spec.

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = emptyList<List<ByteArray>>(),
)

val DisplayFont = FontFamily(
    Font(GoogleFont("Newsreader"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Newsreader"), provider, weight = FontWeight.SemiBold),
)
val BodyFont = FontFamily(
    Font(GoogleFont("Inter"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Inter"), provider, weight = FontWeight.Medium),
    Font(GoogleFont("Inter"), provider, weight = FontWeight.SemiBold),
)
val MetaFont = FontFamily(
    Font(GoogleFont("Space Grotesk"), provider, weight = FontWeight.Medium),
)
val MonoFont = FontFamily(
    Font(GoogleFont("JetBrains Mono"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("JetBrains Mono"), provider, weight = FontWeight.Medium),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = MetaFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = MetaFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.06.sp,
    ),
)
