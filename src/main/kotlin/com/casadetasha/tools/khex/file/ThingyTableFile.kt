package com.casadetasha.tools.khex.file

import com.casadetasha.tools.khex.ui.hex.toHex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

class ThingyTableFile private constructor(private val charMap: Map<Byte, Char>, private val byteMap: Map<Char, Byte>) {

    private val _hexMapListFlow: MutableStateFlow<List<Pair<String, Char>>> = MutableStateFlow(
        charMap.toSortedMap()
            .mapKeys { it.key.toHex() }
            .toList()
    )

    val hexMapListFlow: StateFlow<List<Pair<String, Char>>> = _hexMapListFlow

    fun mapToChar(byte: Byte): Char = when (charMap.contains(byte)) {
        true -> charMap[byte]!!
        else -> byte.toInt().toChar()
    }

    fun mapToByte(char: Char): Byte = when (byteMap.contains(char)) {
        true -> byteMap[char]!!
        else -> char.code.toByte()
    }

    companion object {

        private val HEX_REGEX = """^(0x|xX)?[a-fA-F\d]{2}$""".toRegex()

        val emptyTable = ThingyTableFile(emptyMap(), emptyMap())

        fun parseFromFile(file: File): ThingyTableFile {
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

            return ThingyTableFile(charMap, byteMap)
        }

        private fun checkValidHex(hexValue: String) {
            check(hexValue.matches(HEX_REGEX)) { "Invalid hex value: $hexValue" }
        }

        private fun checkValidChar(charValue: String) {
            check(charValue.length == 1) { "Invalid char value: $charValue" }
        }
    }
}
