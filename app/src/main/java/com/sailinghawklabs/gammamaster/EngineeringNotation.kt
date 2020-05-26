package com.sailinghawklabs.gammamaster

import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class EngineeringNotation {


    // A.Greensted
    // http://www.labbookpages.co.uk
    // Version 1.0

    companion object EngNotation {
        private const val PREFIX_OFFSET = 5
        private val PREFIX_ARRAY = arrayOf("f", "p", "n", "µ", "m", "", "k", "M", "G", "T")
        private val PREFIX_TEST_ARRAY = charArrayOf('f', 'p', 'n', 'u', 'µ', 'm', 'k', 'K', 'M', 'G', 'T')
        private val PREFIX_EXP_ARRAY = intArrayOf(-15, -12, -9, -6, -6, -3, 3, 3, 6, 9, 12)
    }

    // Converts a double into a 'Engineering notated' string
    // val:	The value to be converted
    // dp:	The number of decimal places
    fun convert(`val`: Double, dp: Int): String {
        // If the value is zero, then simply return 0 with the correct number of dp
        var `val` = `val`
        if (`val` == 0.0) return String.format("%." + dp + "f", 0.0)

        // If the value is negative, make it positive so the log10 works
        val posVal = if (`val` < 0) -`val` else `val`
        val log10 = log10(posVal)

        // Determine how many orders of 3 magnitudes the value is
        val count = floor(log10 / 3).toInt()

        // Calculate the index of the prefix symbol
        val index = count + PREFIX_OFFSET

        // Scale the value into the range 1<=val<1000
        `val` /= 10.0.pow(count * 3.toDouble())
        return if (index >= 0 && index < PREFIX_ARRAY.size) {
            // If a prefix exists use it to create the correct string
            String.format("%." + dp + "f%s", `val`, PREFIX_ARRAY[index])
        } else {
            // If no prefix exists just make a string of the form 000e000
            String.format("%." + dp + "fe%d", `val`, count * 3)
        }
    }

    @Suppress("unused")
    fun parse(str: String): Double {
        return parse(str.toCharArray())
    }

    @Throws(NumberFormatException::class)
    fun parse(chars: CharArray): Double {
        var exponent = 0
        var value = 0.0
        var gotChar = false // Set to true once any non-whitespace, or minus character has been found
        var gotMinus = false // Set to true once a minus character has been found
        var gotDP = false //	Set to true once a decimal place character has been found
        var gotPrefix = false // Set to true once a prefix character has been found
        var gotDigit = false // Set to true once a digit character has been found

        // Search for start of string
        var start = 0
        while (start < chars.size) {
            if (chars[start] != ' ' && chars[start] != '\t') break
            start++
        }
        if (start == chars.size) throw NumberFormatException("Empty string")

        // Search for end of string
        var end = chars.size - 1
        while (end >= 0) {
            if (chars[end] != ' ' && chars[end] != '\t') break
            end--
        }

        // Iterate through characters
        CharLoop@ for (c in start..end) {
            // Check for a minus symbol
            if (chars[c] == '-') {
                if (gotChar) throw NumberFormatException("Can only have minus symbol at the start")
                if (gotMinus) throw NumberFormatException("Too many minus symbols")
                gotMinus = true
                continue@CharLoop
            }
            gotChar = true

            // Check for a numerical digit
            if (chars[c] in '0'..'9') {
                if (gotPrefix || gotDP) exponent--
                if (gotPrefix && gotDP) throw NumberFormatException("Cannot have digits after prefix when number includes decimal point")
                value *= 10.0
                value += chars[c] - '0'
                gotDigit = true
                continue@CharLoop
            }

            // Check for a decimal place
            if (chars[c] == '.') {
                if (gotDP) throw NumberFormatException("Too many decimal points")
                if (gotPrefix) throw NumberFormatException("Cannot have decimal point after prefix")
                gotDP = true
                continue@CharLoop
            }

            // Check for a match with a prefix character
            for (p in PREFIX_TEST_ARRAY.indices) {
                if (PREFIX_TEST_ARRAY[p] == chars[c]) {
                    if (gotPrefix) throw NumberFormatException("Too many prefixes")
                    exponent += PREFIX_EXP_ARRAY[p]
                    gotPrefix = true
                    continue@CharLoop
                }
            }
            throw NumberFormatException("Invalid character '" + chars[c] + "'")
        }

        // Check if any digits were found
        if (!gotDigit) throw NumberFormatException("No digits")

        // Apply negation if required
        if (gotMinus) value *= -1.0
        return value * 10.0.pow(exponent.toDouble())
    }
}