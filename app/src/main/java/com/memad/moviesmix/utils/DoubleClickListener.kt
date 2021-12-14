package com.memad.moviesmix.utils

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View

abstract class DoubleClickListener: View.OnClickListener {
    companion object{
        private const val DEFAULT_QUALIFICATION_SPAN = 300L
    }
    private var isSingleEvent = false
    private val doubleClickQualificationSpanInMillis =
        DEFAULT_QUALIFICATION_SPAN
    private var timestampLastClick = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: () -> Unit = {
        if (isSingleEvent) {
            onSingleClick()
        }
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - timestampLastClick < doubleClickQualificationSpanInMillis) {
            isSingleEvent = false
            handler.removeCallbacks(runnable)
            onDoubleClick()
            return
        }
        isSingleEvent = true
        handler.postDelayed(runnable, DEFAULT_QUALIFICATION_SPAN)
        timestampLastClick = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick()
    abstract fun onSingleClick()
}
