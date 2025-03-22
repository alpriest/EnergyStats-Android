package com.example
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.settings.DisplayUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class PowerExtensionsTest {
    @Test
    fun testRoundTo2DP() {
        val input = 2.6999999999989086
        val result = input.energy(DisplayUnit.Watts, 2)
        assertEquals("2699 Wh", result)
    }

    @Test
    fun testRoundAgain() {
        val input = 2.799999
        val result = input.energy(DisplayUnit.Watts, 2)
        assertEquals("2799 Wh", result)
    }

    @Test
    fun testRoundAgain2() {
        val input = 2.799999
        val result = input.energy(DisplayUnit.Kilowatts, 3)
        assertEquals("2.799 kWh", result)
    }
//
//    @Test
//    fun `rounded should round to 3 decimal places`() {
//        val input = 2.6999999999989086
//        val result = input.rounded(3)
//        assertEquals(2.7, result, 0.0000001)
//    }
//
//    @Test
//    fun `rounded should round down correctly`() {
//        val input = 2.1234
//        val result = input.rounded(2)
//        assertEquals(2.12, result, 0.0000001)
//    }
//
//    @Test
//    fun `rounded should round up correctly`() {
//        val input = 2.126
//        val result = input.rounded(2)
//        assertEquals(2.13, result, 0.0000001)
//    }
}