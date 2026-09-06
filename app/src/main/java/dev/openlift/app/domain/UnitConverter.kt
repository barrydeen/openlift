package dev.openlift.app.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

enum class WeightUnit(val label: String) {
    KG("kg"),
    LB("lb");

    companion object {
        fun from(pref: String?): WeightUnit = entries.firstOrNull { it.name == pref } ?: KG
    }
}

object UnitConverter {

    private const val KG_PER_LB = 2.2046226218

    /** Convert a stored canonical weight (kg) into the display unit. */
    fun fromKg(weightKg: Double, unit: WeightUnit): Double {
        val value = if (unit == WeightUnit.KG) weightKg else weightKg * KG_PER_LB
        return roundToTenth(value)
    }

    /** Convert an entered weight in the display unit into canonical kg. */
    fun toKg(weight: Double, unit: WeightUnit): Double {
        val value = if (unit == WeightUnit.KG) weight else weight / KG_PER_LB
        return roundToTenth(value)
    }

    fun displayWeight(weightKg: Double, unit: WeightUnit): String =
        trimZeros(fromKg(weightKg, unit))

    fun parseWeight(text: String, unit: WeightUnit): Double? {
        val parsed = text.toDoubleOrNull() ?: return null
        return toKg(parsed, unit)
    }

    fun weightStep(unit: WeightUnit): Double = if (unit == WeightUnit.KG) 0.5 else 1.0

    private fun roundToTenth(value: Double): Double =
        roundToHalf(value)

    private fun roundToHalf(value: Double): Double {
        val rounded = BigDecimal(value).setScale(1, RoundingMode.HALF_UP).toDouble()
        return rounded
    }

    private fun trimZeros(value: Double): String {
        val rounded = BigDecimal(value).setScale(1, RoundingMode.HALF_UP)
        return if (rounded.toPlainString().endsWith(".0")) {
            rounded.setScale(0, RoundingMode.HALF_UP).toPlainString()
        } else {
            rounded.toPlainString()
        }
    }
}
