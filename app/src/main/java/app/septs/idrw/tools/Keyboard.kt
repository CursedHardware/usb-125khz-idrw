package app.septs.idrw.tools

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


object Keyboard {
    fun hidden(activity: Activity) {
        val view = activity.currentFocus ?: View(activity)
        view.clearFocus()
        val inputService = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputService.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }
}