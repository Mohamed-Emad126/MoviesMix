package com.memad.moviesmix.utils

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.memad.moviesmix.R


fun Context.unAvailableFeature(
    permission: String,
    permissionUnavailableMessage: String,
    permissionLauncher: ActivityResultLauncher<String>
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.unavilable))
        .setMessage(permissionUnavailableMessage)
        .setPositiveButton(getString(R.string.grant)) { _, _ ->
            permissionLauncher.launch(permission)
        }
        .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        .create()
        .show()
}

fun Context.permissionExplanation(
    permissionNeededMessage: String,
    positiveButtonFunction: () -> Unit
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.permission_needed))
        .setMessage(
            permissionNeededMessage
        )
        .setPositiveButton(getString(R.string.ok)) { _, _ ->
            positiveButtonFunction()
        }
        .setNegativeButton(getString(R.string.no_thanks)) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}

fun Context.checkmPermission(
    fragment: Fragment,
    permission: String,
    ifGrantedFunction: () -> Unit,
    permissionNeededMessage: String,
    permissionLauncher: ActivityResultLauncher<String>
) {

    when {
        PermissionChecker.checkSelfPermission(this, permission)
                == PermissionChecker.PERMISSION_GRANTED -> {
            ifGrantedFunction()
        }
        fragment.shouldShowRequestPermissionRationale(permission) -> {
            permissionExplanation(permissionNeededMessage) {
                permissionLauncher.launch(permission)
            }
        }
        else -> {
            permissionLauncher.launch(permission)
        }
    }
}

