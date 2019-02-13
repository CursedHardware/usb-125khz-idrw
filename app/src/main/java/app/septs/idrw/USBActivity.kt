package app.septs.idrw

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import app.septs.idrw.usb.USBBackend

@kotlin.ExperimentalUnsignedTypes
abstract class USBActivity : AppCompatActivity() {
    private lateinit var mUSBManager: UsbManager
    private lateinit var mUSBPermission: String
    private lateinit var mUSBPermissionIntent: PendingIntent
    private lateinit var mUSBReceiverIntent: IntentFilter
    protected var mBackend: USBBackend? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUSBManager = getSystemService(Context.USB_SERVICE) as UsbManager
        mUSBPermission = "${applicationContext.packageName}.USB_PERMISSION"
        mUSBPermissionIntent = PendingIntent.getBroadcast(this, 0, Intent(mUSBPermission), 0)
        mUSBReceiverIntent = IntentFilter().apply {
            addAction(mUSBPermission)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        registerReceiver(mUSBReceiver, mUSBReceiverIntent)
        connectBackend()
    }

    private val mUSBReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            when (intent.action) {
                mUSBPermission -> {
                    setConnected(granted)
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    if (!(granted && mUSBManager.hasPermission(device))) {
                        mUSBManager.requestPermission(device, mUSBPermissionIntent)
                        return
                    }
                    setConnected(true)
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    mBackend?.close()
                    setConnected(false)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            connectBackend(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE))
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mUSBReceiver)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mUSBReceiver, mUSBReceiverIntent)
        connectBackend()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBackend?.close()
    }

    private fun connectBackend(newDevice: UsbDevice? = null) {
        mBackend?.close()
        val device = newDevice
                ?: intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                ?: mUSBManager.deviceList?.values?.find(USBBackend.Companion::isSupported)
        if (device == null) {
            setConnected(false)
            Toast.makeText(this, R.string.toast_device_not_found, Toast.LENGTH_LONG).show()
        } else if (!mUSBManager.hasPermission(device)) {
            setConnected(false)
            mUSBManager.requestPermission(device, mUSBPermissionIntent)
        } else {
            setConnected(true)
            mBackend = USBBackend.connect(mUSBManager, device)
        }
    }

    protected abstract fun setConnected(connected: Boolean)
}