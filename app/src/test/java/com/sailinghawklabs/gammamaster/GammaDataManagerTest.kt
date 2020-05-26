package com.sailinghawklabs.gammamaster

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.math.abs


internal class GammaDataManagerTest {
    // region constants ----------------------------------------------------------------------------
    companion object {
        val DEFAULT_CONDITION =
                Solution(50.0, 13.98, 0.2, 1.5, 75.0, 33.33, 0.18)

        val solutionVectors: ArrayList<Solution> = arrayListOf(
                // set load impedance to 75 ohm
                Solution(z0 = 50.0, returnLossDb = 13.98, gamma = 0.2,
                        vswr = 1.5, rGtZ0 = 75.0, rLtZ0 = 33.33, misMatchDb = 0.18),

                // set returnLoss to 0
                Solution(z0 = 50.0, returnLossDb = 0.0, gamma = 1.0,
                        vswr = Double.POSITIVE_INFINITY, rGtZ0 = Double.POSITIVE_INFINITY, rLtZ0 = 0.0,
                        misMatchDb = Double.POSITIVE_INFINITY),

                // set VSWR to 2.0
                Solution(z0 = 50.0, returnLossDb = 9.54, gamma = .33,
                        vswr = 2.0, rGtZ0 = 100.0, rLtZ0 = 25.00, misMatchDb = 0.51
                ),

                // set gamma to 0.0
                Solution(z0 = 50.0, returnLossDb = Double.POSITIVE_INFINITY, gamma = 0.0,
                        vswr = 1.0, rGtZ0 = 50.0, rLtZ0 = 50.00, misMatchDb = 0.0
                ),

                // set R>Z0 to 200.00
                Solution(z0 = 50.0, returnLossDb = 4.44, gamma = 0.60,
                        vswr = 4.0, rGtZ0 = 200.00, rLtZ0 = 12.50, misMatchDb = 1.94
                ),

                // set R<Z0 to 20.00
                Solution(z0 = 50.0, returnLossDb = 7.36, gamma = 0.43,
                        vswr = 2.5, rGtZ0 = 125.00, rLtZ0 = 20.00, misMatchDb = 0.88
                ),

                // set MismatchLoss to 3.01
                Solution(z0 = 50.0, returnLossDb = 3.01, gamma = 0.71,
                        vswr = 5.83, rGtZ0 = 291.39, rLtZ0 = 8.58, misMatchDb = 3.01
                )
        )
    }
    // endregion constants -------------------------------------------------------------------------

// region helper fields ------------------------------------------------------------------------
// endregion helper fields ---------------------------------------------------------------------

    private lateinit var mSUT: GammaDataManager

    @Before
    fun setup() {
        mSUT = GammaDataManager(DEFAULT_CONDITION.gamma, DEFAULT_CONDITION.z0)
    }

    @Test
    fun getGamma_defaultSuccess_returnsDefaultValues() {
        validateSolution(DEFAULT_CONDITION)
    }

    @Test
    fun setz0to75_getSolution() {
        val index = 0
        mSUT.setZ0(solutionVectors[index].z0)
        validateSolution(solutionVectors[index])
    }

    @Test
    fun setReturnLossTo0_getSolution() {
        val index = 1
        mSUT.setReturnloss(solutionVectors[index].returnLossDb)
        validateSolution(solutionVectors[index])
    }

    @Test
    fun setVSWRto2_getSolution() {
        val index = 2
        mSUT.setVswr(solutionVectors[index].vswr)
        validateSolution(solutionVectors[index])
    }

    @Test
    fun setGammaTo0_getSolution() {
        val index = 3
        mSUT.setGamma(solutionVectors[index].gamma)
        validateSolution(solutionVectors[index])
    }

    @Test
    fun setRgtZ0To12p50_getSolution() {
        val index = 4
        mSUT.setRloadGtZ0(solutionVectors[index].rGtZ0)
        validateSolution(solutionVectors[index])
    }

    @Test
    fun setRltZ0To20_getSolution() {
        val index = 5
        mSUT.setRloadLtZ0(solutionVectors[index].rLtZ0)
        validateSolution(solutionVectors[index])
    }

    @Test
    fun setMismatchTo3_getSolution() {
        val index = 6
        mSUT.setMismatchlossDb(solutionVectors[index].misMatchDb)
        validateSolution(solutionVectors[index])
    }

    // region helper methods -----------------------------------------------------------------------
    private fun validateSolution(solution: Solution) {
        assertThat("check Z0", doubleEquals(mSUT.getZ0(), solution.z0))
        assertThat("check returnLoss", doubleEquals(mSUT.getReturnloss(), solution.returnLossDb, 0.01))
        assertThat("check gamma", doubleEquals(mSUT.getGamma(), solution.gamma, 0.005))
        assertThat("check VSWR", doubleEquals(mSUT.getVswr(), solution.vswr,0.005))
        assertThat("check R>Z0 calc", doubleEquals(mSUT.getRloadGtZ0(), solution.rGtZ0, 0.01))
        assertThat("check R<Z0 calc", doubleEquals(mSUT.getRloadLtZ0(), solution.rLtZ0, 0.01))
        assertThat("check Mismatch Loss", doubleEquals(mSUT.getMismatchLossDb(), solution.misMatchDb, 0.01))
    }


    private fun doubleEquals(d1: Double, d2: Double, threshold: Double = 0.001): Boolean {
        var result = false
        if (d1.isInfinite() && d2.isInfinite()) {
            result = true
        } else if (abs(d1 - d2) < threshold) {
            result = true
        }

        if (!result) {
            println("GammaDataManagerTest, not equal: d1: $d1, d2: $d2, d1-d2: ${d1 - d2}")
        }

        return result
    }
// endregion helper methods --------------------------------------------------------------------

    // region helper classes -----------------------------------------------------------------------
    data class Solution(
            val z0: Double,
            val returnLossDb: Double,
            val gamma: Double,
            val vswr: Double,
            val rGtZ0: Double,
            val rLtZ0: Double,
            val misMatchDb: Double)


// endregion helper classes --------------------------------------------------------------------

}