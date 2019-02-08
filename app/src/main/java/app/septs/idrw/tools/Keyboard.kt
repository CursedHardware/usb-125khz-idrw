package app.septs.idrw.tools

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


class Keyboard {
    companion object {
        fun hidden(activity: Activity) {
            val view = activity.currentFocus ?: View(activity)
            view.clearFocus()
            (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                hideSoftInputFromWindow(
                        view.windowToken,
                        InputMethodManager.RESULT_UNCHANGED_SHOWN
                )
            }
        }
    }
}