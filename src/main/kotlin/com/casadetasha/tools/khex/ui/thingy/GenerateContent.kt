package com.casadetasha.tools.khex.ui.thingy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.casadetasha.tools.khex.file.FileContentMatcher

@Composable
fun GenerateContent(fileContentMatcher: FileContentMatcher) {
    val state = fileContentMatcher.matchResultFlow.collectAsState()
    val searchResultValue by remember { state }
    var matchTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val searchResultPosition by remember { state }

    Column {
        TextField(
            value = matchTextFieldValue,
            label = { Text("Match text") },
            onValueChange = {
                matchTextFieldValue = it
            },
            textStyle = LocalTextStyle.current,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        )
        Button(
            onClick = { fileContentMatcher.match(matchTextFieldValue.text) },
            content = { Text("MATCH") },
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        if (searchResultValue != null) {
            Text("Position: $searchResultPosition", Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp))

            Button(
                onClick = { fileContentMatcher.scrollToResult() },
                content = { Text("Scroll to match") },
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )

            Button(
                onClick = { fileContentMatcher.scrollToResult() },
                content = { Text("Generate table") },
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}
