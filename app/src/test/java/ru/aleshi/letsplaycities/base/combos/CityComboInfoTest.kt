package ru.aleshi.letsplaycities.base.combos

import org.junit.Test

import org.junit.Assert.*

class CityComboInfoTest {

    @Test
    fun isQuick() {
        assertTrue(CityComboInfo.create(2400, "test").isQuick)
        assertTrue(CityComboInfo.create(5000, "test").isQuick)
        assertFalse(CityComboInfo.create(50001, "test").isQuick)
    }

    @Test
    fun isShort() {
        assertTrue(CityComboInfo.create(1000, "рим").isShort)
        assertTrue(CityComboInfo.create(1000, "ялта").isShort)
        assertFalse(CityComboInfo.create(1000, "москва").isShort)
    }

    @Test
    fun isLong() {
        assertTrue(CityComboInfo.create(1000, "ростов-на-дону").isLong)
        assertTrue(CityComboInfo.create(1000, "екатеринбург").isLong)
        assertFalse(CityComboInfo.create(1000, "москва").isLong)
    }
}