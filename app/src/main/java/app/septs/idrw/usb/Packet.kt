package app.septs.idrw.usb

private val HEADER = arrayOf<Byte>(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00)
private const val START_TX: Byte = 0x02
private const val END_TX: Byte = 0x03

fun wrapPacket(packet: ByteArray, command: Command) = ArrayList<Byte>()
        .apply {
            addAll(HEADER)
            add(START_TX)
            addAll(packet.toTypedArray())
            add(END_TX)

            if (command == Command.MF_WRITE) {
                this[6] = 0x1F
            }
        }
        .toByteArray()

fun unwrapPacket(packet: ByteArray) = packet
        .sliceArray(packet.indexOf(START_TX) + 1 until packet.lastIndexOf(END_TX))

@ExperimentalUnsignedTypes
fun toHexString(packet: ByteArray) = packet
        .joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
        .toUpperCase()

@ExperimentalUnsignedTypes
fun toHexString(packet: Array<Byte>) =
        toHexString(packet.toByteArray())
