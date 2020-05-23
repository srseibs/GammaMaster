package com.sailinghawklabs.gammamaster

import android.util.Log
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

class GammaDataManager(private var gamma: Double, private var referenceZ: Double) {
    private var rloadGtZ0: Double = 0.0
    private var rLoadLtZ0: Double = 0.0
    private var vswr: Double = 0.0
    private var returnlossDb: Double = 0.0
    private var mismatchlossDb: Double = 0.0

    init {
        recalculate(gamma)
    }

    private fun recalculate(newGamma: Double) {
        gamma = newGamma
        rloadGtZ0 = referenceZ * (1 + gamma) / (1 - gamma)
        rLoadLtZ0 = referenceZ * (1 - gamma) / (1 + gamma)
        vswr = (1.0 + gamma) / (1.0 - gamma)
        returnlossDb = -20.0 * log10(gamma)
        mismatchlossDb = -10.0 * log10(1 - gamma * gamma)
    }

    fun getGamma(): Double {
        return gamma
    }

    fun setGamma(newGamma: Double) {
        recalculate(newGamma)
    }

    fun getZ0(): Double {
        return referenceZ
    }

    fun setZ0(z0: Double) {
        referenceZ = z0
        recalculate(gamma)
    }

    fun getReturnloss(): Double {
        return returnlossDb
    }

    fun setReturnloss(rl: Double) {
        gamma = (10.0).pow(-rl / 20)
        recalculate(gamma)
    }

    fun getVswr(): Double {
        return vswr
    }

    fun setVswr(vswr: Double) {
        recalculate((vswr - 1.0) / (vswr + 1.0))
    }

    fun getRloadLtZ0(): Double {
        return rLoadLtZ0
    }

    fun setRlLoadLtZ0(r: Double) {
        recalculate((referenceZ - r) / (r + referenceZ))
    }

    fun getRloadGtZ0(): Double {
        return rloadGtZ0
    }

    fun setRloadGtZ0(r: Double) {
        recalculate((r - referenceZ) / (r + referenceZ))
    }

    fun getMismatchLossDb(): Double {
        return mismatchlossDb
    }

    fun setMismatchlossDb(loss: Double) {
        recalculate(sqrt(1 - (10.0).pow(-loss / 10.0)))
        Log.d("GammaDataManager", "setMismatchlossDb: loss = $loss, calculated: $mismatchlossDb")
    }
}