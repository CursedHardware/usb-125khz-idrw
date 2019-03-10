package app.septs.idrw.usb

private val HEADER = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00)
private const val STX: Byte = 0x02
private const val ETX: Byte = 0x03

fun wrapPacket(packet: ByteArray, command: Command): ByteArray {
    var wrapped = byteArrayOf()
    wrapped += HEADER
    wrapped += STX
    wrapped += packet
    wrapped += ETX
    if (command == Command.MF_WRITE) wrapped[6] = 0x1F
    return wrapped
}

fun unwrapPacket(packet: ByteArray) = packet
    .sliceArray(packet.indexOf(STX) + 1 until packet.lastIndexOf(ETX))

@ExperimentalUnsignedTypes
fun toHexString(packet: ByteArray) = packet
    .joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
    .toUpperCase()
