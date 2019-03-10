package app.septs.idrw.usb

@ExperimentalUnsignedTypes
class ResponsePacket(packet: ByteArray) {
    private val stationId: Byte
    private val status: Byte
    val payload: ByteArray

    init {
        if (packet.all { it == 0.toByte() }) {
            throw CardException(0x82.toByte())
        }
        unwrapPacket(packet).also {
            stationId = it[0] // station id
            val length = it[1] - 1 // response length (status + payload)
            status = it[2] // response status (1 byte)
            payload = it.sliceArray(3 until 3 + length) // response payload
        }
        if (status == 0x01.toByte()) {
            throw CardException(payload[0])
        }
    }
}