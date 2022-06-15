package com.casadetasha.tools.khex.file

import HexFile
import com.casadetasha.tools.khex.ui.hex.toHex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileContentMatcher(private val hexFileFlow: StateFlow<HexFile>) {
    private val _matchResultFlow: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val matchResultFlow: StateFlow<Pair<Int, Int>?> = _matchResultFlow
    var searchString: String = ""

    fun match(searchString: String) {
        this.searchString = searchString
        if (searchString.isEmpty()) return

        val firstCharCode = searchString.first().code
        val byteDeltaString = searchString.map { it.code - firstCharCode }
        _matchResultFlow.value = hexFileFlow.value.matchFileContent(byteDeltaString)
    }

    fun scrollToResult() {
        matchResultFlow.value?.let { hexFileFlow.value.scrollTo(it) }
    }

    fun generateTable(): String {
        val matchPair = matchResultFlow.value
        val firstSearchChar = searchString.first()
        val firstCharCode = hexFileFlow.value.getCellByteFlow(matchPair!!.first, matchPair.second).value.toInt()
        val distanceToStart = firstSearchChar.code - 'a'.code
        val tableStartInt = firstCharCode - distanceToStart

        var tableCharValue = tableStartInt

        return ('a'.code..'z'.code).joinToString("\n") {
            "${tableCharValue++.toByte().toHex()}=${it.toChar()}"
        }
    }
}
