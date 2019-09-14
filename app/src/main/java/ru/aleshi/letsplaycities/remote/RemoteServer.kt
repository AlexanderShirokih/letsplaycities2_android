package ru.aleshi.letsplaycities.remote

import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import ru.aleshi.letsplaycities.base.game.BaseServer
import ru.quandastudio.lpsclient.model.WordResult
import javax.inject.Inject

class RemoteServer @Inject constructor(private val mRemoteRepository: RemoteRepository) :
    BaseServer() {

    private var result: ReplaySubject<Pair<WordResult, String>> = ReplaySubject.create()

    override fun getWordsResult(): Observable<Pair<WordResult, String>> {
        return result.mergeWith(mRemoteRepository.words
            .doOnNext { mRemoteRepository.sendWord(WordResult.ACCEPTED, it.word) }
            .map { WordResult.RECEIVED to it.word })
    }

    override fun getInputMessages(): Observable<String> {
        return mRemoteRepository.messages.map { it.message }
    }

    override fun dispose() {
        mRemoteRepository.disconnect()
    }

    override fun broadcastResult(city: String) {
        mRemoteRepository.sendWord(WordResult.RECEIVED, city)
        result.onNext(WordResult.ACCEPTED to city)
    }

    override fun getTimeLimit(): Long = 92L

}