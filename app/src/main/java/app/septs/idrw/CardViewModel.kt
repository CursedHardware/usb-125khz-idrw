package app.septs.idrw

import android.databinding.BaseObservable
import android.databinding.Bindable
import app.septs.idrw.usb.CardType
import app.septs.idrw.usb.IDCard


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

    @get:Bindable
    var autoIncrement = false
        set(value) {
            field = value
            if (autoDecrement && value) autoDecrement = false
            notifyPropertyChanged(BR.autoIncrement)
        }

    @get:Bindable
    var autoDecrement = false
        set(value) {
            field = value
            if (autoIncrement && value) autoIncrement = false
            notifyPropertyChanged(BR.autoDecrement)
        }

    var idCard = IDCard()
        set(value) {
            field = value
            notifyPropertyChanged(BR.customerIdAsString)
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }

    var customerIdAsString: String
        @Bindable
        get() = idCard.customerId.toString()
        set(value) {
            idCard.customerId = value.toUByteOrNull() ?: 0u
            notifyPropertyChanged(BR.customerIdAsString)
        }

    var userIdAsWG34: String
        @Bindable
        get() = idCard.wiegand34.toString()
        set(value) {
            idCard.wiegand34 = value.toUIntOrNull() ?: 0u
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }

    var userIdAsWG26FacilityCode: String
        @Bindable
        get() = idCard.wiegand26FacilityCode.toString()
        set(value) {
            idCard.wiegand26FacilityCode = value.toUByteOrNull() ?: 0u
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }

    var userIdAsWG26IDCode: String
        @Bindable
        get() = idCard.wiegand26IDCode.toString()
        set(value) {
            idCard.wiegand26IDCode = value.toUShortOrNull() ?: 0u
            notifyPropertyChanged(BR.userIdAsWG34)
            notifyPropertyChanged(BR.userIdAsWG26FacilityCode)
            notifyPropertyChanged(BR.userIdAsWG26IDCode)
        }
}


