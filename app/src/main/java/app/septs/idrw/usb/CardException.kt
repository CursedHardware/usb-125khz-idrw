package app.septs.idrw.usb

class CardException(override var message: String) : Exception(message) {
    companion object {
        private fun getCodeMessage(code: Byte): String {
            return when (code) {
                0x80.toByte() -> "Set successfully"
                0x81.toByte() -> "Set failed"
                0x82.toByte() -> "Communication timeout"
                0x83.toByte() -> "Card not found"
                0x84.toByte() -> "Card data error"
                0x87.toByte() -> "Unknown error"
                0x85.toByte() -> "Command format error"
                0x8F.toByte() -> "Command not found"
                else -> "Not defined error"
            }
        }
    }

    constructor(code: Byte) : this(getCodeMessage(code))
}