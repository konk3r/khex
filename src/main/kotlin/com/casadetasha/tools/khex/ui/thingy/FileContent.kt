package com.casadetasha.tools.khex.ui.thingy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.FileExtensionInfo
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.khexTypography
import com.casadetasha.tools.khex.ui.SelectFileDialog
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FileContent(thingyTableFileFlow: MutableStateFlow<ThingyTableFile>) {
    val thingyTableState = thingyTableFileFlow.collectAsState()
    val thingyTable by remember { thingyTableState }
    val thingyHexMapListState = thingyTable.hexMapListFlow.collectAsState()
    val thingyHexMapList by remember { thingyHexMapListState }
    var isTableFileChooserOpen by remember { mutableStateOf(false) }
    var tableFileName by remember { mutableStateOf("") }

    if (isTableFileChooserOpen) {
        SelectFileDialog(
            fileExtension = FileExtensionInfo(
                description = "Thingy table file",
                extension = "tbl"
            )
        ) { file ->
            tableFileName = file?.name ?: ""
            file?.let { thingyTableFileFlow.value = ThingyTableFile.parseFromFile(it) }
            isTableFileChooserOpen = false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (tableFileName.isNotEmpty()) {
            Text(
                "File: $tableFileName",
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
