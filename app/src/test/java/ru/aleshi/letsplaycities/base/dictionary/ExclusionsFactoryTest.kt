package ru.aleshi.letsplaycities.base.dictionary

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import ru.aleshi.letsplaycities.FileProvider

class ExclusionsFactoryTest {

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
        assertNotNull(javaClass.getResource("/others.txt"))
    }

    @Test
    fun testNormalLoadingExclusionsFactory() {
        ExclusionsFactory(fileProvider, createErrMap())
            .load()
            .test()
            .await()
            .assertNoErrors()
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