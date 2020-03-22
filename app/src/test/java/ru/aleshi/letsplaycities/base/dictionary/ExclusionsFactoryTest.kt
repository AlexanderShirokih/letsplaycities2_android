package ru.aleshi.letsplaycities.base.dictionary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import ru.aleshi.letsplaycities.FileProvider

class ExclusionsFactoryTest {

    private lateinit var fileProvider: FileProvider

    private lateinit var countryListLoaderService: CountryListLoaderService

    @Before
    fun setUp() {
        fileProvider = Mockito.mock(FileProvider::class.java)
        Mockito.`when`(fileProvider.open(ArgumentMatchers.anyString())).thenAnswer {
            javaClass.getResourceAsStream("/${it.arguments[0] as String}")
        }

        countryListLoaderService = Mockito.mock(CountryListLoaderService::class.java)
    }

    @Test
    fun testSampleDataExists() {
        assertNotNull(javaClass.getResource("/others.txt"))
    }

    @Test
    fun testNormalLoadingExclusionsFactory(): Unit = runBlocking(Dispatchers.IO) {
        Mockito.`when`(countryListLoaderService.loadCountryList()).thenReturn(
            listOf(
                CountryEntity("test", 1),
                CountryEntity("test2", 2)
            )
        )

        ExclusionsFactory(fileProvider, countryListLoaderService, createErrMap())
            .load()
            .test()
            .await()
            .assertNoErrors()

        Unit
    }

    private fun createErrMap() =
        mapOf(
            ExclusionsServiceImpl.ErrorCode.INCOMPLETE_CITY to "inc_city",
            ExclusionsServiceImpl.ErrorCode.NOT_A_CITY to "not_city",
            ExclusionsServiceImpl.ErrorCode.RENAMED_CITY to "renamed",
            ExclusionsServiceImpl.ErrorCode.THIS_IS_A_COUNTRY to "country",
            ExclusionsServiceImpl.ErrorCode.THIS_IS_A_STATE to "state",
            ExclusionsServiceImpl.ErrorCode.THIS_IS_NOT_A_CITY to "not_a_city"
        )


}