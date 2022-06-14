import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

class ThingyTable private constructor(val charMap: Map<Byte, Char>, val byteMap: Map<Char, Byte>) {

    fun mapToChar(byte: Byte): Char = when (charMap.contains(byte)) {
        true -> charMap[byte]!!
        else -> byte.toInt().toChar()
    }

    companion object {
        val emptyTable = ThingyTable(emptyMap(), emptyMap())
        val HEX_REGEX = """^(0x|xX)?[a-fA-F\d]{2}$""".toRegex()

        fun parseFromFile(file: File): ThingyTable {
            val byteMap = HashMap<Char, Byte>()
            val charMap = HashMap<Byte, Char>()
            file.readLines(UTF_8)
                .filterNot { it.isBlank() }
                .map {
                    val splitIndex = it.indexOf("=")
                    check(splitIndex > 0) { "Invalid tbl file, all non blank lines must text separated by a \"=\" symbol" }
                    it.substring(0, splitIndex).trim() to it.substring(splitIndex + 1, it.length).trim()
                }
                .forEach {
                    checkValidHex(it.first)
                    checkValidChar(it.second)
                    val byteValue = it.first.toInt(16).toByte()
                    val charValue = it.second.first()

                    check(!byteMap.containsKey(charValue)) { "Table contents must be unique. Duplicate found: ${byteValue.toHex()} = $charValue" }
                    check(!charMap.containsKey(byteValue)) { "Table contents must be unique. Duplicate found: ${byteValue.toHex()} = $charValue" }

                    byteMap[charValue] = byteValue
                    charMap[byteValue] = charValue
                }

            return ThingyTable(charMap, byteMap)
        }

        private fun checkValidHex(hexValue: String) {
            check(hexValue.matches(HEX_REGEX)) { "Invalid hex value: $hexValue" }
        }

        private fun checkValidChar(charValue: String) {
            check(charValue.length == 1) { "Invalid char value: $charValue" }
        }
    }
}