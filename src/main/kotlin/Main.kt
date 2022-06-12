// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val listState = rememberLazyListState()
        var isFileChooserOpen by remember { mutableStateOf(false) }
        var file: File? by remember { mutableStateOf(null) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Button(
                modifier = Modifier.padding(end = 16.dp, top = 16.dp, bottom = 32.dp),
                onClick = { isFileChooserOpen = true }
            ) {
                Text("Select File")
            }

            when (file?.name) {
                null -> Text("")
                else -> Text("File: ${file!!.absolutePath} | ${file!!.name}")
            }

            if (isFileChooserOpen) {
                SwingPanel(
                    factory = {
                        JPanel().apply {
                            val chooser = JFileChooser("""C:\Users\konk3\OneDrive\projects\rpi-bs-zelda""")
                            file = when (chooser.showOpenDialog(null)) {
                                JFileChooser.APPROVE_OPTION -> chooser.selectedFile
                                else -> null
                            }
                            isFileChooserOpen = false
                        }
                    }
                )
            }

            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row {
                            headersCells.forEach {
                                HeaderCell(it)
                            }
                            previewHeaderCells.forEach {
                                PreviewHeaderCell(it)
                            }
                        }
                    }

                    val rows: List<List<Byte>> = if (file != null) {
                        file!!.readBytes().asSequence()
                            .chunked(16)
                            .toList()
                    } else {
                        listOf(testRow)
                    }

                    items(
                        items = rows
                    ) {
                        BodyRow(it)
                    }

                }
            }
        }
    }
}

@Composable
fun BodyRow(cellText: List<Byte>) {
    check(cellText.size <= 16) { "Invalid hex row. Row must contain 16 values or less. Actual size was ${cellText.size}" }

    Row {
        cellText
            .map { "%02x".format(it) }
            .forEach { value ->
            BodyCell(text = value)
        }

        Box(modifier = Modifier.cellSize())

        cellText.forEach {
            PreviewCell( it.toInt().toChar().toString() )
        }
    }
}

val headersCells = listOf(
         "0",
         "1",
         "2",
         "3",
         "4",
         "5",
         "6",
         "7",
         "8",
         "9",
         "A",
         "B",
         "C",
         "D",
         "E",
         "F",
         " ",
)

val previewHeaderCells = listOf(
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "A",
    "B",
    "C",
    "D",
    "E",
    "F"
)

private fun Modifier.cellSize(): Modifier {
    return this.size(width = 32.dp, height = 32.dp).defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
}
private fun Modifier.previewSize(): Modifier {
    return this.size(width = 14.dp, height = 32.dp).defaultMinSize(minWidth = 14.dp, minHeight = 32.dp)
}

@Composable
fun HeaderCell(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.cellSize()
    )
}

@Composable
fun PreviewHeaderCell(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.previewSize()
    )
}

@Composable
fun BodyCell(text: String) {
    var textValue by remember { mutableStateOf(text) }
    BasicTextField(
        value = textValue,
        onValueChange = { textValue = it },
        modifier = Modifier.cellSize()
    )
//    TextField(
//        value = textValue,
//        textAlign = TextAlign.Center,
//        modifier = Modifier.cellSize(),
//        onValueChange = { textValue.value = it }
//    )
}

@Composable
fun PreviewCell(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = Modifier.previewSize()
    )
}

val testRow: List<Byte> = listOf(
    "H".toCharArray().first().code.toByte(),
    "e".toCharArray().first().code.toByte(),
    "l".toCharArray().first().code.toByte(),
    "l".toCharArray().first().code.toByte(),
    "o".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
    "G".toCharArray().first().code.toByte(),
    "e".toCharArray().first().code.toByte(),
    "n".toCharArray().first().code.toByte(),
    "e".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
    " ".toCharArray().first().code.toByte(),
)
