package ru.aleshi.letsplaycities.base.combos

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ComboSystemTest {

    class TestSystemView : ComboSystemView {
        var deletes = 0
        var adders = 0
        var updates = 0

        override fun deleteBadge(comboType: ComboType) {
            deletes++
        }

        override fun addBadge(comboType: ComboType) {
            adders++
        }

        override fun updateBadge(comboType: ComboType, multiplier: Float) {
            updates++
        }

        fun assertValues(add: Int, upd: Int, del: Int) {
            assertEquals(add, adders)
            assertEquals(upd, updates)
            assertEquals(del, deletes)
        }

    }

    lateinit var test: TestSystemView
    lateinit var cs: ComboSystem

    @Before
    fun setUp() {
        test = TestSystemView()
        cs = ComboSystem(test, true)
    }

    @After
    fun tearDown() {
        cs.clear()
    }

    @Test
    fun addNormalCites() {
        cs.addCity(CityComboInfo.create(1000, "tests", 10))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests", 11))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(5100, "tests", 12))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests", 13))
        test.assertValues(0, 0, 0)
    }

    @Test
    fun addCity() {
        cs.addCity(CityComboInfo.create(1000, "tests", 16))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests", 17))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests", 18))
        test.assertValues(1, 1, 0)
        cs.addCity(CityComboInfo.create(1000, "tests", 19))
        test.assertValues(1, 2, 0)
        cs.addCity(CityComboInfo.create(1000, "tests", 20))
        test.assertValues(1, 3, 0)
        cs.addCity(CityComboInfo.create(5100, "tests", 21))
        test.assertValues(1, 3, 1)
        cs.addCity(CityComboInfo.create(1000, "tests", 22))
        test.assertValues(1, 3, 1)
    }

    @Test
    fun testCombinedCombos() {
        cs.addCity(CityComboInfo.create(1000, "tests", 14))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tts", 15))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tts", 16))
        test.assertValues(1, 1, 0)
        cs.addCity(CityComboInfo.create(1000, "tts", 17))
        test.assertValues(2, 3, 0)
        cs.addCity(CityComboInfo.create(5200, "tts", 18))
        test.assertValues(2, 4, 1)
        cs.addCity(CityComboInfo.create(1000, "tests", 19))
        test.assertValues(2, 4, 2)
    }

    @Test
    fun testSameCountry() {
        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(0, 0, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(0, 0, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(0, 0, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(0, 0, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(0, 0, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(0, 0, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(1, 1, 0)

        cs.addCity(CityComboInfo.create(5200, "tests", 14))
        test.assertValues(1, 2, 0)
    }

}