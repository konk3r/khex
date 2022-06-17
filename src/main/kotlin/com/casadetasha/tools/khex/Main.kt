package com.casadetasha.tools.khex// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import HexFile
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.ui.hex.KhexTable
import com.casadetasha.tools.khex.ui.thingy.ThingyTableDisplay
import kotlinx.coroutines.flow.MutableStateFlow

lateinit var khexTypography: Typography

fun main() = application {
    var isTableWindowOpen by remember { mutableStateOf(false) }
    Window(
        state = WindowState(size = DpSize(1450.dp, 800.dp)),
        title = "Khex editor",
        onCloseRequest = ::exitApplication,
    ) {
        khexTypography = MaterialTheme.typography.copy(
            body2 = TextStyle.Default.copy(fontFamily = FontFamily.Monospace)
        )
        MenuBar {
            Menu("File") {
                Item("Open table window", onClick = { isTableWindowOpen = true })
            }
        }

        if (isTableWindowOpen) {
            Window(
                state = WindowState(size = DpSize(400.dp, 600.dp)),
                title = "Table window",
                onCloseRequest = { isTableWindowOpen = false }
            ) {
                Box(modifier = Modifier.background(color = Color.Black))
            }
        }

        App()
    }
}

@Composable
@Preview
fun App() {
    val thingyTableFileFlow: MutableStateFlow<ThingyTableFile> = MutableStateFlow(ThingyTableFile.emptyTable)
    val hexFile = HexFile.parseFile(null, thingyTableFileFlow)
    val hexFileFlow: MutableStateFlow<HexFile> = MutableStateFlow(hexFile)

    MaterialTheme(
        typography = khexTypography
    ) {
        Column {
            Row {
                KhexTable(hexFileFlow, thingyTableFileFlow)

                ThingyTableDisplay(thingyTableFileFlow, hexFileFlow)
            }
        }
    }
}

