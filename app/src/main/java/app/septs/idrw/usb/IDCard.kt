package app.septs.idrw.usb

import java.nio.ByteBuffer

@ExperimentalUnsignedTypes
data class IDCard(var payload: ByteArray = ByteArray(0x05)) {
    var customerId: UByte
        get() = payload[0].toUByte()
        set(value) {
            payload[0] = value.toByte()
        }

    var userId: UInt
        get() = ByteBuffer.wrap(payload).getInt(1).toUInt()
        set(value) {
            ByteBuffer.wrap(payload).apply {
                putInt(1, value.toInt())
                payload = array()
            }
        }

    override fun toString(): String {
        return "CID: $customerId, WG34: $userId"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IDCard

        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        return payload.contentHashCode()
    }
}