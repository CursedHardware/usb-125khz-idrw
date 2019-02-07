package app.septs.idrw.tools

import android.text.InputFilter
import android.text.Spanned

class InputFilterValueRange(private var range: LongRange) : InputFilter {
    override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toLong()
            if (isInRange(range.first, range.last, input)) {
                return null
            }
        } catch (e: NumberFormatException) {
            // ignore exception
        }
        return ""
    }

    private fun isInRange(a: Long, b: Long, c: Long): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}