import androidx.compose.runtime.rememberCoroutineScope
import com.casadetasha.tools.khex.file.ThingyTableFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class HexFile private constructor(
    private val byteArray: ByteArray,
    private val thingyTableFileFlow: MutableStateFlow<ThingyTableFile>
) {

    var thingyTableFile: ThingyTableFile = thingyTableFileFlow.value

    val lineIndexes: List<HexRowIndexes>

    init {
        val list: MutableList<HexRowIndexes> = ArrayList()
        val fullRowCount: Int = byteArray.size / 16
        val overflowRowIndex: Int = fullRowCount // the count is always index + 1, so this is fullRowFinalIndex + 1
        (0 until fullRowCount).forEach { index ->
            list.add(HexRowIndexes(rowIndex = index, columnCount = 16))
        }
        when (val overflow = byteArray.size % 16) {
            0 -> {}
            else -> list.add(HexRowIndexes(rowIndex = overflowRowIndex, columnCount = overflow))
        }
        lineIndexes = list

        CoroutineScope(GlobalScope.coroutineContext).launch {
            thingyTableFileFlow.collect {
                thingyTableFile = it
            }
        }
    }

    private val cellByteFlows: MutableMap<String, MutableStateFlow<Byte>> = HashMap()
    private val cellByteFlowReferenceCount: MutableMap<String, Int> = HashMap()

    private val _searchResultFlow: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val searchResultFlow: StateFlow<Pair<Int, Int>?> = _searchResultFlow

    fun search(searchString: String) {
        val searchBytes = searchString.map { thingyTableFile.mapToByte(it) }
        val firstByte = searchBytes.firstOrNull() ?: return clearSearch()

        byteSearch@ for (indexedByte in byteArray.withIndex()) {
            val byte = indexedByte.value
            val index = indexedByte.index

            val lastArrayIndex = byteArray.size - 1
            if (index + searchString.length > lastArrayIndex) break@byteSearch

            if (byte == firstByte) {
                for (byteStringIndex in (1 until searchString.length)) {
                    val bytesAreNotEqual = byteArray[index + byteStringIndex] != searchBytes[byteStringIndex]
                    if (bytesAreNotEqual) continue@byteSearch
                }

                val rowIndex = index / 16
                val columnIndex = index % 16
                _searchResultFlow.value = Pair(rowIndex, columnIndex)
                return
            }
        }
        _searchResultFlow.value = null
    }

    private fun clearSearch() {
        _searchResultFlow.value = null
    }

    @Synchronized fun updateWithTableConversion(charValue: Char, rowIndex: Int, columnIndex: Int) {
        val byteValue = thingyTableFile.mapToByte(charValue)
        val index = rowIndex * 16 + columnIndex
        val cellByteFlow = cellByteFlows[Pair(rowIndex, columnIndex).toKey()]

        byteArray[index] = byteValue
        if (cellByteFlow != null) cellByteFlow.value = byteValue
    }

    @Synchronized fun getCellByteFlow(rowIndex: Int, columnIndex: Int): StateFlow<Byte> {
        val key = Pair(rowIndex, columnIndex).toKey()
        when (val referenceCount = cellByteFlowReferenceCount[key]) {
            null -> cellByteFlowReferenceCount[key] = 1
            else -> cellByteFlowReferenceCount[key] = referenceCount + 1
        }
        return when (val cachedValue = cellByteFlows[key]) {
            null -> {
                val value = byteArray[rowIndex * 16 + columnIndex]
                cellByteFlows.insertAndReturn(key, value)
            }
            else -> cachedValue
        }
    }

    @Synchronized fun getPreviewCellByteFlow(rowIndex: Int, columnIndex: Int): StateFlow<Char> {
        val cellByteFlow = getCellByteFlow(rowIndex, columnIndex);
        return thingyTableFileFlow
            .combine(cellByteFlow) { file, byte -> file.mapToChar(byte) }
            .stateIn(GlobalScope, SharingStarted.Eagerly, thingyTableFile.mapToChar(cellByteFlow.value))
    }

    @Synchronized fun cleanupCellByteFlow(rowIndex: Int, columnIndex: Int) {
        val key = Pair(rowIndex, columnIndex).toKey()
        when (val previousReferenceCount = checkNotNull(cellByteFlowReferenceCount[key])) {
            1 -> {
                cellByteFlowReferenceCount.remove(key)
                cellByteFlows.remove(key)
            }
            else -> cellByteFlowReferenceCount[key] = previousReferenceCount - 1
        }
    }

    fun matchFileContent(byteDeltaString: List<Int>): List<Pair<Int, Int>> {
        val list: MutableList<Pair<Int, Int>> = mutableListOf()
        rootMatchLoop@ for (indexedByte in byteArray.withIndex()) {
            val index = indexedByte.index
            val byteValue = indexedByte.value.toInt()

            if (index + byteDeltaString.size > byteArray.size) break

            for (charIndex in byteDeltaString.withIndex()) {
                val subIndex = index + charIndex.index
                val possibleCharValue = byteValue + charIndex.value
                if (byteArray[subIndex].toInt() != possibleCharValue) continue@rootMatchLoop
            }
            list += Pair(index / 16, index % 16)
        }
        return list
    }

    fun scrollTo(scrollPosition: Pair<Int, Int>) {
        _searchResultFlow.value = scrollPosition
    }

    private fun MutableMap<String, MutableStateFlow<Byte>>.insertAndReturn(key: String, value: Byte): StateFlow<Byte> {
        this[key] = MutableStateFlow(value)
        return this[key]!!
    }

    fun updateTable(table: ThingyTableFile) {
        thingyTableFileFlow.value = table
    }

    companion object {
        fun parseFile(file: File?, thingyTableFileFlow: MutableStateFlow<ThingyTableFile>): HexFile {
            return HexFile(file?.readBytes() ?: TEST_ROW, thingyTableFileFlow)
        }

        private val TEST_ROW: ByteArray = byteArrayOf(
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
}

class HexRowIndexes constructor(val rowIndex: Int, val columnCount: Int)

fun Pair<Int, Int>.toKey(): String = "${first}/$second"

fun String.toPair(): Pair<Int, Int> = this.split("/").map {
    it.first().toString().toInt() to it.last().toString().toInt()
}.first()
