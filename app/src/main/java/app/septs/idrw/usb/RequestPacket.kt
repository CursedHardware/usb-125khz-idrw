package app.septs.idrw.usb

import kotlin.experimental.xor

class RequestPacket {
    companion object {
        private const val DEFAULT_STATION_ID = 0x00.toByte()

        fun makeGetSNR(): RequestPacket {
            return RequestPacket(
                    0x25.toByte(),
                    arrayOf(0x00, 0x00)
            )
        }

        fun makeControlBuzzer(cycle: Int, count: Int): RequestPacket {
            return RequestPacket(
                    0x89.toByte(),
                    arrayOf(cycle.toByte(), count.toByte())
            )
        }

        fun makeWrite(tagType: Byte, payload: ByteArray): RequestPacket {
            return RequestPacket(
                    0x21.toByte(),
                    arrayOf(
                            0x00, // Write mode control
                            0x01, // Length
                            0x01, // Start address
                            tagType,
                            payload[0], // EM4100 tag number
                            payload[1], // EM4100 tag number
                            payload[2], // EM4100 tag number
                            payload[3], // EM4100 tag number
                            payload[4], // EM4100 tag number
                            0x80.toByte() // unknown
                    )
            )
        }

        fun build(packet: RequestPacket): ByteArray {
            return wrapHeader(wrapPacket(packet.toPacket()), packet.command)
        }

        private fun wrapHeader(packet: ByteArray, command: Byte): ByteArray {
            val header = arrayOf<Byte>(
                    0x01, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x08, 0x00
            )
            if (command == 0x21.toByte()) { // MF_WRITE
                header[6] = 0x1F
            }
            return header.toByteArray() + packet
        }

        private fun wrapPacket(packet: ByteArray): ByteArray {
            val startTX = arrayOf<Byte>(0x02).toByteArray()
            val endTX = arrayOf<Byte>(0x03).toByteArray()
            return (startTX + packet + endTX)
        }

        private fun getCRC8(buffer: ByteArray): Byte {
            return buffer.reduce { checksum, it -> checksum xor it }
        }
    }

    private val stationId: Byte
    private val command: Byte
    private val payload: Array<Byte>

    constructor(stationId: Byte, command: Byte, payload: Array<Byte>) {
        this.stationId = stationId
        this.command = command
        this.payload = payload
    }

    constructor(command: Byte, payload: Array<Byte>) : this(DEFAULT_STATION_ID, command, payload)

    fun toPacket(): ByteArray {
        var buffer = emptyArray<Byte>()
        buffer += stationId
        buffer += (payload.size + 1).toByte()
        buffer += command
        buffer += payload
        buffer += getCRC8(buffer.toByteArray())
        return buffer.toByteArray()
    }
}
