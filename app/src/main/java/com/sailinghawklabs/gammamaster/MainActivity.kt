package com.sailinghawklabs.gammamaster


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.sailinghawklabs.gammamaster.GammaViewController.OnMismatchUpdate
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMismatchUpdate, OnEditorActionListener {
    private lateinit var gammaViewController1: GammaViewController
    private lateinit var gammaViewController2: GammaViewController
    private lateinit var mismatchViewController: MismatchViewController

    companion object {
        private val TAG = MainActivity::class.java.name
        private const val KEY_PRESET_GAMMA1 = "PRESET_GAMMA1"
        private const val KEY_PRESET_GAMMA2 = "PRESET_GAMMA2"
        private const val KEY_PRESET_Z0 = "PRESET_Z0"
        private const val KEY_GAMMA1 = "GAMMA1"
        private const val KEY_GAMMA2 = "GAMMA2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: entered")

        et_z0.setOnEditorActionListener(this)
        et_z0.imeOptions = EditorInfo.IME_ACTION_DONE

        mismatchViewController = MismatchViewController(
                tv_mismatchErrorMagPlus = tv_mismag_p,
                tv_mismatchErrorMagMinus = tv_mismag_n,
                tv_mismatchErrorPhase = tv_misphas)

        gammaViewController1 = GammaViewController(
                mContext = this,
                myListener = this,
                etReturnloss = et_rl_1,
                etGamma = et_gamma_1,
                etVswr = et_vswr_1,
                etRloadGt = et_rload_lg_1,
                etRloadLt = et_rload_sm_1,
                etMismatchloss = et_misloss_1)

        gammaViewController2 = GammaViewController(
                mContext = this,
                myListener = this,
                etReturnloss = et_rl_2,
                etGamma = et_gamma_2,
                etVswr = et_vswr_2,
                etRloadGt = et_rload_lg_2,
                etRloadLt = et_rload_sm_2,
                etMismatchloss = et_misloss_2)

        if (savedInstanceState == null) {
            preset()
        }

        enableAdvertisements()
    }

    private fun enableAdvertisements() {
        MobileAds.initialize(this) {
            Log.d("MainActivity", "MobileAds initialization returned")
        }

        // add physical device(s) as Test Devices for development (not release)
        if (BuildConfig.DEBUG) {
            val config = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf("28B5489C26A46FD308BFB094FC7F36D8"))
                    .build()
            MobileAds.setRequestConfiguration(config)
        }
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        Log.d("MainActivity", "onCreate: ad id = ${adView.adUnitId}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // put gammas into bundle; everything else can be recalculated after lifecycle event
        Log.d(TAG, "onSaveInstanceState: gamma: ${gammaViewController1.getGamma()}, ${gammaViewController2.getGamma()}")
        outState.putDouble(KEY_GAMMA1, gammaViewController1.getGamma())
        outState.putDouble(KEY_GAMMA2, gammaViewController2.getGamma())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // et_z0 survives rotation, so it does not need babysitting...
        Log.d(TAG, "onRestoreInstanceState: z0: " + et_z0!!.text.toString())
        super.onRestoreInstanceState(savedInstanceState)
        gammaViewController1.setGamma(savedInstanceState.getDouble(KEY_GAMMA1))
        gammaViewController2.setGamma(savedInstanceState.getDouble(KEY_GAMMA2))
    }

    private fun preset() {
        Log.d("MainActivity", "preset: ")
        // preset to the Shared Preferences (or the String resources as default)
        val preferences = getDefaultSharedPreferences(this)
        val z0  = preferences.getString(KEY_PRESET_Z0, getString(R.string.default_z0))!!
        val gamma1 = preferences.getString(KEY_PRESET_GAMMA1, getString(R.string.default_gamma1))!!
        val gamma2 = preferences.getString(KEY_PRESET_GAMMA2, getString(R.string.default_gamma2))!!
        et_z0.setText(z0)
        gammaViewController1.setGammaAndReference(gamma1.toDouble(), z0.toDouble())
        gammaViewController2.setGammaAndReference(gamma2.toDouble(), z0.toDouble())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */inflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onMismatchErrorUpdateNeeded() {
        Log.d("MainActivity", "onMismatchErrorUpdateNeeded: ")
        mismatchViewController.updateViews(gammaViewController1.getGamma(), gammaViewController2.getGamma())
    }

    private fun updateZ0(z0_string: String) {
        Log.d("MainActivity", "updateZ0: ${et_z0.text}")
        et_z0.setText(z0_string)
        val doubleZ0 = z0_string.toDouble()
        gammaViewController1.setReferenceZ(doubleZ0)
        gammaViewController2.setReferenceZ(doubleZ0)
    }

    // this handles the Z0 edittext changes
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onEditorAction: Z0 changed")
        v.clearFocus()
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val z0String = v.text.toString()
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            updateZ0(z0String)
            return true
        }
        return false
    }

    private fun saveSharedPreferences(z0: Double, gamma1: Double, gamma2: Double) {
        Log.d("MainActivity", "saveSharedPreferences: Z0 = $z0, Gamma1 = $gamma1, Gamma2 = $gamma2")
        val sharedPreferences = getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_PRESET_Z0, z0.toString())
        editor.putString(KEY_PRESET_GAMMA1, gamma1.toString())
        editor.putString(KEY_PRESET_GAMMA2, gamma2.toString())
        editor.apply()
    }

    fun menuHandler(item: MenuItem) {
        when (item.itemId) {
            R.id.action_preset -> {
                preset()
            }

            R.id.action_save -> {
                saveSharedPreferences(
                        gammaViewController1.getReferenceZ(),
                        gammaViewController1.getGamma(),
                        gammaViewController2.getGamma())
                val toast = Toast.makeText(this, R.string.preset_stored, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                toast.show()
            }

            R.id.action_revert -> {
                et_z0.setText(getString(R.string.default_z0))
                gammaViewController1.setGammaAndReference(
                        getString(R.string.default_gamma1).toDouble(),
                        getString(R.string.default_z0).toDouble())
                gammaViewController2.setGammaAndReference(
                        getString(R.string.default_gamma2).toDouble(),
                        getString(R.string.default_z0).toDouble())
            }
            R.id.action_help -> {
                val dialog = DialogUtils(this)
                dialog.showHelp()
            }
            R.id.action_about -> {
                val dialog = DialogUtils(this)
                dialog.showAbout()
            }
        }
    }

}