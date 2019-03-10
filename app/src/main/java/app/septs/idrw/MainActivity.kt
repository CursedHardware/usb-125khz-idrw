package app.septs.idrw


import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import app.septs.idrw.databinding.ActivityMainBinding
import app.septs.idrw.tools.InputFilterValueRange
import app.septs.idrw.tools.Keyboard
import app.septs.idrw.usb.CardException
import app.septs.idrw.usb.CardType
import app.septs.idrw.usb.IDCard
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager


@ExperimentalUnsignedTypes
class MainActivity : USBActivity() {
    companion object {
        private const val CARD_VM = "CARD_VM"
    }

    private var mCard = CardViewModel()
    private lateinit var mBinding: ActivityMainBinding

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

        checkForUpdates()
    }

    private fun onReadCard() {
        try {
            mCard.idCard = mBackend?.readCard() ?: return

            Toast.makeText(this, getString(R.string.toast_read_from_card, mCard.idCard), Toast.LENGTH_LONG).show()
        } catch (e: CardException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onWriteCard() {
        try {
            mBackend?.writeCard(mCard.type, mCard.idCard, mCard.lock)

            Thread.sleep(200)

            val message = if (mCard.idCard == mBackend?.readCard()) {
                when {
                    mCard.autoIncrement -> mCard.idCard += 1u
                    mCard.autoDecrement -> mCard.idCard -= 1u
                }
                mCard.notifyChange()
                getString(R.string.toast_write_card_success, mCard.idCard)
            } else {
                getString(R.string.toast_write_card_failed)
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: CardException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_clear -> {
                mCard.idCard = IDCard()
            }
            R.id.menu_change_orientation -> {
                val isEnabled = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
                if (isEnabled == 0) {
                    Toast.makeText(this, R.string.toast_please_enable_auto_rotate, Toast.LENGTH_LONG).show()
                    return false
                }
                requestedOrientation = if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT else
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            R.id.menu_homepage -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(getString(R.string.project_link))
                })
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        val editor = getSharedPreferences(CARD_VM, Context.MODE_PRIVATE).edit()
        editor.putString("TYPE", mCard.type.name)
        editor.putBoolean("LOCK", mCard.lock)
        editor.putBoolean("AUTO_INCREMENT", mCard.autoIncrement)
        editor.putBoolean("AUTO_DECREMENT", mCard.autoDecrement)
        editor.putInt("CUSTOMER_ID", mCard.idCard.customerId.toInt())
        editor.putInt("USER_ID", mCard.idCard.wiegand34.toInt())
        editor.apply()

        unregisterManagers()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(CARD_VM, Context.MODE_PRIVATE)
        mCard.type = CardType.valueOf(prefs.getString("TYPE", CardType.T5577.name)!!)
        mCard.lock = prefs.getBoolean("LOCK", false)
        mCard.autoIncrement = prefs.getBoolean("AUTO_INCREMENT", false)
        mCard.autoDecrement = prefs.getBoolean("AUTO_DECREMENT", false)
        mCard.idCard.customerId = prefs.getInt("CUSTOMER_ID", 0u.toInt()).toUByte()
        mCard.idCard.wiegand34 = prefs.getInt("USER_ID", 0u.toInt()).toUInt()

        checkForCrashes()
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterManagers()
    }

    override fun setConnected(connected: Boolean) {
        if (!connected) {
            Keyboard.hidden(this)
        }
        mCard.connected = connected
    }

    private fun checkForCrashes() {
        if (!BuildConfig.DEBUG) {
            CrashManager.register(this)
        }
    }

    private fun checkForUpdates() {
        if (!BuildConfig.DEBUG) {
            UpdateManager.register(this)
        }
    }

    private fun unregisterManagers() {
        if (!BuildConfig.DEBUG) {
            UpdateManager.unregister()
        }
    }
}
