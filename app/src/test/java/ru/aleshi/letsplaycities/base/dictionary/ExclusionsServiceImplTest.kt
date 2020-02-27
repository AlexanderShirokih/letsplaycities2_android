package ru.aleshi.letsplaycities.base.dictionary

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.aleshi.letsplaycities.base.dictionary.ExclusionsServiceImpl.*

class ExclusionsServiceImplTest {

    lateinit var exclusion: ExclusionsServiceImpl

    @Before
    fun setUp() {
        exclusion = ExclusionsServiceImpl(
            exclusionsList = createExclusionMap(),
            countries = listOf("aabr", "creade", "denito"),
            states = listOf("statea", "stateb"),
            errMessages = ErrorCode.values().associate { t -> t to "${t.name}:%s" }
        )
    }

    @Test
    fun checkForExclusionWithCountryReturnsAnExclusion() {
        assertEquals("THIS_IS_A_COUNTRY:Creade", exclusion.checkForExclusion("creade"))
    }

    @Test
    fun checkForExclusionWithStateReturnsAnExclusion() {
        assertEquals("THIS_IS_A_STATE:Stateb", exclusion.checkForExclusion("stateb"))
    }

    @Test
    fun checkForExclusionByAllTypesReturnsAnExclusion() {
        assertEquals("RENAMED_CITY:Bcde", exclusion.checkForExclusion("bcde"))
        assertEquals("INCOMPLETE_CITY:Incomplete", exclusion.checkForExclusion("cdef"))
        assertEquals("NOT_A_CITY:Efgh", exclusion.checkForExclusion("efgh"))
        assertEquals("THIS_IS_NOT_A_CITY:Defg", exclusion.checkForExclusion("defg"))
    }

    @Test
    fun checkForExclusionForAlternativeNameReturnsEmptyString() {
        assertEquals("", exclusion.checkForExclusion("abcd"))
    }

    @Test
    fun checkForExclusionWithNormalWordReturnEmptyString() {
        assertEquals("", exclusion.checkForExclusion("normal"))
    }

    @Test
    fun getAlternativeNameReturnsAlternativeNameOrNullIfNotExists() {
        assertEquals("alter", exclusion.getAlternativeName("abcd"))
        assertNull(exclusion.getAlternativeName("bcde"))
    }

    @Test
    fun testNormalWordHasNoExclusions() {
        assertTrue(exclusion.hasNoExclusions("normal"))
    }

    @Test
    fun testThatHasNoExclusionReturnsWordWithAnyCase() {
        assertFalse(exclusion.hasNoExclusions("AbCd"))
    }

    @Test
    fun testThatHasNoExclusionReturnsFalseForAllTheOtherTypes() {
        assertFalse(exclusion.hasNoExclusions("creade"))
        assertFalse(exclusion.hasNoExclusions("stateb"))
        assertFalse(exclusion.hasNoExclusions("bcde"))
    }

    private fun createExclusionMap() = mapOf(
        "abcd" to Exclusion(ExclusionType.ALTERNATIVE_NAME, "alter"),
        "bcde" to Exclusion(ExclusionType.CITY_WAS_RENAMED, "renamed"),
        "cdef" to Exclusion(ExclusionType.INCOMPLETE_NAME, "incomplete"),
        "defg" to Exclusion(ExclusionType.REGION_NAME, "region"),
        "efgh" to Exclusion(ExclusionType.NOT_A_CITY, "not a city")
    )

}