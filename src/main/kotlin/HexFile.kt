import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import java.io.File

class HexFile private constructor(
    private val byteArray: ByteArray,
    private val thingyTableFlow: MutableStateFlow<ThingyTable>
) {

    val thingyTable: ThingyTable get() = thingyTableFlow.value

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
    }

    private val cellByteFlows: MutableMap<String, MutableStateFlow<Byte>> = HashMap()
    private val cellByteFlowReferenceCount: MutableMap<String, Int> = HashMap()

    private val _searchResultFlow: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val searchResultFlow: StateFlow<Pair<Int, Int>?> = _searchResultFlow

    fun search(searchString: String) {
        val searchBytes = searchString.map { thingyTable.mapToByte(it) }
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
        val byteValue = thingyTable.mapToByte(charValue)
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
                val char = value.toInt().toChar()
                cellByteFlows.insertAndReturn(key, value)
            }
            else -> cachedValue
        }
    }

    @Synchronized fun getPreviewCellByteFlow(rowIndex: Int, columnIndex: Int): StateFlow<Char> {
        val cellByteFlow = getCellByteFlow(rowIndex, columnIndex);
        return cellByteFlow.map {
            thingyTable.mapToChar(it)
        }.stateIn(GlobalScope, SharingStarted.Eagerly, thingyTable.mapToChar(cellByteFlow.value))
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

    private fun MutableMap<String, MutableStateFlow<Byte>>.insertAndReturn(key: String, value: Byte): StateFlow<Byte> {
        this[key] = MutableStateFlow(value)
        return this[key]!!
    }

    companion object {
        fun parseFile(file: File?, thingyTableFlow: MutableStateFlow<ThingyTable>): HexFile {
            return HexFile(file?.readBytes() ?: TEST_ROW, thingyTableFlow)
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

fun <A, B> Pair<A, B>.toKey(): String = "${first}/$second"