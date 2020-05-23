package com.sailinghawklabs.gammamaster


import android.widget.TextView
import java.util.*
import kotlin.math.log10

class MismatchViewController (
     var tv_mismatchErrorMagPlus: TextView,
     var tv_mismatchErrorMagMinus: TextView,
     var tv_mismatchErrorPhase: TextView)
{
    fun updateViews(gamma1: Double, gamma2: Double) {
        val mismatchmagPlus = 20.0 * log10(1 + gamma1 * gamma2)
        val mismatchmagMinus = 20.0 * log10(1 - gamma1 * gamma2)
        val mismatchphaseDeg = gamma1 * gamma2 * 180.0 / Math.PI
        tv_mismatchErrorMagPlus.text = String.format(Locale.getDefault(), "%+.2f", mismatchmagPlus)
        tv_mismatchErrorMagMinus.text = String.format(Locale.getDefault(), "%+.2f", mismatchmagMinus)
        tv_mismatchErrorPhase.text = String.format(Locale.getDefault(), "%.2f", mismatchphaseDeg)
    }
}