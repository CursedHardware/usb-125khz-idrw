package app.septs.idrw.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import java.io.Closeable


@ExperimentalUnsignedTypes
class USBBackend(
        private val connection: UsbDeviceConnection,
        private val iface: UsbInterface
) : Closeable {
    companion object {
        private const val TAG = "USBBackend"

        private const val TIMEOUT = 5000

        private const val REQUEST_TYPE_STANDARD = 0x00 shl 5
        private const val REQUEST_GET_DESCRIPTOR = 0x06
        private const val DT_REPORT = 0x22

        private const val HID_GET_REPORT = 0x01
        private const val HID_SET_REPORT = 0x09
        private const val HID_REPORT_TYPE_FEATURE = 0x03

        private const val ENDPOINT_IN = 0x80
        private const val ENDPOINT_OUT = 0x00

        private const val REQUEST_TYPE_CLASS = 0x01 shl 5
        private const val RECIPIENT_INTERFACE = 0x01
        private const val CONTROL_IN = ENDPOINT_IN or REQUEST_TYPE_CLASS or RECIPIENT_INTERFACE
        private const val CONTROL_OUT = ENDPOINT_OUT or REQUEST_TYPE_CLASS or RECIPIENT_INTERFACE

        fun isSupported(device: UsbDevice): Boolean {
            return ((device.vendorId == 0xFFFF && device.productId == 0x0035) ||
                    (device.vendorId == 0x6688 && device.productId == 0x6850))
        }

        fun connect(usbManager: UsbManager, device: UsbDevice): USBBackend {
            val iface = (0 until device.interfaceCount)
                    .map { device.getInterface(it) }
                    .first()
            return USBBackend(usbManager.openDevice(device), iface)
        }
    }

    init {
        connection.claimInterface(iface, false)
        initControl()
    }

    private fun initControl() {
        val buffer = ByteArray(1)
        val requestType = ENDPOINT_IN or REQUEST_TYPE_STANDARD or RECIPIENT_INTERFACE
        val request = REQUEST_GET_DESCRIPTOR
        val value = DT_REPORT shl 8
        connection.controlTransfer(requestType, request, value, 0, buffer, buffer.size, TIMEOUT)
    }

    fun readCard(): IDCard {
        val response = sendPacket(RequestPacket.makeGetSNR())
        return IDCard(response.payload)
    }

    fun writeCard(type: CardType, card: IDCard, lock: Boolean = false) {
        val tagType: Byte = if (type == CardType.T5577) 0x00 else 0x02
        protect(tagType, false)
        sendPacket(RequestPacket.makeWrite(tagType, card.payload))
        if (lock) {
            protect(tagType, lock)
        }
        // send reset packet
        sendPacket(RequestPacket.makeControlBuzzer(1, 1))
    }

    private fun protect(tagType: Byte, lock: Boolean) {
        when (tagType) {
            0x00.toByte() -> { // T5577
                val cycle = if (lock) 6 else 4 // Protected or Unprotected
                sendPacket(RequestPacket.makeControlBuzzer(cycle, 1))
            }
            0x02.toByte() -> { // EM4305
                val cycle = if (lock) 7 else 5 // Protected or Unprotected
                sendPacket(RequestPacket.makeControlBuzzer(cycle, 1))
            }
        }
    }

    private fun sendPacket(packet: RequestPacket): ResponsePacket {
        val controlValue = (HID_REPORT_TYPE_FEATURE shl 8) or 0x00
        val bufOut = RequestPacket.build(packet).copyOf(0x100)
        connection.controlTransfer(
                CONTROL_OUT, HID_SET_REPORT,
                controlValue,
                0,
                bufOut, bufOut.size,
                TIMEOUT
        )

        val bufIn = ByteArray(0x100)
        connection.controlTransfer(
                CONTROL_IN, HID_GET_REPORT,
                controlValue,
                0,
                bufIn, bufIn.size,
                TIMEOUT
        )

        Log.d(TAG, ">>> ${toHexString(bufOut)}")
        Log.d(TAG, "<<< ${toHexString(bufIn)}")
        return ResponsePacket.fromRawData(bufIn)
    }

    override fun close() {
        connection.releaseInterface(iface)
        connection.close()
    }

    private fun toHexString(buffer: ByteArray): String {
        return buffer
                .joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
                .toUpperCase()
                .trimEnd('0')
    }
}
