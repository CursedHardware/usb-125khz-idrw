package app.septs.idrw.usb

import java.nio.ByteBuffer

@ExperimentalUnsignedTypes
data class IDCard(var card: ByteArray = ByteArray(0x05)) {
    var customerId: UByte
        get() = card[0].toUByte()
        set(value) {
            card[0] = value.toByte()
        }

    var userId: UInt
        get() = ByteBuffer.wrap(card).getInt(1).toUInt()
        set(value) {
            ByteBuffer.wrap(card).apply {
                putInt(1, value.toInt())
                card = array()
            }
        }

    override fun toString(): String {
        val cid = "$customerId".padStart(3, '0')
        val uid = "$userId".padStart(10, '0')
        return "CID: $cid, UID: $uid"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IDCard

        if (!card.contentEquals(other.card)) return false

        return true
    }

    override fun hashCode(): Int {
        return card.contentHashCode()
    }
}