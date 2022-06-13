import StaticCellValues.HEADER_CELLS
import StaticCellValues.TEST_ROW
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File


@Composable
fun HexHeaderRow() {
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
fun HexTable(file: File?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var rowCount = 0
        val byteRows: List<List<Byte>> = file?.readBytes()?.asSequence()?.chunked(16)?.toList() ?: listOf(TEST_ROW)
        val hexRows: List<HexRow> = byteRows.map { HexRow(rowCount++, it) }

        items(
            items = hexRows
        ) { row ->
            BodyRow(row)
        }
    }
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
