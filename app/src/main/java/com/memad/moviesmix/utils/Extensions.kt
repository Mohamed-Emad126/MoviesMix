package com.memad.moviesmix.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.lifecycle.MutableLiveData

fun Context.createDialog(loadingDialogBinding: View, cancelable: Boolean): Dialog {
    return Dialog(this).apply {
        window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        setContentView(loadingDialogBinding)
        setCanceledOnTouchOutside(cancelable)
    }
}