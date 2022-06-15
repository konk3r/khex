package com.casadetasha.tools.khex

import androidx.compose.foundation.background
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.casadetasha.tools.khex.compose.cellSize

@Composable
fun HexCell(byteState: State<Byte>) {
    val byte by remember { byteState }
    val hexValue by derivedStateOf { byte.toHex() }
    Text(
        text = hexValue,
        modifier = Modifier.cellSize().background(color = Color.LightGray),
        textAlign = TextAlign.Center,
    )
}

internal fun Byte.toHex() = "%02x".format(this)
