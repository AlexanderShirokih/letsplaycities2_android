package ru.aleshi.letsplaycities.base.dictionary

import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import ru.aleshi.letsplaycities.BadTokenException
import ru.aleshi.letsplaycities.FileProvider
import java.io.File
import java.util.*

class DictionaryFactoryTest {

    private lateinit var fileProvider: FileProvider

    @get:Rule
    var tempFolder = TemporaryFolder()

    @Before
    fun setUp() {
        fileProvider = Mockito.mock(FileProvider::class.java)
        Mockito.`when`(fileProvider.open(ArgumentMatchers.anyString())).thenAnswer {
            javaClass.getResourceAsStream("/${it.arguments[0] as String}")
        }
        Mockito.`when`(fileProvider.filesDir)
            .thenAnswer { File(javaClass.getResource("/test_valid_v10.bin")!!.toURI()).parentFile }
    }

    @Test
    fun testSampleDataExists() {
        Assert.assertNotNull(javaClass.getResource("/test_valid_v10.bin"))
        Assert.assertNotNull(javaClass.getResource("/test_valid_v12.bin"))
        Assert.assertNotNull(javaClass.getResource("/test_err_count_v10.bin"))
        Assert.assertNotNull(javaClass.getResource("/test_err_hmagic_v10.bin"))
        Assert.assertNotNull(javaClass.getResource("/test_err_eof_v10.bin"))
    }

    @Test
    fun testLoadingValidDictionarySuccessful() {
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .load("invalid", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()
    }

    @Test
    fun testLoadingInvalidDictionaryWithBadCountThrowsAnException() {
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .load("invalid", "test_err_count_v10.bin")
            .test()
            .await()
            .assertError(BadTokenException::class.java)
    }

    @Test
    fun testLoadingInvalidDictionaryWithBadMagicThrowsAnException() {
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .load("invalid", "test_err_hmagic_v10.bin")
            .test()
            .await()
            .assertError(BadTokenException::class.java)
    }

    @Test
    fun testLoadingInvalidDictionaryWithEOFThrowsAnException() {
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .load("invalid", "test_err_eof_v10.bin")
            .test()
            .await()
            .assertError(BadTokenException::class.java)
    }

    @Test
    fun testIfBothDictionaryFilesPresentInternalWillBePreferred() {
        val cache = DictionaryFactory.Cache()
        DictionaryFactory(fileProvider, cache)
            .load("test_valid_v12.bin", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()

        Assert.assertEquals(12, cache.entry!!.version)
    }

    @Test
    fun testIfInternalFileOlderThanEmbeddedItWillBeIgnored() {
        val cache = DictionaryFactory.Cache()
        DictionaryFactory(fileProvider, cache)
            .load("test_valid_v10.bin", "test_valid_v12.bin")
            .test()
            .await()
            .assertNoErrors()

        Assert.assertNotNull(cache.entry)
        Assert.assertEquals(12, cache.entry!!.version)
    }

    @Test
    fun testIfInternalFileThrowsInErrorEmbeddedFileWillLoaded() {
        val errTempFile = tempFile("test_err_count_v10.bin")
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .load(errTempFile.toString(), "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()
    }

    @Test
    fun testLoadingToCacheAfterSuccessfulLoading() {
        val cache = DictionaryFactory.Cache()

        DictionaryFactory(fileProvider, cache)
            .load("invalid", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()

        Assert.assertNotNull(cache.entry)
        Assert.assertEquals(10, cache.entry!!.version)
        val dictionary = cache.entry!!.dictionaryService

        DictionaryFactory(fileProvider, cache)
            .load("invalid", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()
            .assertValue { it == dictionary }

        Assert.assertNotNull(cache.entry)
        Assert.assertSame(dictionary, cache.entry!!.dictionaryService)
    }

    @Test
    fun testUpdatingCacheAfterNewerFileLoaded() {
        val cache = DictionaryFactory.Cache()

        DictionaryFactory(fileProvider, cache)
            .load("invalid", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()

        val dictionary = cache.entry!!.dictionaryService

        DictionaryFactory(fileProvider, cache)
            .load("test_valid_v12.bin", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()
            .assertValue { it != dictionary }

        Assert.assertNotNull(cache.entry)
        Assert.assertEquals(12, cache.entry!!.version)
        Assert.assertNotSame(dictionary, cache.entry!!.dictionaryService)
    }

    @Test
    fun testGetLatestInfoWhenOnlyEmbeddedPresent() {
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .getLatestDictionaryInfo("invalid", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()
            .assertValue { it.version == 10 }
    }

    @Test
    fun testGetLatestInfoWhenBothFilesPresentReturnsNewer() {
        DictionaryFactory(fileProvider, DictionaryFactory.Cache())
            .getLatestDictionaryInfo("test_valid_v12.bin", "test_valid_v10.bin")
            .test()
            .await()
            .assertNoErrors()
            .assertValue {
                it.version == 12 && it.savePath == File(
                    fileProvider.filesDir,
                    "test_valid_v12.bin"
                )
            }
    }

    private fun tempFile(filename: String): File {
        return tempFolder.newFile("${UUID.randomUUID()}_filename.bin").apply {
            File(fileProvider.filesDir, filename).copyTo(this, true)
        }
    }
}