// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JPanel

fun main() = application {
    Window(
        state = WindowState(size = DpSize(1600.dp, 1200.dp)),
        title = "Khex Editor",
        onCloseRequest = ::exitApplication,
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var isFileChooserOpen by remember { mutableStateOf(false) }
        var file: File? by remember { mutableStateOf(null) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Box(
                modifier = Modifier.padding(end = 16.dp, top = 16.dp, bottom = 32.dp),
            ) {
                when (file?.name) {
                    null -> Button( onClick = { isFileChooserOpen = true } ) { Text("Select File") }
                    else -> Text("File: ${file!!.absolutePath}")
                }
            }

            if (isFileChooserOpen) {
                SelectFileDialog{
                    file = it
                    isFileChooserOpen = false
                }
            }

            HexHeaderRow()
            HexTable(file)
        }
    }
}

@Composable
fun SelectFileDialog(onFileSelected: (File?) -> Unit) {
    SwingPanel(
        factory = {
            JPanel().apply {
                val chooser = JFileChooser("""C:\Users\konk3\OneDrive\projects\rpi-bs-zelda""")
                val file = when (chooser.showOpenDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> chooser.selectedFile
                    else -> null
                }
                onFileSelected(file)
            }
        }
    )
}
