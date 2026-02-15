package com.pl.myweightapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pl.myweightapp.R

//val PlaywriteFontFamily = FontFamily(
//    Font(
//        resId = R.font.playwrite_nz_basic_variable,
//        weight = FontWeight.Normal
//    )
//)
val OrbitronFontFamily = FontFamily(
    Font(
        resId = R.font.orbitron_variable,
        weight = FontWeight.Normal
    )
)

//val RobotoFontFamily = FontFamily(
//    Font(
//        resId = R.font.roboto_variable,
//        weight = FontWeight.Normal
//    ),
//    Font(
//        resId = R.font.roboto_italic_variable,
//        weight = FontWeight.Normal,
//        style = FontStyle.Italic
//    )
//)



// Set of Material typography styles to start with
val Typography = Typography(
    /*bodyLarge = Typography().bodyLarge.copy(
        fontFamily = OrbitronFontFamily
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = OrbitronFontFamily
    ),*/

    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
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