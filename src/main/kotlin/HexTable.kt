import StaticCellValues.HEADER_CELLS
import StaticCellValues.TEST_ROW
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.File


@Composable
fun HexTable(file: File?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row {
                (HEADER_CELLS + " ").forEach {
                    HeaderCell(it)
                }
                HEADER_CELLS.forEach {
                    PreviewHeaderCell(it)
                }
            }
        }

        val rows: List<List<Byte>> = file?.readBytes()?.asSequence()?.chunked(16)?.toList() ?: listOf(TEST_ROW)
        items(
            items = rows
        ) {row ->
            BodyRow( row.map { it } )
        }

    }
}

@Composable
fun BodyRow(cellText: List<Byte>) {
    check(cellText.size <= 16) { "Invalid hex row. Row must contain 16 values or less. Actual size was ${cellText.size}" }
    val byteState = cellText.map { mutableStateOf(it) }

    Row {
        byteState.forEach { byteState -> BodyCell(byteState = byteState) { byteState.value = it.toByte() } }
        Box(modifier = Modifier.cellSize())
        byteState.forEach { byteState -> PreviewCell(byteState) {
            if (it.length == 1)  {
                byteState.value = it.first().code.toByte()
            } else {
                byteState.value = 0.toByte()
            }
        }
        }
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
fun BodyCell(byteState: State<Byte>, onTextChanged: (String) -> Unit) {
    val byte by remember { byteState }
    val hexValue by derivedStateOf { "%02x".format(byte) }
    BasicTextField(
        value = hexValue,
        onValueChange = { onTextChanged(it) },
        modifier = Modifier.cellSize(),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
    )
}

@Composable
fun PreviewCell(byteState: State<Byte>, onTextChanged: (String) -> Unit) {
    val byte by remember { byteState }
    val byteValue by derivedStateOf {
        when (val intValue = byte.toInt()) {
            0 -> ""
            else -> intValue.toChar().toString()
        }
    }

    BasicTextField(
        value = byteValue,
        onValueChange = { if (it.length <= 1) ( onTextChanged(it) ) },
        modifier = Modifier.previewSize(),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
    )
}

private fun Modifier.cellSize(): Modifier {
    return this.size(width = 32.dp, height = 32.dp).defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
}

private fun Modifier.previewSize(): Modifier {
    return this.size(width = 14.dp, height = 32.dp).defaultMinSize(minWidth = 14.dp, minHeight = 32.dp)
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

    val TEST_ROW: List<Byte> = listOf(
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
}
