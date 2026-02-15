package com.pl.myweightapp.ui.temp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pl.myweightapp.ui.theme.MyWeightAppTheme

@Preview
@Composable
fun ColorSchemeDemoPreview() {
    MyWeightAppTheme {
        ColorSchemeDemo()
    }
}


@Composable
fun ColorSchemeDemo() {
    val colorScheme = MaterialTheme.colorScheme

    // Lista wszystkich kolorów z ich nazwami
    val colors = listOf(
        "primary" to colorScheme.primary,
        "onPrimary" to colorScheme.onPrimary,
        "primaryContainer" to colorScheme.primaryContainer,
        "onPrimaryContainer" to colorScheme.onPrimaryContainer,
        "primaryFixed" to colorScheme.primaryFixed,
        "primaryFixedDim" to colorScheme.primaryFixedDim,
        "onPrimaryFixed" to colorScheme.onPrimaryFixed,
        "onPrimaryFixedVariant" to colorScheme.onPrimaryFixedVariant,
        "secondary" to colorScheme.secondary,
        "onSecondary" to colorScheme.onSecondary,
        "secondaryContainer" to colorScheme.secondaryContainer,
        "onSecondaryContainer" to colorScheme.onSecondaryContainer,
        "secondaryFixed" to colorScheme.secondaryFixed,
        "secondaryFixedDim" to colorScheme.secondaryFixedDim,
        "onSecondaryFixed" to colorScheme.onSecondaryFixed,
        "onSecondaryFixedVariant" to colorScheme.onSecondaryFixedVariant,
        "tertiary" to colorScheme.tertiary,
        "onTertiary" to colorScheme.onTertiary,
        "tertiaryContainer" to colorScheme.tertiaryContainer,
        "onTertiaryContainer" to colorScheme.onTertiaryContainer,
        "tertiaryFixed" to colorScheme.tertiaryFixed,
        "tertiaryFixedDim" to colorScheme.tertiaryFixedDim,
        "onTertiaryFixed" to colorScheme.onTertiaryFixed,
        "onTertiaryFixedVariant" to colorScheme.onTertiaryFixedVariant,
        "error" to colorScheme.error,
        "onError" to colorScheme.onError,
        "errorContainer" to colorScheme.errorContainer,
        "onErrorContainer" to colorScheme.onErrorContainer,
        "background" to colorScheme.background,
        "onBackground" to colorScheme.onBackground,
        "surface" to colorScheme.surface,
        "onSurface" to colorScheme.onSurface,
        "surfaceVariant" to colorScheme.surfaceVariant,
        "onSurfaceVariant" to colorScheme.onSurfaceVariant,
        "outline" to colorScheme.outline,
        "outlineVariant" to colorScheme.outlineVariant,
        "inverseSurface" to colorScheme.inverseSurface,
        "inverseOnSurface" to colorScheme.inverseOnSurface,
        "inversePrimary" to colorScheme.inversePrimary,
        "scrim" to colorScheme.scrim,
        "surfaceBright" to colorScheme.surfaceBright,
        "surfaceDim" to colorScheme.surfaceDim,
        "surfaceContainer" to colorScheme.surfaceContainer,
        "surfaceContainerHigh" to colorScheme.surfaceContainerHigh,
        "surfaceContainerHighest" to colorScheme.surfaceContainerHighest,
        "surfaceContainerLow" to colorScheme.surfaceContainerLow,
        "surfaceContainerLowest" to colorScheme.surfaceContainerLowest
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { (name, color) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(color)
            ) {
                Text(
                    text = name,
                    color = getContrastingTextColor(color), // Funkcja do kontrastującego koloru tekstu
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Pomocnicza funkcja do wyboru kontrastującego koloru tekstu (czarny lub biały)
fun getContrastingTextColor(background: Color): Color {
    val luminance = (0.299 * background.red + 0.587 * background.green + 0.114 * background.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
}