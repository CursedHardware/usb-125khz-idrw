package app.septs.idrw


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import app.septs.idrw.databinding.ActivityMainBinding
import app.septs.idrw.tools.InputFilterValueRange
import app.septs.idrw.usb.CardException
import app.septs.idrw.usb.CardType
import app.septs.idrw.usb.IDCard
import app.septs.idrw.usb.USBBackend


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
    companion object {
        private const val CARD_VM = "CARD_VM"
    }

    private var mCard = CardViewModel()
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mUSBManager: UsbManager
    private var mPermissionIntent: PendingIntent? = null
    private lateinit var mBackend: USBBackend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.apply {
            vm = mCard

            customerId.filters += InputFilterValueRange(0x00L..0xFFL)
            wiegand34.filters += InputFilterValueRange(0x00000000..0xFFFFFFFF)
            wiegand26FacilityCode.filters += InputFilterValueRange(0x00L..0xFFL)
            wiegand26IDCode.filters += InputFilterValueRange(0x0000L..0xFFFFL)
            readCard.setOnClickListener { onReadCard() }
            writeCard.setOnClickListener { onWriteCard() }
        }
        mUSBManager = getSystemService(Context.USB_SERVICE) as UsbManager
        mUSBManager.apply {
            val action = "${applicationContext.packageName}.USB_PERMISSION"
            mPermissionIntent = PendingIntent.getBroadcast(this@MainActivity, 0, Intent(action), 0)
            registerUSBReceiver()
            deviceList.values
                    .find { USBBackend.isSupported(it) }
                    .let { onConnectDevice(it) }
        }
    }

    private fun onConnectDevice(device: UsbDevice?) {
        if (device == null) {
            Toast.makeText(this, R.string.toast_device_not_found, Toast.LENGTH_LONG).show()
        } else if (!mUSBManager.hasPermission(device)) {
            mUSBManager.requestPermission(device, mPermissionIntent)
        } else {
            mCard.connected = true
            mBackend = USBBackend.connect(mUSBManager, device)
        }
    }

    private fun onReadCard() {
        try {
            val card = mBackend.readCard()
            mCard.customerId = card.customerId
            mCard.userId = card.userId

            Toast.makeText(this, getString(R.string.toast_read_from_card, card), Toast.LENGTH_LONG).show()
        } catch (e: CardException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onWriteCard() {
        val card = IDCard().apply {
            customerId = mCard.customerId
            userId = mCard.userId
        }
        try {
            mBackend.writeCard(mCard.type, card, mCard.writeProtect)

            Thread.sleep(200)

            val verified = card == mBackend.readCard()
            if (verified && mCard.autoIncrement) {
                mCard.userId += 1u
            }

            val message = if (verified)
                getString(R.string.toast_write_card_success, card) else
                getString(R.string.toast_write_card_failed)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: CardException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val mUSBReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            when (intent.action) {
                "${applicationContext.packageName}.USB_PERMISSION" -> {
                    mCard.connected = granted
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    if (!(granted && mUSBManager.hasPermission(device))) {
                        mUSBManager.requestPermission(device, mPermissionIntent)
                        return
                    }
                    mCard.connected = true
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    mCard.connected = false
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mUSBReceiver)
        val editor = getSharedPreferences(CARD_VM, Context.MODE_PRIVATE).edit()
        editor.putString("TYPE", mCard.type.name)
        editor.putBoolean("WRITE_PROTECT", mCard.writeProtect)
        editor.putBoolean("AUTO_INCREMENT", mCard.autoIncrement)
        editor.putInt("CUSTOMER_ID", mCard.customerId.toInt())
        editor.putInt("USER_ID", mCard.userId.toInt())
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        registerUSBReceiver()
        val prefs = getSharedPreferences(CARD_VM, Context.MODE_PRIVATE)
        mCard.type = CardType.valueOf(prefs.getString("TYPE", CardType.T5577.name)!!)
        mCard.writeProtect = prefs.getBoolean("WRITE_PROTECT", false)
        mCard.autoIncrement = prefs.getBoolean("AUTO_INCREMENT", false)
        mCard.customerId = prefs.getInt("CUSTOMER_ID", 0u.toInt()).toUByte()
        mCard.userId = prefs.getInt("USER_ID", 0u.toInt()).toUInt()
    }

    private fun registerUSBReceiver() {
        registerReceiver(mUSBReceiver, IntentFilter().apply {
            addAction("${applicationContext.packageName}.USB_PERMISSION")
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        })
    }
}
