package app.septs.idrw.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import app.septs.idrw.BuildConfig
import app.septs.idrw.usb.Constants.*
import app.septs.idrw.usb.RequestPacket.Companion.makeControlBuzzer
import app.septs.idrw.usb.RequestPacket.Companion.makeGetSNR
import app.septs.idrw.usb.RequestPacket.Companion.makeLock
import app.septs.idrw.usb.RequestPacket.Companion.makeWrite
import java.io.Closeable


@ExperimentalUnsignedTypes
class USBBackend(
    private val connection: UsbDeviceConnection,
    private val face: UsbInterface
) : Closeable {
    companion object {
        private const val TAG = "USBBackend"

        fun isSupported(device: UsbDevice): Boolean {
            return ((device.vendorId == 0xFFFF && device.productId == 0x0035) ||
                    (device.vendorId == 0x6688 && device.productId == 0x6850))
        }

        fun connect(mUsbManager: UsbManager, device: UsbDevice) = USBBackend(
            mUsbManager.openDevice(device),
            (0 until device.interfaceCount).map(device::getInterface).first()
        )
    }

    init {
        connection.claimInterface(face, false)
        transfer(CONTROL_INIT, REQUEST_GET_DESCRIPTOR, DT_REPORT shl 8)
    }

    fun readCard() = IDCard(sendPacket(makeGetSNR()).payload)

    fun writeCard(type: CardType, card: IDCard, lock: Boolean) {
        sendPacket(makeLock(type, false))
        sendPacket(makeWrite(type, card))
        sendPacket(makeLock(type, lock))
        sendPacket(makeControlBuzzer())
    }

    private fun sendPacket(request: RequestPacket): ResponsePacket {
        transfer(CONTROL_OUT, HID_SET_REPORT, SEND_PACKET_CONTROL, request.toPacket())
        return ResponsePacket(transfer(CONTROL_IN, HID_GET_REPORT, SEND_PACKET_CONTROL))
    }

    private fun transfer(type: Int, request: Int, value: Int, packet: ByteArray? = null): ByteArray {
        val payload = packet?.copyOf(0x100) ?: ByteArray(0x100)
        connection.controlTransfer(type, request, value, 0, payload, payload.size, TIMEOUT)
        if (BuildConfig.DEBUG) {
            when (type) {
                CONTROL_OUT -> Log.d(TAG, ">>> ${toHexString(payload)}")
                CONTROL_IN -> Log.d(TAG, "<<< ${toHexString(payload)}")
            }
        }
        return payload
    }

    override fun close() {
        connection.releaseInterface(face)
        connection.close()
    }
}
