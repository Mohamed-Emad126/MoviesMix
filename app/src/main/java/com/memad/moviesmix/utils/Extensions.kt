package com.memad.moviesmix.utils

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.memad.moviesmix.R

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

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}


fun ChipGroup.addChip(context: Context, label: String) {
    Chip(context).apply {
        id = View.generateViewId()
        text = label
        isClickable = true
        isCheckable = true
        setChipSpacing(8)
        isCheckedIconVisible = false
        isFocusable = true
        chipBackgroundColor = ColorStateList.valueOf(resources.getColor(R.color.primaryLightColor, this.context.theme))
        addView(this)

    }

}