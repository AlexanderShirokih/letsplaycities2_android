package ru.aleshi.letsplaycities.base.dictionary

import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Test

class DictionaryServiceImplTest {

    @Test
    fun testDictionaryHasAllPresentCities() {
        val dic = DictionaryServiceImpl(simpleMap())

        assertEquals(CityResult.OK, dic.checkCity("abcd").blockingFirst())
        assertEquals(CityResult.OK, dic.checkCity("bcde").blockingFirst())
        assertEquals(CityResult.OK, dic.checkCity("cdef").blockingFirst())
        assertEquals(CityResult.CITY_NOT_FOUND, dic.checkCity("another").blockingFirst())
    }

    @Test
    fun testDictionaryMarkUsed() {
        val dic = DictionaryServiceImpl(simpleMap())

        assertEquals(CityResult.OK, dic.checkCity("abcd").blockingFirst())
        dic.markUsed("abcd")

        assertEquals(CityResult.ALREADY_USED, dic.checkCity("abcd").blockingFirst())
        assertEquals(CityResult.OK, dic.checkCity("bcde").blockingFirst())
    }

    @Test
    fun testDictionaryMarkUsedWhenWordNotExists() {
        val dic = DictionaryServiceImpl(simpleMap())

        assertEquals(CityResult.CITY_NOT_FOUND, dic.checkCity("wyxz").blockingFirst())
        dic.markUsed("wyxz")

        assertEquals(CityResult.CITY_NOT_FOUND, dic.checkCity("wyxz").blockingFirst())
    }

    @Test
    fun testDifficultyProperty() {
        val dic = DictionaryServiceImpl(hashMapOf())

        assertEquals(1.toByte(), dic.difficulty)

        dic.difficulty = 2
        assertEquals(2.toByte(), dic.difficulty)

        dic.difficulty = 5
        assertEquals(3.toByte(), dic.difficulty)
    }

    @Test
    fun testCountryCodeWhenExistsAndNot() {
        val dic = DictionaryServiceImpl(simpleMap())
        assertEquals(10.toShort(), dic.getCountryCode("abcd"))
        assertEquals(0.toShort(), dic.getCountryCode("wxyz"))
    }

    @Test
    fun testResetFlipsUsageFlags() {
        val dic = DictionaryServiceImpl(simpleMap())
        dic.markUsed("abcd")
        dic.markUsed("bcde")
        dic.markUsed("another")
        dic.reset()

        assertEquals(CityResult.OK, dic.checkCity("abcd").blockingFirst())
        assertEquals(CityResult.OK, dic.checkCity("bcde").blockingFirst())
        assertEquals(CityResult.OK, dic.checkCity("cdef").blockingFirst())
        assertEquals(CityResult.CITY_NOT_FOUND, dic.checkCity("another").blockingFirst())
    }

    @Test
    fun testClearReallyClearsDictionary() {
        val dic = DictionaryServiceImpl(simpleMap())
        dic.clear()
        assertEquals(CityResult.CITY_NOT_FOUND, dic.checkCity("abcd").blockingFirst())
    }

    @Test
    fun testRandomWordWhenNoAvailable() {
        assertNull(DictionaryServiceImpl(simpleMap()).getRandomWord('z').blockingGet())
    }

    @Test
    fun testRandomWordWhenEmpty() {
        assertNull(DictionaryServiceImpl(hashMapOf()).getRandomWord('a').blockingGet())
    }

    @Test
    fun testRandomWordWhenNormal() {
        assertThat(
            DictionaryServiceImpl(simpleMap()).getRandomWord('a').blockingGet(),
            CoreMatchers.startsWith("a")
        )
    }

    @Test
    fun testRandomWordNotReturnUsed() {
        val dic = DictionaryServiceImpl(simpleMap())
        dic.markUsed("abcd")

        assertNull(dic.getRandomWord('a').blockingGet())
    }

    @Test
    fun testGetCorrectionReplacement() {
        val corrections =
            DictionaryServiceImpl(simpleMapCyr()).getCorrectionVariants("абвв").blockingGet()
        println("$corrections")

        assertEquals("абвг", corrections[0])
    }

    @Test
    fun testGetCorrectionInsertion() {
        val corrections =
            DictionaryServiceImpl(simpleMapCyr()).getCorrectionVariants("абдвг").blockingGet()
        println("$corrections")

        assertEquals("абвг", corrections[0])
    }

    private fun simpleMap() =
        hashMapOf(
            "abcd" to DictionaryServiceImpl.CityProperties(1, 10),
            "bcde" to DictionaryServiceImpl.CityProperties(2, 20),
            "cdef" to DictionaryServiceImpl.CityProperties(3, 30)
        )

    private fun simpleMapCyr() =
        hashMapOf(
            "абвг" to DictionaryServiceImpl.CityProperties(1, 10),
            "бвгд" to DictionaryServiceImpl.CityProperties(2, 20),
            "вгде" to DictionaryServiceImpl.CityProperties(3, 30)
        )
}