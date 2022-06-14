import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

private val tableFileNameFlow = MutableStateFlow("")

@Composable
fun ThingyTableDisplay(thingyTableFlow: MutableStateFlow<ThingyTable>) {
    val thingyTableState = thingyTableFlow.collectAsState()
    val thingyTable by remember { thingyTableState }
    var isTableFileChooserOpen by remember { mutableStateOf(false) }

    ProvideTextStyle(khexTypography.body2) {
        Column {
            Text("Conversion table", style = MaterialTheme.typography.h6)

            if (isTableFileChooserOpen) {
                SelectFileDialog(
                    fileExtension = FileExtensionInfo(
                        description = "Thingy table file",
                        extension = "tbl"
                    )
                ) { file ->
                    tableFileNameFlow.value = file?.absolutePath ?: ""
                    file?.let { thingyTableFlow.value = ThingyTable.parseFromFile(it) }
                    isTableFileChooserOpen = false
                }
            }

            Button(onClick = { isTableFileChooserOpen = true }) { Text("Select thingy table file (.tbl)") }

            if (tableFileNameFlow.value.isNotEmpty()) {
                Text("File: ${tableFileNameFlow.value}", modifier = Modifier.padding(bottom = 16.dp))
            }

            val thingyHexMapListState = thingyTable.hexMapListFlow.collectAsState()
            val thingyHexMapList by remember { thingyHexMapListState }
            LazyColumn {
                items(items = thingyHexMapList) { thingyRow ->
                    Text("0x${thingyRow.first} = ${thingyRow.second}", modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

