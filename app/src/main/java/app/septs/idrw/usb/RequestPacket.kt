package app.septs.idrw.usb

import kotlin.experimental.xor

@ExperimentalUnsignedTypes
class RequestPacket(
        private val command: Command,
        private val payload: Array<Byte>,
        private val stationId: Byte = 0x00
) {
    companion object {
        fun makeGetSNR() =
                RequestPacket(Command.GET_SNR, arrayOf(0x00, 0x00))

        fun makeControlBuzzer(cycle: Byte = 1, count: Byte = 1) =
                RequestPacket(Command.CONTROL_BUZZER, arrayOf(cycle, count))

        fun makeProtect(type: CardType, lock: Boolean) = when (type) {
            CardType.T5577 -> makeControlBuzzer(if (lock) 6 else 4)
            CardType.EM4305 -> makeControlBuzzer(if (lock) 7 else 5)
        }

        fun makeWrite(type: CardType, card: IDCard): RequestPacket {
            val payload = ArrayList<Byte>().apply {
                add(0x00) // MF_WRITE mode control
                add(0x01) // Length
                add(0x01) // Start address
                add(type.code)
                addAll(card.card.toTypedArray())
                add(0x80.toByte())
            }
            return RequestPacket(Command.MF_WRITE, payload.toTypedArray())
        }
    }

    fun toPacket(): ByteArray {
        val packet = ArrayList<Byte>().apply {
            add(stationId)
            add((payload.size + 1).toByte())
            add(command.value)
            addAll(payload)
            add(reduce(Byte::xor)) // CRC8
        }
        return wrapPacket(packet.toByteArray(), command)
    }

    override fun toString(): String {
        return "${toHexString(payload)} (Command: $command, Station ID: $stationId)"
    }
}
