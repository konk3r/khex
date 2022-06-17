package com.casadetasha.tools.khex.file

import HexFile
import com.casadetasha.tools.khex.ui.hex.toHex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileContentMatcher(private val hexFileFlow: StateFlow<HexFile>) {
    private val _matchResultFlow: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(listOf())
    val matchResultFlow: StateFlow<List<Pair<Int, Int>>> = _matchResultFlow
    var searchString: String = ""

    fun match(searchString: String) {
        this.searchString = searchString
        if (searchString.isEmpty()) return

        val firstCharCode = searchString.first().code
        val byteDeltaString = searchString.map { it.code - firstCharCode }
        _matchResultFlow.value = hexFileFlow.value.matchFileContent(byteDeltaString)
    }

    fun scrollToResult(result: Pair<Int, Int>) {
        hexFileFlow.value.scrollTo(result)
    }

    fun generateTable(result: Pair<Int, Int>): String {
        val firstSearchChar = searchString.first()
        val firstCharCode = hexFileFlow.value.getCellByteFlow(result.first, result.second).value.toInt()
        val distanceToStart = firstSearchChar.code - 'a'.code
        val tableStartInt = firstCharCode - distanceToStart

        var tableCharValue = tableStartInt

        return ('a'.code..'z'.code).joinToString("\n") {
            "${tableCharValue++.toByte().toHex()}=${it.toChar()}"
        }
    }

    fun applyTable(result: Pair<Int, Int>) {
        val table = generateTable(result)

        hexFileFlow.value.updateTable(ThingyTableFile.parseFromString(table))
    }
}
