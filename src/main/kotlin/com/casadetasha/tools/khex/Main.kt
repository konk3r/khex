package com.casadetasha.tools.khex// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import HexFile
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.ui.hex.KhexTable
import com.casadetasha.tools.khex.ui.thingy.ThingyTableDisplay
import kotlinx.coroutines.flow.MutableStateFlow

lateinit var khexTypography: Typography

fun main() = application {
    Window(
        state = WindowState(size = DpSize(1400.dp, 800.dp)),
        title = "Khex editor",
        onCloseRequest = ::exitApplication,
    ) {
        khexTypography = MaterialTheme.typography.copy(
            body2 = TextStyle.Default.copy(fontFamily = FontFamily.Monospace)
        )

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
        Row {
            KhexTable(hexFileFlow, thingyTableFileFlow)

            ThingyTableDisplay(thingyTableFileFlow)
        }
    }
}

