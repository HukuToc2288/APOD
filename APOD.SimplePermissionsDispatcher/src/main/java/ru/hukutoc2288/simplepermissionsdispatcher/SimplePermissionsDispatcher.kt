package ru.hukutoc2288.simplepermissionsdispatcher

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private var currentRequestCode = 0

abstract class SimplePermissionsDispatcher(internal val permission: String) {

    private val requestCode = currentRequestCode++
    internal lateinit var currentRunnable: Runnable

    fun executeWithPermission(context: Activity, runnable: Runnable) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            runnable.run()
        } else { currentRunnable = runnable
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                onShowRationale()
            } else {
                ActivityCompat.requestPermissions(context, arrayOf(permission), requestCode)
            }
        }
    }

    fun onRequestPermissionsResult(context: Activity, grantResults: IntArray) {
        if (grantResults.isEmpty()){
            SimpleRationaleDialogFragment()
            // something seems to be broken, don't process this
            return
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            currentRunnable.run()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                onPermissionDenied()
            } else {
                onNeverAskAgain()
            }
        }
    }

    fun proceedAfterRationale(context: Activity) {
        ActivityCompat.requestPermissions(context, arrayOf(permission), requestCode)
    }

    abstract fun onPermissionDenied()

    abstract fun onNeverAskAgain()

    abstract fun onShowRationale()
}