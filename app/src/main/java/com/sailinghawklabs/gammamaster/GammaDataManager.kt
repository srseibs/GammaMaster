package com.sailinghawklabs.gammamaster

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
        gamma = positiveOnly(gamma)
        recalculate(positiveOnly(gamma))
    }

    private fun positiveOnly(x: Double): Double {
        return if (x < 0) 0.0 else x
    }

    private fun recalculate(newGamma: Double) {
        // adding 0.0 avoids a floating point "-0" that is possible with IEEE Floats
        // it looks ugly but preventing it here means not worrying about display later
        gamma = 0.0 + newGamma
        rloadGtZ0 = 0.0 + (referenceZ * (1 + gamma) / (1 - gamma))
        rLoadLtZ0 = 0.0 + (referenceZ * (1 - gamma) / (1 + gamma))
        vswr = 0.0 + ((1.0 + gamma) / (1.0 - gamma))
        returnlossDb = 0.0 + (-20.0 * log10(gamma))
        mismatchlossDb = 0.0 + (-10.0 * log10(1 - gamma * gamma))
    }

    fun getGamma(): Double {
        return gamma
    }

    fun setGamma(newGamma: Double) {
        recalculate(positiveOnly(newGamma))
    }

    fun getZ0(): Double {
        return referenceZ
    }

    fun setZ0(z0: Double) {
        referenceZ = positiveOnly(z0)
        recalculate(gamma)
    }

    fun getReturnloss(): Double {
        return returnlossDb
    }

    fun setReturnloss(inRl: Double) {
        val rl = positiveOnly(inRl)
        gamma = (10.0).pow(-rl / 20)
        recalculate(gamma)
    }

    fun getVswr(): Double {
        return vswr
    }

    fun setVswr(inVswr: Double) {
        val vswr = positiveOnly(inVswr)
        recalculate((vswr - 1.0) / (vswr + 1.0))
    }

    fun getRloadLtZ0(): Double {
        return rLoadLtZ0
    }

    fun setRloadLtZ0(inR: Double) {
        val r = positiveOnly(inR)
        recalculate((referenceZ - r) / (r + referenceZ))
    }

    fun getRloadGtZ0(): Double {
        return rloadGtZ0
    }

    fun setRloadGtZ0(inR: Double) {
        val r = positiveOnly(inR)
        recalculate((r - referenceZ) / (r + referenceZ))
    }

    fun getMismatchLossDb(): Double {
        return mismatchlossDb
    }

    fun setMismatchlossDb(inLoss: Double) {
        val loss  = positiveOnly(inLoss)
        recalculate(sqrt(1 - (10.0).pow(-loss / 10.0)))
    }
}