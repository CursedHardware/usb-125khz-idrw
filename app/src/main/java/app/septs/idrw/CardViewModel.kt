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

    var reading = false
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.reading)
            notifyPropertyChanged(BR.available)
        }

    var writing = false
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.writing)
            notifyPropertyChanged(BR.available)
        }

    val available: Boolean
        @Bindable
        get() {
            if (!connected) {
                return false
            }
            return !(reading || writing)
        }

    var type = CardType.T5577
    var writeProtect = false
    var autoIncrement = false

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
        get() {
            val buffer = ByteBuffer.allocate(4)
            buffer.putInt(userId.toInt())
            return buffer.get(1).toUByte().toString()
        }
        set(value) {
            val code = value.toUByteOrNull()
            val buffer = ByteBuffer.allocate(4)
            buffer.putInt(userId.toInt())
            buffer.put(1, (code ?: 0u).toByte())
            userId = buffer.getInt(0).toUInt()
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }
    var userIdAsWG26IDCode: String
        @Bindable
        get() {
            val buffer = ByteBuffer.allocate(4)
            buffer.putInt(userId.toInt())
            return buffer.getShort(2).toUShort().toString()
        }
        set(value) {
            val code = value.toUShortOrNull()
            val buffer = ByteBuffer.allocate(4)
            buffer.putInt(userId.toInt())
            buffer.putShort(2, (code ?: 0u).toShort())
            userId = buffer.getInt(0).toUInt()
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }
}

