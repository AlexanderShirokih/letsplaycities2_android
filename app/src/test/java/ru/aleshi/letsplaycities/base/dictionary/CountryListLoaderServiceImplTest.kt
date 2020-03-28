package ru.aleshi.letsplaycities.base.dictionary

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import ru.aleshi.letsplaycities.FileProvider
import java.io.BufferedReader
import java.io.InputStreamReader

class CountryListLoaderServiceImplTest {

    private lateinit var fileProvider: FileProvider

    @Before
    fun setUp() {
        fileProvider = Mockito.mock(FileProvider::class.java)
        Mockito.`when`(fileProvider.open(ArgumentMatchers.anyString())).thenAnswer {
            javaClass.getResourceAsStream("/${it.arguments[0] as String}")
        }
    }

    @Test
    fun testSampleDataExists() {
        assertNotNull(javaClass.getResource("/countries.txt"))
    }

    @Test
    fun testSampleDataValid() {
        assertTrue(BufferedReader(InputStreamReader(fileProvider.open("countries.txt")))
            .readLines()
            .filter { it.isNotBlank() }
            .all {
                if (it.indexOf('|') < 0) false
                else {
                    val (name, cc) = it.split('|', '+')
                    name.isNotBlank() && cc.isNotBlank() && cc.matches("[0-9]+".toRegex())
                }
            })
    }

    @Test
    fun testDataLoadsCorrectly() {
        val list = runBlocking {
            CountryListLoaderServiceImpl(fileProvider)
                .loadCountryList()
        }
        assertEquals(4, list.size)
        assertEquals("ангола", list.first().name)
        assertEquals(41.toShort(), list.first().countryCode)
        assertTrue(list.component2().hasSiblingCity)
    }
}