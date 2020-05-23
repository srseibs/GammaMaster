package com.sailinghawklabs.gammamaster

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import java.util.*

class GammaViewController(
        private val mContext: Context,
        private val myListener: OnMismatchUpdate,
        private var etReturnloss: EditText,
        private var etGamma: EditText,
        private var etVswr: EditText,
        private var etRloadGt: EditText,
        private var etRloadLt: EditText,
        private var etMismatchloss: EditText

)    : OnEditorActionListener, OnFocusChangeListener {

    private lateinit var mData: GammaDataManager

    private var gamma: Double = 0.2
    private var referenceZ: Double = 50.0

    init {
        mData = GammaDataManager(gamma, referenceZ)

        // setUpListener(etZ0)
        setUpListener(etReturnloss)
        setUpListener(etGamma)
        setUpListener(etVswr)
        setUpListener(etRloadGt)
        setUpListener(etRloadLt)
        setUpListener(etMismatchloss)

        updateViews()
    }

    fun setGammaAndReference(newGamma: Double, newZ0: Double) {
        mData.setZ0(newZ0)
        mData.setGamma(newGamma)
        updateViews()
        myListener.onMismatchErrorUpdateNeeded()
    }

    fun setGamma(newGamma: Double) {
        mData.setGamma(newGamma)
        updateViews()
        myListener.onMismatchErrorUpdateNeeded()
    }

    fun getGamma() : Double {
        return mData.getGamma()
    }

    fun setReferenceZ(newReferenceZ: Double) {
        mData.setZ0(newReferenceZ)
        updateViews()
        myListener.onMismatchErrorUpdateNeeded()
    }

    interface OnMismatchUpdate {
        fun onMismatchErrorUpdateNeeded()
    }

    private fun setUpListener(v: EditText) {
        v.imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        v.onFocusChangeListener = this
        v.setOnEditorActionListener(this)
        v.setSelectAllOnFocus(true)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        Log.d("GammaViewController", "onFocusChange: view= ${v.resources.getResourceName(v.id)}, focus= $hasFocus")
        if (!hasFocus) {
            enterValue(v as EditText)
            v.clearFocus()
            myListener.onMismatchErrorUpdateNeeded()
        } else {
            v.requestFocus()
            val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun enterValue(v: EditText) {
        var value: Double? = null
        val valueString = v.text.toString()
        try {
            value = valueString.toDouble()
        } catch (err: NumberFormatException) {
            val toast = Toast.makeText(v.context, "Invalid number format found!", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            toast.show()
        }
        Log.d("GammaViewController", "enterValue: value= $value, valueString= $valueString, id= ${v.resources.getResourceName(v.id)}")
        if (value == null) {
            return
        }
        val viewId = v.id
        when {
            etGamma.id == viewId -> {
                mData.setGamma(value)
            }
            etReturnloss.id == viewId -> {
                mData.setReturnloss(value)
            }
            etMismatchloss.id == viewId -> {
                mData.setMismatchlossDb(value)
            }
            etRloadGt.id == viewId -> {
                mData.setRloadGtZ0(value)
            }
            etRloadLt.id == viewId -> {
                mData.setRlLoadLtZ0(value)
            }
            etVswr.id == viewId -> {
                mData.setVswr(value)
            }
            else -> {
                throw IllegalStateException("Unexpected entry field")
            }
        }
        updateViews()
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        Log.d("GammaViewController", "onEditorAction: actionId = $actionId, KeyEvent = $event")
        v.clearFocus()
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            enterValue(v as EditText)
            hideKeyboard(v)
            myListener.onMismatchErrorUpdateNeeded()
            return true // consumed.
        }
        return false // pass on to other listeners.
    }

    private fun hideKeyboard(v: TextView) {
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun updateViews() {
        //etZ0.setText(String.format(Locale.getDefault(), "%.1f", mData.getZ0()))
        etReturnloss.setText(String.format(Locale.getDefault(), "%.2f", mData.getReturnloss()))
        etGamma.setText(String.format(Locale.getDefault(), "%.3f", mData.getGamma()))
        etVswr.setText(String.format(Locale.getDefault(), "%.3f", mData.getVswr()))
        etRloadGt.setText(String.format(Locale.getDefault(), "%.1f", mData.getRloadGtZ0()))
        etRloadLt.setText(String.format(Locale.getDefault(), "%.1f", mData.getRloadLtZ0()))
        etMismatchloss.setText(String.format(Locale.getDefault(), "%.2f", mData.getMismatchLossDb()))
    }
}