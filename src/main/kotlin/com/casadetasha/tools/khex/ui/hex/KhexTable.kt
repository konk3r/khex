package com.casadetasha.tools.khex.ui.hex

import HexFile
import HexRowIndexes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.ThingyTableFile
import com.casadetasha.tools.khex.khexTypography
import com.casadetasha.tools.khex.ui.hex.StaticCellValues.HEADER_CELLS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import toKey

@Composable
fun KhexTable(hexFileFlow: MutableStateFlow<HexFile>, thingyTableFileFlow: StateFlow<ThingyTableFile>) {
    val listCoroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val hexFileState = hexFileFlow.collectAsState()
    val hexFile by remember { hexFileState }
    val hexRows = hexFile.lineIndexes

    listCoroutineScope.launch {
        hexFile.searchResultFlow.filterNotNull().collectLatest {
            val paddedIndex = Integer.max(it.first - 1, 0)
            listState.scrollToItem(paddedIndex)
        }
    }

    Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)) {
        KhexTableHeader(hexFileFlow, thingyTableFileFlow)
        Column {
            KhexTableCellHeaderRow()

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(
                    items = hexRows
                ) { row ->
                    KhexTableRow(hexFile, row)
                }
            }
        }
    }
}

@Composable
fun KhexTableCellHeaderRow() {
    Row {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.rowNumberSize())
            (HEADER_CELLS + " ").forEach {
                HeaderCell(it)
            }
            HEADER_CELLS.forEach {
                PreviewHeaderCell(it)
            }
        }
    }
}

@Composable
fun KhexTableRow(hexRepo: HexFile, hexRow: HexRowIndexes) {
    ProvideTextStyle(khexTypography.body2) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RowNumberCell(hexRow.rowIndex)

            (0 until hexRow.columnCount).forEach { columnIndex ->
                val byteState: State<Byte> = hexRepo.getCellByteFlow(hexRow.rowIndex, columnIndex).collectAsState()
                HexCell(byteState = byteState)

                DisposableEffect(Pair(hexRow.rowIndex, columnIndex).toKey()) {
                    onDispose { hexRepo.cleanupCellByteFlow(hexRow.rowIndex, columnIndex) }
                }
            }

            Box(modifier = Modifier.cellSize())

            (0 until hexRow.columnCount).forEach { columnIndex ->
                val byteState: State<Char> = hexRepo.getPreviewCellByteFlow(hexRow.rowIndex, columnIndex).collectAsState()

                PreviewCell(byteState) { incomingString ->
                    if (incomingString.length == 1) {
                        val char = incomingString.first()
                        hexRepo.updateWithTableConversion(char, rowIndex = hexRow.rowIndex, columnIndex = columnIndex)
                    } else {
                        hexRepo.updateWithTableConversion(0.toChar(), rowIndex = hexRow.rowIndex, columnIndex = columnIndex)
                    }
                }

                DisposableEffect(Pair(hexRow.rowIndex, columnIndex).toKey()) {
                    onDispose { hexRepo.cleanupCellByteFlow(hexRow.rowIndex, columnIndex) }
                }
            }
        }
    }
}

@Composable
fun RowNumberCell(rowNumber: Int) {
    val hexValue = "0x%08x".format(rowNumber)
    SelectionContainer {
        Text(
            text = hexValue,
            modifier = Modifier.rowNumberSize(),
            textAlign = TextAlign.Center,
        )
    }
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

object StaticCellValues {

    val HEADER_CELLS = listOf(
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
    )
}

internal fun Modifier.rowNumberSize(): Modifier {
    return this.size(width = 124.dp, height = 32.dp).defaultMinSize(minWidth = 64.dp, minHeight = 32.dp)
}

internal fun Modifier.cellSize(): Modifier {
    return this.size(width = 32.dp, height = 32.dp).defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
}
