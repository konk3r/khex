package com.casadetasha.tools.khex.ui.hex

import HexFile
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.ui.SelectFileDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun KhexTableHeader(hexFileFlow: MutableStateFlow<HexFile>, thingyTableFileFlow: StateFlow<ThingyTableFile>) {
    val hexFileState = hexFileFlow.collectAsState()
    val hexFile by remember { hexFileState }
    val searchResultFlow: StateFlow<Pair<Int, Int>?> = hexFile.searchResultFlow
    var hexFileName by remember { mutableStateOf("") }

    val searchState = searchResultFlow.collectAsState()
    val searchResultValue by remember { searchState }
    val loadFileText by remember {
        mutableStateOf(
            when (hexFileName.isNotEmpty()) {
                true -> "LOAD A DIFFERENT FILE"
                false -> "LOAD FILE"
            }
        )
    }
    var isSourceFileChooserOpen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    Text("Hex table",
        style = MaterialTheme.typography.h3.copy(
            color = MaterialTheme.colors.primary,
        ),
        modifier = Modifier.padding(bottom = 16.dp))
    Row {

        Column {
            if (hexFileName.isNotEmpty()) {
                Text(
                    "File: $hexFileName",
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

            if (hexFileName.isNotEmpty()) {
                Row {
                    TextField(
                        value = searchText,
                        label = { Text("Search") },
                        onValueChange = {
                            searchText = it
                            hexFile.search(it.text)
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
            hexFileName = file?.name ?: ""
            hexFileFlow.value = HexFile.parseFile(file, thingyTableFileFlow)
            isSourceFileChooserOpen = false
        }
    }
}
