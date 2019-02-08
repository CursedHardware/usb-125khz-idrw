package app.septs.idrw.usb

class ResponsePacket(val payload: ByteArray) {
    companion object {
        private fun from(packet: ByteArray): ResponsePacket {
            val dataLength = packet[1]
            val status = packet[2]
            val payload = packet
                    .slice(3 until 3 + (dataLength - 1))
                    .toByteArray()
            if (status == 0x01.toByte()) {
                throw CardException(payload[0])
            }
            return ResponsePacket(payload)
        }

        fun fromRawData(packet: ByteArray): ResponsePacket {
            val buffer = unwrapPacket(unwrapHeader(packet))
            return from(buffer)
        }

        private fun unwrapHeader(packet: ByteArray): ByteArray {
            return packet.slice(8 until packet.size).toByteArray()
        }

        private fun unwrapPacket(packet: ByteArray): ByteArray {
            val startTX = 0x02.toByte()
            val endTX = 0x03.toByte()
            val start = packet.indexOf(startTX) + 1
            val end = packet.lastIndexOf(endTX) - 1
            return packet.slice(start..end).toByteArray()
        }
    }
}