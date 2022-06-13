import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class HexRepository private constructor(private val byteArray: ByteArray) {

    val hexRowsFlow: StateFlow<List<HexRow>>

    init {
        val list: MutableList<HexRow> = ArrayList()
        val fullRowCount: Int = byteArray.size / 16
        val overflowRowIndex: Int = fullRowCount + 1
        (0 until fullRowCount).forEach { index ->
            list.add(HexRow(rowIndex = index, columnCount = 16))
        }
        when (val overflow = byteArray.size % 16) {
            0 -> {}
            else -> list.add(HexRow(rowIndex = overflowRowIndex, columnCount = overflow))
        }
        hexRowsFlow = MutableStateFlow(list)
    }

    private val cellByteFlows: MutableMap<String, MutableStateFlow<Byte>> = HashMap()
    private val cellByteFlowReferenceCount: MutableMap<String, Int> = HashMap()

    private val _searchResultFlow: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val searchResultFlow: StateFlow<Pair<Int, Int>?> = _searchResultFlow

    fun searchForChar(search: String) {
        if (search.isEmpty()) {
            _searchResultFlow.value = null
            return
        }

        val searchChar = search.first()
        var index = 0
        return run search@ {
            byteArray.forEach {
                if (it == searchChar.code.toByte()) {
                    val rowIndex = index / 16
                    val columnIndex = index % 16
                    _searchResultFlow.value = Pair(rowIndex, columnIndex)
                    return@search
                }
                index++
            }
            _searchResultFlow.value = null
        }
    }

    fun update(byteValue: Byte, rowIndex: Int, columnIndex: Int) {
        val index = rowIndex * 16 + columnIndex
        val cellByteFlow = cellByteFlows[Pair(rowIndex, columnIndex).toKey()]

        byteArray[index] = byteValue
        if (cellByteFlow != null) cellByteFlow.value = byteValue
    }

    fun getCellByteFlow(rowIndex: Int, columnIndex: Int): StateFlow<Byte> {
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

    fun cleanupCellByteFlow(rowIndex: Int, columnIndex: Int) {
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
        fun parseFile(file: File?): HexRepository {
            return HexRepository(file?.readBytes() ?: TEST_ROW)
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

fun <A, B> Pair<A, B>.toKey(): String = "${first}/$second"
