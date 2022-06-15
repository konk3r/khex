package com.casadetasha.tools.khex.file

import HexFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileContentMatcher(private val hexFileFlow: StateFlow<HexFile>) {
    private val _matchResultFlow: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val matchResultFlow: StateFlow<Pair<Int, Int>?> = _matchResultFlow

    fun match(searchString: String) {
        if (searchString.isEmpty()) return

        val firstCharCode = searchString.first().code
        val byteDeltaString = searchString.map { it.code - firstCharCode }
        _matchResultFlow.value = hexFileFlow.value.matchFileContent(byteDeltaString)
    }

    fun scrollToResult() {
        matchResultFlow.value?.let { hexFileFlow.value.scrollTo(it) }
    }
}
