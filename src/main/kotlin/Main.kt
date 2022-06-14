// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

lateinit var khexTypography: Typography

private var thingyTableFlow: MutableStateFlow<ThingyTable> = MutableStateFlow(ThingyTable.emptyTable)
private var hexRepoFlow: MutableStateFlow<HexRepository> = MutableStateFlow(
    HexRepository.parseFile(null, thingyTableFlow)
)

private val searchResultFlow: StateFlow<Pair<Int, Int>?> get() = hexRepoFlow.value.searchResultFlow

private val fileNameFlow = MutableStateFlow("")

fun main() = application {
    Window(
        state = WindowState(size = DpSize(1400.dp, 500.dp)),
        title = "Khex editor",
        onCloseRequest = ::exitApplication,
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    khexTypography = MaterialTheme.typography.copy(
        body2 = TextStyle.Default.copy(fontFamily = FontFamily.Monospace)
    )

    MaterialTheme(
        typography = khexTypography
    ) {
        var isSourceFileChooserOpen by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Box(
                modifier = Modifier.padding(end = 16.dp, top = 16.dp, bottom = 32.dp),
            ) {
                Row {
                    var searchText by remember { mutableStateOf(TextFieldValue("")) }
                    val fileTextState = fileNameFlow.map{
                        when (it.isNotBlank()) {
                            true -> "Load a different file"
                            false -> "Load file"
                        }
                    }.collectAsState("Load file")

                    val fileText by remember { fileTextState }

                    Column {
                        Button(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onClick = { isSourceFileChooserOpen = true }
                        ) {
                            Text(fileText, modifier = Modifier.padding())
                        }

                        if (fileNameFlow.value.isNotEmpty()) {
                            Text("File: ${fileNameFlow.value}", modifier = Modifier.padding(bottom = 16.dp))

                            TextField(
                                value = searchText,
                                label = { Text("Search") },
                                onValueChange = {
                                    searchText = it
                                    searchText(it.text)
                                },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }

                val state = searchResultFlow.collectAsState()
                val searchResultValue by remember { state }
                if (searchResultValue != null) {
                    val searchTextValue by derivedStateOf { "Result: $searchResultValue" }
                    Text(searchTextValue)
                }
            }

            if (isSourceFileChooserOpen) {
                SelectFileDialog { file ->
                    fileNameFlow.value = file?.absolutePath ?: ""
                    hexRepoFlow.value = HexRepository.parseFile(file, thingyTableFlow)
                    isSourceFileChooserOpen = false
                }
            }

            val tableState = hexRepoFlow.collectAsState()
            val tableRepo by remember { tableState }
            val thingyTableState = thingyTableFlow.collectAsState()
            val thingyTable by remember { thingyTableState }

            HexHeaderRow()
            Row {
                HexTable(tableRepo)
                ThingyTableDisplay(thingyTable)
            }
        }
    }
}

@Composable
fun ThingyTableDisplay(thingyTable: ThingyTable) {
    var isTableFileChooserOpen by remember { mutableStateOf(false) }

    Column {
        Row {
            Text("Thingy Table")

            if (isTableFileChooserOpen) {
                SelectFileDialog(
                    fileExtension = FileExtensionInfo(
                        description = "Thingy table file",
                        extension = "tbl"
                    )
                ) { file ->
                    file?.let { thingyTableFlow.value = ThingyTable.parseFromFile(it) }
                    isTableFileChooserOpen = false
                }
            }

            Button(
                modifier = Modifier.padding(start = 16.dp),
                onClick = { isTableFileChooserOpen = true }
            ) { Text("Select thingy table file (.tbl)") }
        }

        LazyColumn {
            items(items = thingyTable.charMap.toList()) { thingyRow ->
                Text("0x${thingyRow.first.toHex()} = ${thingyRow.second}")
            }
        }
    }
}

fun searchText(searchPhrase: String) {
    hexRepoFlow.value.search(searchPhrase)
}

@Composable
fun SelectFileDialog(fileExtension: FileExtensionInfo? = null, onFileSelected: (File?) -> Unit) {
    SwingPanel(
        factory = {
            JPanel().apply {
                val chooser = JFileChooser("""C:\Users\konk3\OneDrive\projects\rpi-bs zelda""")
                fileExtension?.let { chooser.fileFilter = FileNameExtensionFilter(it.description, it.extension) }

                val file = when (chooser.showOpenDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> chooser.selectedFile
                    else -> null
                }
                onFileSelected(file)
            }
        }
    )
}

class FileExtensionInfo(val description: String, val extension: String)
