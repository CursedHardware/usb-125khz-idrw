package app.septs.idrw.usb

import kotlin.experimental.xor

@ExperimentalUnsignedTypes
class RequestPacket(
    private val command: Command,
    private val payload: ByteArray,
    private val stationId: Byte = 0x00
) {
    companion object {
        fun makeGetSNR() =
            RequestPacket(Command.MF_GET_SNR, byteArrayOf(0x00, 0x00))

        fun makeControlBuzzer(cycle: Byte = 1, count: Byte = 1) =
            RequestPacket(Command.CONTROL_BUZZER, byteArrayOf(cycle, count))

        fun makeLock(type: CardType, lock: Boolean) =
            makeControlBuzzer(type.getLockCode(lock))

        fun makeWrite(type: CardType, card: IDCard) =
            RequestPacket(Command.MF_WRITE, let {
                var packet = byteArrayOf()
                packet += 0x00 // MF_WRITE mode control
                packet += 0x01 // Length
                packet += 0x01 // Start address
                packet += type.code
                packet += card.toPacket()
                return@let packet
            })
    }

    fun toPacket(): ByteArray {
        var packet = byteArrayOf()
        packet += stationId
        packet += (payload.size + 1).toByte() // length
        packet += command.code
        packet += payload
        packet += packet.reduce(Byte::xor) // checksum (crc8)
        return Packet.wrap(packet, command)
    }
}
