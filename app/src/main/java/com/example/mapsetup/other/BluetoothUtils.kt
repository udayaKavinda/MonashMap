package com.example.mapsetup.other

import com.example.mapsetup.models.BluetoothResponse

object BluetoothUtils {
    fun bytesToInt16Array(data: ByteArray): List<Pair<Int, Int>> {
        val int16Values = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until data.size - 3 step 4) {
            val higherByteReal = data[i].toInt()
            val lowerByteReal = data[i + 1].toInt()
            val higherByteImag = data[i + 2].toInt()
            val lowerByteImag = data[i + 3].toInt()

            var valueReal = (higherByteReal shl 8) or (lowerByteReal and 0xFF)
            var valueImag = (higherByteImag shl 8) or (lowerByteImag and 0xFF)

            if (valueReal > 0x7FFF) valueReal -= 0x10000
            if (valueImag > 0x7FFF) valueImag -= 0x10000

            int16Values.add(Pair(valueReal, valueImag))
        }
        return int16Values
    }
}
