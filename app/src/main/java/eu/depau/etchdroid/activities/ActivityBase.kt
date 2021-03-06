package eu.depau.etchdroid.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import eu.depau.etchdroid.R
import eu.depau.etchdroid.kotlin_exts.toast
import eu.depau.etchdroid.utils.DoNotShowAgainDialogFragment
import eu.depau.etchdroid.utils.NightModeHelper


abstract class ActivityBase : AppCompatActivity() {
    protected lateinit var nightModeHelper: NightModeHelper
    val DISMISSED_DIALOGS_PREFS = "dismissed_dialogs"
    val READ_REQUEST_CODE = 42
    val READ_EXTERNAL_STORAGE_PERMISSION = 29

    var shouldShowAndroidPieAlertDialog: Boolean
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                return false
            val settings = getSharedPreferences(DISMISSED_DIALOGS_PREFS, 0)
            return !settings.getBoolean("Android_Pie_alert", false)
        }
        set(value) {
            val settings = getSharedPreferences(DISMISSED_DIALOGS_PREFS, 0)
            val editor = settings.edit()
            editor.putBoolean("Android_Pie_alert", !value)
            editor.apply()
        }

    fun showAndroidPieAlertDialog(callback: () -> Unit) {
        val dialogFragment = DoNotShowAgainDialogFragment(nightModeHelper.nightMode)
        dialogFragment.title = getString(R.string.android_pie_bug)
        dialogFragment.message = getString(R.string.android_pie_bug_dialog_text)
        dialogFragment.positiveButton = getString(R.string.i_understand)
        dialogFragment.listener = object : DoNotShowAgainDialogFragment.DialogListener {
            override fun onDialogNegative(dialog: DoNotShowAgainDialogFragment, showAgain: Boolean) {}
            override fun onDialogPositive(dialog: DoNotShowAgainDialogFragment, showAgain: Boolean) {
                shouldShowAndroidPieAlertDialog = showAgain
                callback()
            }
        }
        dialogFragment.show(supportFragmentManager, "DMGBetaAlertDialogFragment")
    }

    fun checkAndRequestStorageReadPerm(): Boolean {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                toast(getString(R.string.storage_permission_required))
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_PERMISSION)
            }
        } else {
            // Permission granted
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nightModeHelper = NightModeHelper(this, R.style.AppTheme)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_licenses -> {
                val intent = Intent(this, LicensesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_reset_warnings -> {
                getSharedPreferences(DISMISSED_DIALOGS_PREFS, 0)
                        .edit().clear().apply()
                toast(getString(R.string.warnings_reset))
                return true
            }
            R.id.action_nightmode -> {
                nightModeHelper.toggle()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}