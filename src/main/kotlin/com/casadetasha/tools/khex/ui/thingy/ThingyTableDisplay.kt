package com.casadetasha.tools.khex.ui.thingy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.FileExtensionInfo
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.khexTypography
import com.casadetasha.tools.khex.ui.SelectFileDialog
import kotlinx.coroutines.flow.MutableStateFlow

private val tableFileNameFlow = MutableStateFlow("")
private val searchResultFlow: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)

@Composable
fun ThingyTableDisplay(thingyTableFileFlow: MutableStateFlow<ThingyTableFile>) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val isFileTableSelected by derivedStateOf { selectedTabIndex == 0 }


    Column {
        Column(modifier = Modifier.background(color = MaterialTheme.colors.primary)) {
            Text(
                "Conversion table",
                style = MaterialTheme.typography.h5.copy(color = Color.White),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = isFileTableSelected,
                    content = { Text("File Table", modifier = Modifier.padding(8.dp)) },
                    onClick = { selectedTabIndex = 0 },
                )
                Tab(
                    selected = !isFileTableSelected,
                    content = { Text("Generate Table", modifier = Modifier.padding(8.dp)) },
                    onClick = { selectedTabIndex = 1 }
                )
            }
        }

        Column(modifier = Modifier.background(color = Color(0xFFECECEC)).fillMaxSize()) {

            when (isFileTableSelected) {
                true -> FileContent(thingyTableFileFlow)
                false -> GenerateContent()
            }
        }
    }
}

@Composable
fun GenerateContent() {
    val state = searchResultFlow.collectAsState()
    val searchResultValue by remember { state }
    var matchTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    Column {
        TextField(
            value = matchTextFieldValue,
            label = { Text("Match text") },
            onValueChange = {
                matchTextFieldValue = it
            },
            textStyle = LocalTextStyle.current,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
        )
        if (searchResultValue != null) {
            val searchTextValue by derivedStateOf { "Result: $searchResultValue" }
            Text(searchTextValue)
        }
        Button(onClick = { matchText(matchTextFieldValue.text) }, content = { Text("MATCH") })
    }
}

fun matchText(text: String) {
    searchResultFlow.value = if (text.isNotEmpty()) Pair(0,0) else null
}

@Composable
fun FileContent(thingyTableFileFlow: MutableStateFlow<ThingyTableFile>) {
    val thingyTableState = thingyTableFileFlow.collectAsState()
    val thingyTable by remember { thingyTableState }
    val thingyHexMapListState = thingyTable.hexMapListFlow.collectAsState()
    val thingyHexMapList by remember { thingyHexMapListState }
    var isTableFileChooserOpen by remember { mutableStateOf(false) }
    if (isTableFileChooserOpen) {
        SelectFileDialog(
            fileExtension = FileExtensionInfo(
                description = "Thingy table file",
                extension = "tbl"
            )
        ) { file ->
            tableFileNameFlow.value = file?.name ?: ""
            file?.let { thingyTableFileFlow.value = ThingyTableFile.parseFromFile(it) }
            isTableFileChooserOpen = false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (tableFileNameFlow.value.isNotEmpty()) {
            Text(
                "File: ${tableFileNameFlow.value}",
                modifier = Modifier.padding(bottom = 2.dp),
                fontWeight = FontWeight.Bold
            )
        } else {
            Text("File:", modifier = Modifier.padding(bottom = 2.dp), fontWeight = FontWeight.Bold)
        }

        Button(onClick = { isTableFileChooserOpen = true }) { Text("LOAD TABLE FILE") }

        LazyColumn {
            items(items = thingyHexMapList) { thingyRow ->
                Text(
                    "0x${thingyRow.first} = ${thingyRow.second}",
                    modifier = Modifier.fillMaxSize(),
                    style = khexTypography.body2.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
