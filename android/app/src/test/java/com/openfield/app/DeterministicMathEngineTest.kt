package com.openfield.app

import com.openfield.app.feature.tools.math.DeterministicMathEngine
import com.openfield.app.feature.tools.math.MathResult
import com.openfield.app.feature.tools.math.UnitCategory
import com.openfield.app.feature.tools.math.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicMathEngineTest {

    @Test
    fun evaluate_operatorPrecedence() {
        val res = DeterministicMathEngine.evaluate("2 + 3 * 4")
        assertTrue(res is MathResult.Success)
        assertEquals(14.0, (res as MathResult.Success).value, 0.0001)
        assertEquals("14", res.formatted)
    }

    @Test
    fun evaluate_parenthesesOverridePrecedence() {
        val res = DeterministicMathEngine.evaluate("(2 + 3) * 4")
        assertTrue(res is MathResult.Success)
        assertEquals(20.0, (res as MathResult.Success).value, 0.0001)
        assertEquals("20", res.formatted)
    }

    @Test
    fun evaluate_powersAndModulo() {
        val powRes = DeterministicMathEngine.evaluate("2 ^ 3 + 1")
        assertTrue(powRes is MathResult.Success)
        assertEquals(9.0, (powRes as MathResult.Success).value, 0.0001)

        val modRes = DeterministicMathEngine.evaluate("17 % 5")
        assertTrue(modRes is MathResult.Success)
        assertEquals(2.0, (modRes as MathResult.Success).value, 0.0001)
    }

    @Test
    fun evaluate_unaryMinus() {
        val res1 = DeterministicMathEngine.evaluate("-5 + 12")
        assertTrue(res1 is MathResult.Success)
        assertEquals(7.0, (res1 as MathResult.Success).value, 0.0001)

        val res2 = DeterministicMathEngine.evaluate("4 * -3")
        assertTrue(res2 is MathResult.Success)
        assertEquals(-12.0, (res2 as MathResult.Success).value, 0.0001)
    }

    @Test
    fun evaluate_functions_sqrt_abs_round() {
        val sqrtRes = DeterministicMathEngine.evaluate("sqrt(144) + 6")
        assertTrue(sqrtRes is MathResult.Success)
        assertEquals(18.0, (sqrtRes as MathResult.Success).value, 0.0001)

        val absRes = DeterministicMathEngine.evaluate("abs(-99)")
        assertTrue(absRes is MathResult.Success)
        assertEquals(99.0, (absRes as MathResult.Success).value, 0.0001)

        val roundRes = DeterministicMathEngine.evaluate("round(4.6)")
        assertTrue(roundRes is MathResult.Success)
        assertEquals(5.0, (roundRes as MathResult.Success).value, 0.0001)
    }

    @Test
    fun evaluate_divisionByZero_returnsSafeError() {
        val res = DeterministicMathEngine.evaluate("42 / 0")
        assertTrue(res is MathResult.Error)
        assertEquals("Division by zero", (res as MathResult.Error).message)
    }

    @Test
    fun evaluate_syntaxError_returnsSafeError() {
        val res = DeterministicMathEngine.evaluate("(3 + 4 *")
        assertTrue(res is MathResult.Error)
    }

    @Test
    fun unitConverter_lengthConversions() {
        val meters = UnitConverter.convert(5.0, "km", "m", UnitCategory.LENGTH)
        assertEquals(5000.0, meters, 0.001)

        val inches = UnitConverter.convert(1.0, "ft", "in", UnitCategory.LENGTH)
        assertEquals(12.0, inches, 0.001)
    }

    @Test
    fun unitConverter_temperatureConversions() {
        val fahrenheit = UnitConverter.convert(100.0, "c", "f", UnitCategory.TEMPERATURE)
        assertEquals(212.0, fahrenheit, 0.01)

        val celsius = UnitConverter.convert(32.0, "f", "c", UnitCategory.TEMPERATURE)
        assertEquals(0.0, celsius, 0.01)
    }

    @Test
    fun unitConverter_dataConversions() {
        val gb = UnitConverter.convert(2048.0, "mb", "gb", UnitCategory.DATA)
        assertEquals(2.0, gb, 0.001)
    }
}
