package dev.openlift.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun `kg to kg is identity`() {
        assertEquals(100.0, UnitConverter.fromKg(100.0, WeightUnit.KG), 0.001)
    }

    @Test
    fun `kg to lb multiplies`() {
        assertEquals(220.5, UnitConverter.fromKg(100.0, WeightUnit.LB), 0.001)
    }

    @Test
    fun `lb to kg divides`() {
        assertEquals(100.0, UnitConverter.toKg(220.462, WeightUnit.LB), 0.05)
    }

    @Test
    fun `displayWeight trims trailing zero`() {
        assertEquals("100", UnitConverter.displayWeight(100.0, WeightUnit.KG))
        assertEquals("220.5", UnitConverter.displayWeight(100.0, WeightUnit.LB))
        assertEquals("20", UnitConverter.displayWeight(20.0, WeightUnit.KG))
    }

    @Test
    fun `parseWeight roundtrips`() {
        val parsed = UnitConverter.parseWeight("100", WeightUnit.KG)
        assertEquals(100.0, parsed!!, 0.001)
    }

    @Test
    fun `parseWeight handles lb`() {
        val parsed = UnitConverter.parseWeight("220.5", WeightUnit.LB)
        assertEquals(100.0, parsed!!, 0.05)
    }

    @Test
    fun `parseWeight rejects invalid`() {
        assertEquals(null, UnitConverter.parseWeight("abc", WeightUnit.KG))
    }

    @Test
    fun `unit from string falls back to kg`() {
        assertEquals(WeightUnit.KG, WeightUnit.from("garbage"))
        assertEquals(WeightUnit.LB, WeightUnit.from("LB"))
        assertEquals(WeightUnit.KG, WeightUnit.from(null))
    }
}
