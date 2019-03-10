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
        val input = (dest.toString() + source.toString()).toLongOrNull()
        val disallow = input != null && isInRange(range.first, range.last, input)
        return if (disallow) null else ""
    }

    private fun isInRange(a: Long, b: Long, target: Long) =
        if (b > a) target in a..b else target in b..a
}