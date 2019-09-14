package ru.aleshi.letsplaycities.base.combos

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityComboInfoTest {

    @Test
    fun isQuick() {
        assertTrue(CityComboInfo.create(2400, "test", 10).isQuick)
        assertTrue(CityComboInfo.create(5000, "test", 11).isQuick)
        assertFalse(CityComboInfo.create(50001, "test", 12).isQuick)
    }

    @Test
    fun isShort() {
        assertTrue(CityComboInfo.create(1000, "рим", 13).isShort)
        assertTrue(CityComboInfo.create(1000, "ялта", 15).isShort)
        assertFalse(CityComboInfo.create(1000, "москва", 25).isShort)
    }

    @Test
    fun isLong() {
        assertTrue(CityComboInfo.create(1000, "ростов-на-дону", 25).isLong)
        assertTrue(CityComboInfo.create(1000, "екатеринбург", 13).isLong)
        assertFalse(CityComboInfo.create(1000, "москва", 15).isLong)
    }
}