package com.casadetasha.tools.khex// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import HexFile
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.ui.KhexTable
import com.casadetasha.tools.khex.ui.KhexTableHeader
import com.casadetasha.tools.khex.ui.ThingyTableDisplay
import kotlinx.coroutines.flow.MutableStateFlow

lateinit var khexTypography: Typography

private var thingyTableFileFlow: MutableStateFlow<ThingyTableFile> = MutableStateFlow(ThingyTableFile.emptyTable)
private var hexFileFlow: MutableStateFlow<HexFile> = MutableStateFlow(
    HexFile.parseFile(null, thingyTableFileFlow)
)

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
    MaterialTheme(
        typography = khexTypography
    ) {
        Row {
            Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)) {
                KhexTableHeader(hexFileFlow, thingyTableFileFlow)
                KhexTable(hexFileFlow)
            }

            ThingyTableDisplay(thingyTableFileFlow)
        }
    }
}

