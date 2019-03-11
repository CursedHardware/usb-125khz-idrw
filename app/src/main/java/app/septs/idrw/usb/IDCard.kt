package app.septs.idrw.usb

import java.nio.ByteBuffer

@ExperimentalUnsignedTypes
class IDCard(private val card: ByteBuffer = ByteBuffer.allocate(5)) {
    constructor(card: ByteArray) : this(ByteBuffer.wrap(card))

    var customerId: UByte
        get() = card.get(0).toUByte()
        set(value) {
            card.put(0, value.toByte())
        }

    var wiegand34: UInt
        get() = card.getInt(1).toUInt()
        set(value) {
            card.putInt(1, value.toInt())
        }

    var wiegand26FacilityCode: UByte
        get() = card.get(2).toUByte()
        set(value) {
            card.put(2, value.toByte())
        }

    var wiegand26IDCode: UShort
        get() = card.getShort(3).toUShort()
        set(value) {
            card.putShort(3, value.toShort())
        }

    operator fun plus(value: UInt) = IDCard(card.duplicate()).apply {
        when {
            wiegand34 == UInt.MAX_VALUE -> {
                customerId++
                wiegand34 = 1u
            }
            customerId == UByte.MAX_VALUE && wiegand34 == UInt.MAX_VALUE -> {
                customerId = UByte.MIN_VALUE
                wiegand34 = 1u
            }
            else -> {
                wiegand34 += value
            }
        }
    }

    operator fun minus(value: UInt) = IDCard(card.duplicate()).apply {
        when {
            wiegand34 == UInt.MIN_VALUE || wiegand34 == 1u -> {
                customerId--
                wiegand34 = UInt.MAX_VALUE
            }
            customerId == UByte.MIN_VALUE && wiegand34 == 1u -> {
                customerId = UByte.MAX_VALUE
                wiegand34 = UInt.MAX_VALUE
            }
            else -> {
                wiegand34 -= value
            }
        }
    }

    fun toPacket(): ByteArray = card.array()

    override fun toString() = "CID: %3s, UID: %10s".format(customerId, wiegand34)

    override fun equals(other: Any?) = other is IDCard && card == other.card

    override fun hashCode() = card.hashCode()
}