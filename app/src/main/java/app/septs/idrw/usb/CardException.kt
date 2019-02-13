package app.septs.idrw.usb

class CardException(private val code: Byte) : Exception() {
    override val message: String?
        get() = when (code.toInt()) {
            0x80 -> "Set successfully"
            0x81 -> "Set failed"
            0x82 -> "Communication timeout"
            0x83 -> "Card not found"
            0x84 -> "Card data error"
            0x87 -> "Unknown error"
            0x85 -> "Command format error"
            0x8F -> "Command not found"
            else -> "Not defined error"
        }
}