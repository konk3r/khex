import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class HexRow(val rowNumber: Int, val bytes: List<Byte>)

@Composable
fun BodyRow(hexRow: HexRow) {
    val byteState = hexRow.bytes.map { mutableStateOf(it) }
    var colorState by remember { mutableStateOf(Color.Transparent) }

    Row(
        modifier = Modifier.background(color = colorState),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
//            .pointerMoveFilter(
//                onEnter = { colorState = Color.LightGray; false },
//                onExit = { colorState = Color.Transparent; false }
//            )
    ) {
        RowNumberCell(hexRow.rowNumber)
        byteState.forEach { byteState -> HexCell(byteState = byteState) { byteState.value = it.toByte() } }
        Box(modifier = Modifier.cellSize())
        byteState.forEach { byteState ->
            PreviewCell(byteState) {
                if (it.length == 1) {
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
fun RowNumberCell(rowNumber: Int) {
    val hexValue = "%08x".format(rowNumber)
    SelectionContainer {
        Text(
            text = hexValue,
            modifier = Modifier.rowNumberSize(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun HexCell(byteState: State<Byte>, onTextChanged: (String) -> Unit) {
    val byte by remember { byteState }
    val hexValue by derivedStateOf { "%02x".format(byte) }
    SelectionContainer {
        Text(
            text = hexValue,
            modifier = Modifier.cellSize().background(color = Color.LightGray),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PreviewCell(byteState: State<Byte>, onTextChanged: (String) -> Unit) {
    val byte by remember { byteState }
    var isSelected by remember { mutableStateOf(false) }
    val byteValue: TextFieldValue by derivedStateOf {
        val selectionRange = if (isSelected) TextRange(0, 1) else TextRange.Zero
        when (val intValue = byte.toInt()) {
            0 -> TextFieldValue("", selection = selectionRange)
            else -> TextFieldValue(intValue.toChar().toString(), selection = selectionRange)
        }
    }

    BasicTextField(
        value = byteValue,
        onValueChange = { if (it.text.length <= 1) (onTextChanged(it.text)) },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        modifier = Modifier.previewSize()
            .onFocusChanged { focusState ->
                when (focusState.isFocused) {
                    true -> isSelected = true
                    false -> isSelected = false
                }
            },
    )
}

internal fun Modifier.rowNumberSize(): Modifier {
    return this.size(width = 124.dp, height = 32.dp).defaultMinSize(minWidth = 64.dp, minHeight = 32.dp)
}

private fun Modifier.cellSize(): Modifier {
    return this.size(width = 32.dp, height = 32.dp).defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
}

private fun Modifier.previewSize(): Modifier {
    return this.size(width = 14.dp, height = 32.dp).defaultMinSize(minWidth = 14.dp, minHeight = 32.dp)
}

