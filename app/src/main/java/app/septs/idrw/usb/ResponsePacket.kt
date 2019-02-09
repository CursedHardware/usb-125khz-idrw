package app.septs.idrw.usb

@ExperimentalUnsignedTypes
class ResponsePacket(packet: ByteArray) {
    private val stationId: Byte
    private val status: Byte
    val payload: ByteArray

    init {
        // payload data structure
        // 0: station id (1 byte)
        // 1: response length (status + payload)
        // 2: response status (1 byte)
        // 3: response payload (1 ... n)
        val unwrapped = unwrapPacket(packet)
        this.stationId = unwrapped[0]
        val length = unwrapped[1] - 1
        this.status = unwrapped[2]
        this.payload = unwrapped.sliceArray(3 until 3 + length)
    }

    fun throwException() {
        if (status == 0x01.toByte()) {
            throw CardException(payload[0])
        }
    }

    override fun toString(): String {
        return "${toHexString(payload)} (Status: $status, Station ID: $stationId)"
    }
}