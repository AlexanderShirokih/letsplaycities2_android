package ru.aleshi.letsplaycities.base.dictionary

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.BuildConfig
import ru.aleshi.letsplaycities.base.GamePreferences
import java.io.*
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * [DictionaryUpdater] is responsible for checking dictionary updates and downloading it
 * @param gson [Gson] instance for serialization update message
 * @param prefs [GamePreferences] instance
 * @param dictionaryFactory [DictionaryUpdater] which is used to get latest dictionary version
 */
class DictionaryUpdater constructor(
    private val gson: Gson,
    private val prefs: GamePreferences,
    private val dictionaryFactory: DictionaryFactory
) {

    companion object {
        private const val HOST = "http://${BuildConfig.HOST}:8080"
    }


    /**
     * Contains progress information when downloading is in process
     * @param loadingPercent current percent of file been loaded
     */
    class LoadingProgress(val loadingPercent: Int)

    fun checkForUpdates(): Observable<LoadingProgress> {
        val updPeriod = prefs.getDictionaryUpdatePeriod()
        if (updPeriod > 0) {
            val lastTime = prefs.dictionaryUpdateDate
            val now = System.currentTimeMillis()
            if (now - lastTime > TimeUnit.HOURS.toMillis(updPeriod)) {
                prefs.dictionaryUpdateDate = now
                return fetchUpdatesInfo()
            }
        }
        return Observable.empty()
    }

    /**
     * Fetches updates from server and compares it with local version.
     * Run downloading update it newer version if available.
     * @return [Observable] with [LoadingProgress] that
     */
    private fun fetchUpdatesInfo(): Observable<LoadingProgress> {
        return fetchLatestDictionaryVersion()
            .zipWith(
                dictionaryFactory.getLatestDictionaryInfo(),
                BiFunction { fetchedVersion: Int, localInfo: DictionaryFactory.DictionaryInfo ->
                    fetchedVersion to localInfo
                })
            .filter { it.first > it.second.version }
            .map { "$HOST/data-${it.first}.bin" to it.second }
            .toObservable()
            .flatMap { loadFile(it.first, it.second.savePath) }
    }

    /**
     * Fetches info about newer dictionary versions on origin $HOST/update.
     * Return [Single] of latest dictionary version fetched from server.
     */
    private fun fetchLatestDictionaryVersion(): Single<Int> {
        val url = URL("$HOST/update")

        return Single.just(url)
            .subscribeOn(Schedulers.io())
            .map { it.openConnection() }
            .doOnSuccess { it.connect() }
            .map { InputStreamReader(BufferedInputStream(url.openStream(), 64)) }
            .map { gson.fromJson(it, UpdateRequest::class.java) }
            .map { it.dictionary.version }
    }

    /**
     * Opens network connection on [urlName] and saves file to [saveFile].
     * @param urlName URL for file downloading
     * @param saveFile destination file
     * @return [Observable] which emits [LoadingProgress] when
     * loading in progress and then completes.
     * When network is unreachable or cannot complete downloading by any reasons,
     * will trigger `onError`.
     */
    private fun loadFile(urlName: String, saveFile: File): Observable<LoadingProgress> {
        return Observable.create<LoadingProgress> { emitter ->
            try {
                val url = URL(urlName)
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength

                // download the file
                val input = BufferedInputStream(
                    url.openStream(),
                    8192
                )

                // Output stream
                val output = FileOutputStream(saveFile)

                val buffer = ByteArray(4096)

                var total = 0
                var count = input.read(buffer)

                while (count != -1) {
                    total += count
                    emitter.onNext(LoadingProgress(((total * 100f) / lengthOfFile).toInt()))

                    output.write(buffer, 0, count)
                    count = input.read(buffer)
                }

                output.flush()
                output.close()
                input.close()
                emitter.onComplete()
            } catch (e: IOException) {
                emitter.tryOnError(e)
            }
        }.subscribeOn(Schedulers.io())
    }
}
