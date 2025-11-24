package com.stevegiralt.spirals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.stevegiralt.spirals.ui.SpiralScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        SpiralScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}