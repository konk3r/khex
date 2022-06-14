import StaticCellValues.HEADER_CELLS
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.lang.Integer.max


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
fun HexTable(repo: HexRepository) {
    val hexRowState = repo.hexRowsFlow.collectAsState()
    val hexRows by remember { hexRowState }
    val listCoroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    listCoroutineScope.launch {
        repo.searchResultFlow.filterNotNull().collectLatest {
            val paddedIndex = max(it.first - 1, 0)
            listState.scrollToItem(paddedIndex)
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            items = hexRows
        ) { row ->
            BodyRow(repo, row)
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
}
