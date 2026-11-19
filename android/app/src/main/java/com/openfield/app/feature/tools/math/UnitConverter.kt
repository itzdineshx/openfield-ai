package com.openfield.app.feature.tools.math

import java.util.Locale

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    MASS("Mass & Weight"),
    TEMPERATURE("Temperature"),
    DATA("Digital Storage"),
    SPEED("Speed")
}

data class UnitDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

/**
 * Deterministic unit conversion utility. Zero model inference required.
 */
object UnitConverter {

    private val lengthUnits = listOf(
        UnitDefinition("m", "Meters", "m", { it }, { it }),
        UnitDefinition("km", "Kilometers", "km", { it * 1000.0 }, { it / 1000.0 }),
        UnitDefinition("cm", "Centimeters", "cm", { it * 0.01 }, { it / 0.01 }),
        UnitDefinition("mm", "Millimeters", "mm", { it * 0.001 }, { it / 0.001 }),
        UnitDefinition("mi", "Miles", "mi", { it * 1609.344 }, { it / 1609.344 }),
        UnitDefinition("yd", "Yards", "yd", { it * 0.9144 }, { it / 0.9144 }),
        UnitDefinition("ft", "Feet", "ft", { it * 0.3048 }, { it / 0.3048 }),
        UnitDefinition("in", "Inches", "in", { it * 0.0254 }, { it / 0.0254 })
    )

    private val massUnits = listOf(
        UnitDefinition("kg", "Kilograms", "kg", { it }, { it }),
        UnitDefinition("g", "Grams", "g", { it * 0.001 }, { it / 0.001 }),
        UnitDefinition("mg", "Milligrams", "mg", { it * 0.000001 }, { it / 0.000001 }),
        UnitDefinition("lb", "Pounds", "lb", { it * 0.45359237 }, { it / 0.45359237 }),
        UnitDefinition("oz", "Ounces", "oz", { it * 0.028349523125 }, { it / 0.028349523125 })
    )

    private val tempUnits = listOf(
        UnitDefinition("c", "Celsius", "°C", { it }, { it }),
        UnitDefinition("f", "Fahrenheit", "°F", { (it - 32.0) * (5.0 / 9.0) }, { it * (9.0 / 5.0) + 32.0 }),
        UnitDefinition("k", "Kelvin", "K", { it - 273.15 }, { it + 273.15 })
    )

    private val dataUnits = listOf(
        UnitDefinition("b", "Bytes", "B", { it }, { it }),
        UnitDefinition("kb", "Kilobytes", "KB", { it * 1024.0 }, { it / 1024.0 }),
        UnitDefinition("mb", "Megabytes", "MB", { it * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0) }),
        UnitDefinition("gb", "Gigabytes", "GB", { it * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0) }),
        UnitDefinition("tb", "Terabytes", "TB", { it * 1024.0 * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0 * 1024.0) })
    )

    private val speedUnits = listOf(
        UnitDefinition("mps", "Meters per second", "m/s", { it }, { it }),
        UnitDefinition("kmh", "Kilometers per hour", "km/h", { it / 3.6 }, { it * 3.6 }),
        UnitDefinition("mph", "Miles per hour", "mph", { it * 0.44704 }, { it / 0.44704 }),
        UnitDefinition("knot", "Knots", "kn", { it * 0.514444 }, { it / 0.514444 })
    )

    fun getUnits(category: UnitCategory): List<UnitDefinition> {
        return when (category) {
            UnitCategory.LENGTH -> lengthUnits
            UnitCategory.MASS -> massUnits
            UnitCategory.TEMPERATURE -> tempUnits
            UnitCategory.DATA -> dataUnits
            UnitCategory.SPEED -> speedUnits
        }
    }

    fun convert(value: Double, fromUnitId: String, toUnitId: String, category: UnitCategory): Double {
        val units = getUnits(category)
        val from = units.find { it.id == fromUnitId } ?: throw IllegalArgumentException("Unknown unit $fromUnitId")
        val to = units.find { it.id == toUnitId } ?: throw IllegalArgumentException("Unknown unit $toUnitId")

        val baseValue = from.toBase(value)
        return to.fromBase(baseValue)
    }

    fun formatConverted(value: Double): String {
        return if (value == kotlin.math.round(value) && kotlin.math.abs(value) < 1e10) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
    }
}
