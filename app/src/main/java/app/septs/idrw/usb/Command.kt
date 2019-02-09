package app.septs.idrw.usb

enum class Command(val value: Byte) {
    MF_WRITE(0x21.toByte()),
    GET_SNR(0x25.toByte()),
    CONTROL_BUZZER(0x89.toByte())
}