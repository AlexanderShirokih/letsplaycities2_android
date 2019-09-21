package ru.aleshi.letsplaycities.remote.internal

import java.io.Reader

class JsonMessageReader(private val reader: Reader) {

    fun read(): CharArray {
        val buffers = mutableListOf<Pair<CharArray, Int>>()

        var openedBrackets = 0

        do {
            val buffer = CharArray(1024)
            val readLimit = reader.read(buffer)

            if (readLimit == -1)
                return charArrayOf('{', '}')

            openedBrackets += countBrackets(buffer, readLimit)
            buffers.add(buffer to readLimit)
        } while (openedBrackets > 0)

        return concatBuffers(buffers)
    }

    private fun concatBuffers(buffers: List<Pair<CharArray, Int>>): CharArray {
        val size = buffers.sumBy { it.second }
        var currentBuf = 0
        var localCnt = 0
        return CharArray(size) {
            val res = buffers[currentBuf].first[localCnt]
            localCnt++
            if (localCnt == buffers[currentBuf].second) {
                currentBuf++
                localCnt = 0
            }
            res
        }
    }

    private fun countBrackets(buffer: CharArray, readLimit: Int): Int {
        return buffer
            .take(readLimit)
            .fold(0) { acc: Int, c: Char ->
                when (c) {
                    '{' -> acc + 1
                    '}' -> acc - 1
                    else -> acc
                }
            }
    }
}