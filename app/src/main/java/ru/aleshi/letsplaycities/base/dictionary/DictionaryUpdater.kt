package ru.aleshi.letsplaycities.base.dictionary

import com.google.gson.Gson
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.network.lpsv3.NetworkClient
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
class DictionaryUpdater(val mGson: Gson) {

    interface DownloadingListener {
        fun onStart()
        fun onProgress(res: Int)
        fun onEnd()
        fun onError()
    }

    companion object {
        private val HOST = "http://${NetworkClient.HOST}:80"
    }

    private var inProgress = false

    fun checkForUpdates(
        gamePreferences: GamePreferences,
        dictionary: Dictionary,
        listener: DownloadingListener
    ): Disposable? {
        if (inProgress) return null
        val updPeriod = gamePreferences.getDictionaryUpdatePeriod()
        if (updPeriod > 0) {
            val lastTime = gamePreferences.getDictionaryUpdateDate()
            val now = System.currentTimeMillis()
            if (now - lastTime > TimeUnit.HOURS.toMillis(updPeriod)) {
                gamePreferences.setDictionaryUpdateDate(now)
                return checkForUpdates(dictionary, listener)
            }
        }
        return null
    }

    private fun checkForUpdates(dictionary: Dictionary, listener: DownloadingListener): Disposable {
        return fetchLastDataVersion()
            .filter { it > dictionary.version }
            .map { "$HOST/data-$it.bin" }
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { listener.onStart() }
            .flatMap { loadFile(it, dictionary.savePath) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(listener::onProgress, { listener.onError() }, listener::onEnd)
    }

    private fun fetchLastDataVersion(): Maybe<Int> {
        val url = URL("$HOST/update")

        return Single.just(url)
            .subscribeOn(Schedulers.io())
            .map { it.openConnection() }
            .doOnSuccess { it.connect() }
            .filter { it.contentLength > 0 }
            .map { InputStreamReader(BufferedInputStream(url.openStream(), 64)) }
            .map { mGson.fromJson(it, UpdateRequest::class.java) }
            .map { it.dictionary.version }
    }

    private fun loadFile(name: String, file: File): Observable<Int> {
        return Observable.create<Int> { emitter ->
            try {
                val url = URL(name)
                val connection = url.openConnection()
                connection.connect()

                val lengthOfFile = connection.contentLength

                // download the file
                val input = BufferedInputStream(
                    url.openStream(),
                    8192
                )

                // Output stream
                val output = FileOutputStream(file)

                val data = ByteArray(4096)

                var total = 0
                var count = input.read(data)

                while (count != -1) {
                    total += count
                    emitter.onNext(((total * 100f) / lengthOfFile).toInt())

                    output.write(data, 0, count)
                    count = input.read(data)
                }

                output.flush()
                output.close()
                input.close()
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
    }
}
