package app.septs.idrw.usb

import java.util.*



object Packet {
    private val HEADER = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00)
    private const val STX: Byte = 0x02
    private const val ETX: Byte = 0x03

    fun wrap(packet: ByteArray, command: Command): ByteArray {
        val request = HEADER + STX + packet + ETX
        if (command == Command.MF_WRITE) request[6] = 0x1F
        return request
    }

    fun unwrap(packet: ByteArray) = packet
        .sliceArray(packet.indexOf(STX) + 1 until packet.lastIndexOf(ETX))

    @ExperimentalUnsignedTypes
    fun toHexString(packet: ByteArray) = packet
        .joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
        .toUpperCase(Locale.ROOT)
}
