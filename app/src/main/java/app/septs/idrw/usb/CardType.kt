package app.septs.idrw.usb

enum class CardType(val code: Byte) {
    T5577(0x00),
    EM4305(0x02);

    fun getLockCode(lock: Boolean): Byte = when (this) {
        T5577 -> if (lock) 6 else 4
        EM4305 -> if (lock) 7 else 5
    }
}