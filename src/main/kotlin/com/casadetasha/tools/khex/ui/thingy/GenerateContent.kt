package com.casadetasha.tools.khex.ui.thingy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.FileContentMatcher
import com.casadetasha.tools.khex.file.FileExtensionInfo
import com.casadetasha.tools.khex.ui.SaveFileDialog

@Composable
fun GenerateContent(fileContentMatcher: FileContentMatcher) {
    val state = fileContentMatcher.matchResultFlow.collectAsState()
    val searchResultValue by remember { state }
    var matchTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var tableOutputText by remember { mutableStateOf("") }
    var isSaveToFileDialogOpen by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = matchTextFieldValue,
            label = { Text("Match text") },
            onValueChange = {
                matchTextFieldValue = it
            },
            textStyle = LocalTextStyle.current,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        )
        Button(
            onClick = { fileContentMatcher.match(matchTextFieldValue.text) },
            content = { Text("MATCH") },
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        if (searchResultValue.isNotEmpty()) {
            LazyColumn {
                items(items = searchResultValue) {result ->
                    Column {
                        Text("Position: $result", Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp))

                        Row {
                            Button(
                                onClick = { fileContentMatcher.scrollToResult(result) },
                                content = { Text("Scroll to match") },
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 4.dp)
                            )

                            Button(
                                onClick = { fileContentMatcher.applyTable(result) },
                                content = { Text("Apply table") },
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 4.dp)
                            )
                        }

                        Button(
                            onClick = { tableOutputText = fileContentMatcher.generateTable(result) },
                            content = { Text("Generate table") },
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }

                item {
                    if (tableOutputText.isNotBlank()) {
                        Column {
                            Button(
                                onClick = { isSaveToFileDialogOpen = true },
                                content = { Text("Save to file") },
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                            )

                            Text(tableOutputText, Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp))

                            if (isSaveToFileDialogOpen) {
                                SaveFileDialog(
                                    fileExtension = FileExtensionInfo(
                                        description = "Thingy table file",
                                        extension = "tbl"
                                    )
                                ) {
                                        file -> file?.writeBytes(tableOutputText.toByteArray())
                                    isSaveToFileDialogOpen = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
