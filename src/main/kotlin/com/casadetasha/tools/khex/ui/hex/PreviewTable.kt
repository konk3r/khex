package com.casadetasha.tools.khex.ui.hex

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun PreviewHeaderCell(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.previewSize()
    )
}

@Composable
internal fun PreviewCell(charState: State<Char>, onTextChanged: (String) -> Unit) {
    val char by remember { charState }
    var isSelected by remember { mutableStateOf(false) }
    val byteValue: TextFieldValue by derivedStateOf {
        val selectionRange = if (isSelected) TextRange(0, 1) else TextRange.Zero
        TextFieldValue(char.toString(), selection = selectionRange)
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

internal fun Modifier.previewSize(): Modifier {
    return this.size(width = 14.dp, height = 32.dp).defaultMinSize(minWidth = 14.dp, minHeight = 32.dp)
}
