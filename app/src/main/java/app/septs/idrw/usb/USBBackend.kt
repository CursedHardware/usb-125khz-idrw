package app.septs.idrw.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import app.septs.idrw.usb.RequestPacket.Companion.makeControlBuzzer
import app.septs.idrw.usb.RequestPacket.Companion.makeGetSNR
import app.septs.idrw.usb.RequestPacket.Companion.makeProtect
import app.septs.idrw.usb.RequestPacket.Companion.makeWrite
import java.io.Closeable


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

private const val SEND_PACKET_CONTROL = (HID_REPORT_TYPE_FEATURE shl 8) or 0x00


@ExperimentalUnsignedTypes
class USBBackend(private val conn: UsbDeviceConnection, private val face: UsbInterface) : Closeable {
    init {
        conn.claimInterface(face, false)
        transfer(
                ENDPOINT_IN or REQUEST_TYPE_STANDARD or RECIPIENT_INTERFACE,
                REQUEST_GET_DESCRIPTOR,
                DT_REPORT shl 8
        )
    }

    fun readCard() = IDCard(sendPacket(makeGetSNR()).payload)

    fun writeCard(type: CardType, card: IDCard, lock: Boolean) {
        sendPacket(makeProtect(type, false))
        sendPacket(makeWrite(type, card))
        sendPacket(makeProtect(type, lock))
        sendPacket(makeControlBuzzer())
    }

    private fun sendPacket(request: RequestPacket): ResponsePacket {
        transfer(
                CONTROL_OUT, HID_SET_REPORT, SEND_PACKET_CONTROL, request.toPacket()
        )
        Log.d("SEND_PACKET", "<<< $request")

        val response = ResponsePacket(transfer(
                CONTROL_IN, HID_GET_REPORT, SEND_PACKET_CONTROL
        ))
        Log.d("SEND_PACKET", ">>> $response")

        response.throwException()

        return response
    }

    private fun transfer(
            requestType: Int,
            request: Int,
            controlValue: Int,
            packet: ByteArray = ByteArray(0x100)
    ) = packet.copyOf(0x100).let {
        conn.controlTransfer(
                requestType, request,
                controlValue,
                0,
                it, it.size,
                TIMEOUT
        )
        return@let it
    }

    override fun close() {
        conn.releaseInterface(face)
        conn.close()
    }

    companion object {
        fun isSupported(device: UsbDevice): Boolean {
            return ((device.vendorId == 0xFFFF && device.productId == 0x0035) ||
                    (device.vendorId == 0x6688 && device.productId == 0x6850))
        }

        fun connect(mUsbManager: UsbManager, device: UsbDevice) = USBBackend(
                mUsbManager.openDevice(device),
                (0 until device.interfaceCount).map(device::getInterface).first()
        )
    }
}
