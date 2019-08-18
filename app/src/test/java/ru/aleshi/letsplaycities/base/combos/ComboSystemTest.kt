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

        override fun updateBadge(comboType: ComboType, multiplier: Int) {
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
        cs = ComboSystem(test)
    }

    @After
    fun tearDown() {
        cs.clear()
    }

    @Test
    fun addNormalCites() {
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(4000, "tests"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(0, 0, 0)
    }

    @Test
    fun addCity() {
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(1, 1, 0)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(1, 2, 0)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(1, 3, 0)
        cs.addCity(CityComboInfo.create(3000, "tests"))
        test.assertValues(1, 3, 1)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(1, 3, 1)
    }

    @Test
    fun testCombinedCombos() {
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tts"))
        test.assertValues(0, 0, 0)
        cs.addCity(CityComboInfo.create(1000, "tts"))
        test.assertValues(1, 1, 0)
        cs.addCity(CityComboInfo.create(1000, "tts"))
        test.assertValues(2, 3, 0)
        cs.addCity(CityComboInfo.create(3000, "tts"))
        test.assertValues(2, 4, 1)
        cs.addCity(CityComboInfo.create(1000, "tests"))
        test.assertValues(2, 4, 2)
    }

    @Test
    fun clear() {
    }
}