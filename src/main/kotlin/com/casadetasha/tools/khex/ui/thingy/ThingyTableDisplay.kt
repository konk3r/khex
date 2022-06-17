package com.casadetasha.tools.khex.ui.thingy

import HexFile
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.FileContentMatcher
import com.casadetasha.tools.khex.file.ThingyTableFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ThingyTableDisplay(thingyTableFileFlow: MutableStateFlow<ThingyTableFile>, hexFileFlow: StateFlow<HexFile>) {
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
                false -> GenerateContent(FileContentMatcher(hexFileFlow))
            }
        }
    }
}
