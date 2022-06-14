// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

private val hexFileNameFlow = MutableStateFlow("")

fun main() = application {
    Window(
        state = WindowState(size = DpSize(1400.dp, 800.dp)),
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
        var searchText by remember { mutableStateOf(TextFieldValue("")) }
        val state = searchResultFlow.collectAsState()
        val searchResultValue by remember { state }
        val fileTextState = hexFileNameFlow.collectAsState()
        val loadFileText by remember {
            mutableStateOf(
                when (fileTextState.value.isNotEmpty()) {
                    true -> "LOAD A DIFFERENT FILE"
                    false -> "LOAD FILE"
                }
            )
        }

        Row {
            Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)) {
                Text("Hex table",
                    style = MaterialTheme.typography.h3.copy(
                        color = MaterialTheme.colors.primary,
                    ),
                    modifier = Modifier.padding(bottom = 16.dp))
                Row {

                    Column {
                        if (hexFileNameFlow.value.isNotEmpty()) {
                            Text(
                                "File: ${hexFileNameFlow.value}",
                                modifier = Modifier.padding(bottom = 2.dp),
                                fontWeight = FontWeight.Bold,
                            )
                        } else {
                            Text("File:",
                                modifier = Modifier.padding(bottom = 2.dp),
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Button(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onClick = { isSourceFileChooserOpen = true }
                        ) {
                            Text(loadFileText, modifier = Modifier.padding())
                        }

                        if (hexFileNameFlow.value.isNotEmpty()) {
                            Row {
                                TextField(
                                    value = searchText,
                                    label = { Text("Search") },
                                    onValueChange = {
                                        searchText = it
                                        searchText(it.text)
                                    },
                                    textStyle = LocalTextStyle.current,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
                                )
                                if (searchResultValue != null) {
                                    val searchTextValue by derivedStateOf { "Result: $searchResultValue" }
                                    Text(searchTextValue)
                                }
                            }
                        }
                    }
                }

                if (isSourceFileChooserOpen) {
                    SelectFileDialog { file ->
                        hexFileNameFlow.value = file?.name ?: ""
                        hexRepoFlow.value = HexRepository.parseFile(file, thingyTableFlow)
                        isSourceFileChooserOpen = false
                    }
                }

                val tableState = hexRepoFlow.collectAsState()
                val tableRepo by remember { tableState }

                HexHeaderRow()
                HexTable(tableRepo)
            }

            ThingyTableDisplay(thingyTableFlow)
        }
    }
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

private fun searchText(searchPhrase: String) {
    hexRepoFlow.value.search(searchPhrase)
}

class FileExtensionInfo(val description: String, val extension: String)
