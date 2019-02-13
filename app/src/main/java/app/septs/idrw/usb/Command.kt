package app.septs.idrw.usb

enum class Command(code: Int) {
    MF_WRITE(0x21),
    MF_GET_SNR(0x25),
    CONTROL_BUZZER(0x89);

    val code: Byte = code.toByte()
}