package ru.aleshi.letsplaycities.base.scoring

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.aleshi.letsplaycities.Localization
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.CityComboInfo
import ru.aleshi.letsplaycities.base.game.GameMode
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_LOSE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_TIME
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.F_WINS
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_BIG_CITIES
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_COMBO
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_FRQ_CITIES
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_HISCORE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_ONLINE
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.G_PARTS
import ru.aleshi.letsplaycities.base.scoring.ScoringGroupsHelper.V_EMPTY_S
import ru.aleshi.letsplaycities.utils.StringUtils
import javax.inject.Inject

class ScoreManager @Inject constructor(
    private val gameSession: GameSession,
    private val prefs: GamePreferences,
    private val cityStatDatabaseHelper: CityStatDatabaseHelper,
    @Localization("score_result_names")
    private val results: Map<GameResult, String>
) {
    enum class GameResult {
        TIME_UP,
        WIN,
        WIN_BY_REMOTE,
        DRAW
    }

    enum class ScoringType {
        BY_SCORE, // Чем длиннее слово, тем больше очков
        BY_TIME, // Чем быстрее даётся ответ, тем больше очков
        LAST_MOVE//Вседа побеждает тот, кто остался ждать ответа
    }

    // Статистика партий
    private lateinit var groupParts: ScoringGroup
    // Статистика онлайна
    private lateinit var groupOnline: ScoringGroup
    // Статистика рекордов
    private lateinit var groupHighScores: ScoringGroup
    // Статистика комбо
    private lateinit var groupCombo: ScoringGroup
    // Самые загадываемые города
    private lateinit var groupMostFrqCities: ScoringGroup
    // Самые длинные города
    private lateinit var groupMostBigCities: ScoringGroup

    private val allGroups: ScoringSet = ScoringGroupsHelper.fromPreferences(prefs)
    private val scoringType: ScoringType = ScoringType.values()[prefs.getCurrentScoringType()]
    private var lastTime: Long = 0

    init {
        loadStatGroups()
        saveStats()
    }

    private fun loadStatGroups() {
        groupCombo = allGroups.getGroupAt(G_COMBO)
        groupParts = allGroups.getGroupAt(G_PARTS)
        groupOnline = allGroups.getGroupAt(G_ONLINE)
        groupHighScores = allGroups.getGroupAt(G_HISCORE)
        groupMostFrqCities = allGroups.getGroupAt(G_FRQ_CITIES)
        groupMostBigCities = allGroups.getGroupAt(G_BIG_CITIES)
    }

    fun saveStats() {
        prefs.putScoring(allGroups.storeStatsGroups())
    }

    fun moveStarted() {
        //for by_time, save time
        lastTime = System.currentTimeMillis()
    }

    fun moveEnded(word: String): Disposable {
        val deltaTime = System.currentTimeMillis() - lastTime

        if (gameSession.gameMode == GameMode.MODE_NET)
            groupOnline.findField(F_TIME).add(deltaTime.toInt() / 1000)

        val disposable = mostChecker(word).subscribe()

        lastTime += deltaTime

        val points = when (scoringType) {
            ScoringType.BY_SCORE -> {
                checkCombos(deltaTime, word)
                word.length
            }
            ScoringType.BY_TIME -> {
                checkCombos(deltaTime, word)
                val dt = ((40000 - deltaTime.toInt()) / 2000)
                2 + if (dt > 0) dt else 0
            }
            ScoringType.LAST_MOVE -> 0
        }

        gameSession.currentUser?.increaseScore(points)

        saveStats()

        return disposable
    }

    private fun checkCombos(deltaTime: Long, word: String) {
        val current = gameSession.currentUser!!

        Completable.fromAction {
            current.comboSystem.addCity(
                CityComboInfo.create(
                    deltaTime,
                    word,
                    gameSession.game.getCountryCode(word)
                )
            )
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .andThen(Completable.fromAction {
                if (currentIsPlayer()) {
                    current.comboSystem.activeCombosList.forEach {
                        groupCombo.child[it.key.ordinal].max(it.value)
                    }
                }
            })
            .subscribe()
    }

    private fun mostChecker(word: String): Completable {
        return if (!currentIsPlayer())
            Completable.complete()
        else
            Completable.fromAction {
                //Most biggest cities
                val wlen = word.length
                for (i in groupMostBigCities.child.indices) {
                    val f = groupMostBigCities.child[i]
                    if (f.hasValue() && f.value() != V_EMPTY_S) {
                        if (wlen > f.value().length) {
                            //Shift
                            for (j in groupMostBigCities.child.size - 1 downTo i + 1)
                                groupMostBigCities.child[j].set(groupMostBigCities.child[j - 1].value())
                            f.set(word)
                            break
                        } else if (word == f.value())
                            break
                    } else {
                        f.set(word)
                        break
                    }
                }
            }
                .andThen(cityStatDatabaseHelper.updateFrequentWords(word, groupMostFrqCities))
                .doOnComplete { saveStats() }
    }


    //onGameEnded
    fun updateScore() {
        getWinner(timeIsUp = false, remote = false)
    }

    fun getWinner(timeIsUp: Boolean, remote: Boolean): String {
        val mode = gameSession.gameMode

        groupParts.main.increase()
        groupParts.child[mode.ordinal].increase()

        val me = getPlayer()

        groupHighScores.main.max(me.score)
        groupHighScores.child[mode.ordinal].max(me.score)

        saveStats()

        if (timeIsUp) {
            val next = gameSession.nextUser
            if (mode == GameMode.MODE_NET) {
                updWinsForNetMode(next)
                return String.format(
                    results.getValue(GameResult.TIME_UP),
                    next.name,
                    gameSession.currentUser!!.name
                )
            }
            return String.format(
                results.getValue(GameResult.TIME_UP),
                next.name,
                StringUtils.formatName(gameSession.currentUser!!.name)
            )
        }

        if (scoringType == ScoringType.LAST_MOVE) {
            if (remote) {
                updWinsForNetMode(getPlayer())
                return results.getValue(GameResult.WIN_BY_REMOTE)
            }

            //Всегда побеждает тот, кто ожидает ответа
            val next = gameSession.nextUser
            updWinsForNetMode(next)
            return String.format(results.getValue(GameResult.WIN), next.name)
        } else {

            if (gameSession.users.all { it.score == gameSession.users.first().score }) {
                return results.getValue(GameResult.DRAW)
            }

            val winner = gameSession.users.maxBy { it.score }!!
            updWinsForNetMode(winner)

            return String.format(results.getValue(GameResult.WIN), winner.name)
        }
    }

    private fun updWinsForNetMode(winner: User) {
        if (gameSession.gameMode == GameMode.MODE_NET) {
            if (getPlayer() == winner)
                groupOnline.findField(F_WINS).increase()
            else
                groupOnline.findField(F_LOSE).increase()
        }
        saveStats()
    }

    private fun currentIsPlayer() = gameSession.currentUser is Player

    private fun getPlayer(): User {
        return gameSession.users.first { it is Player }
    }
}