package com.master.musicroomclient.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackBarUtils {

    fun showSnackBar(view: View, message: String) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackBar.setAction("Dismiss") { snackBar.dismiss() }
        snackBar.show()
    }
}