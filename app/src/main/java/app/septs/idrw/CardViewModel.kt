package app.septs.idrw

import android.databinding.BaseObservable
import android.databinding.Bindable
import app.septs.idrw.usb.CardType
import java.nio.ByteBuffer


@ExperimentalUnsignedTypes
class CardViewModel : BaseObservable() {
    var connected = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.available)
        }

    val available: Boolean
        @Bindable
        get() = connected

    var type = CardType.T5577

    var lock = false

    var autoIncrement = false
        @Bindable
        get() = field
        set(value) {
            field = value
            if (autoDecrement && value) {
                autoDecrement = !value
            }
            notifyPropertyChanged(BR.autoIncrement)
            notifyPropertyChanged(BR.autoDecrement)
        }

    var autoDecrement = false
        @Bindable
        get() = field
        set(value) {
            field = value
            if (autoIncrement && value) {
                autoIncrement = !value
            }
            notifyPropertyChanged(BR.autoIncrement)
            notifyPropertyChanged(BR.autoDecrement)
        }

    var customerId: UByte = 0u
        set(value) {
            field = value
            notifyPropertyChanged(BR.customerIdAsString)
        }

    var customerIdAsString: String
        @Bindable
        get() = customerId.toString()
        set(value) {
            customerId = value.toUByteOrNull() ?: 0u
            notifyPropertyChanged(BR.customerIdAsString)
        }

    var userId: UInt = 0u
        set(value) {
            field = value
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }

    var userIdAsWG34: String
        @Bindable
        get() = userId.toString()
        set(value) {
            userId = value.toUIntOrNull() ?: 0u
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }

    var userIdAsWG26FacilityCode: String
        @Bindable
        get() = ByteBuffer.allocate(4).let {
            it.putInt(userId.toInt())
            return@let it.get(1).toUByte().toString()
        }
        set(value) {
            ByteBuffer.allocate(4).let {
                val code = value.toUByteOrNull() ?: 0u
                it.putInt(userId.toInt())
                it.put(1, code.toByte())
                userId = it.getInt(0).toUInt()

                notifyPropertyChanged(BR.userIdAsWG34)
                notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
                notifyPropertyChanged(BR.userIdAsWG26IDCode)
            }
        }

    var userIdAsWG26IDCode: String
        @Bindable
        get() = ByteBuffer.allocate(4).let {
            it.putInt(userId.toInt())
            return@let it.getShort(2).toUShort().toString()
        }
        set(value) {
            ByteBuffer.allocate(4).let {
                val code = value.toUShortOrNull() ?: 0u
                it.putInt(userId.toInt())
                it.putShort(2, code.toShort())
                userId = it.getInt(0).toUInt()

                notifyPropertyChanged(BR.userIdAsWG34)
                notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
                notifyPropertyChanged(BR.userIdAsWG26IDCode)
            }
        }
}

