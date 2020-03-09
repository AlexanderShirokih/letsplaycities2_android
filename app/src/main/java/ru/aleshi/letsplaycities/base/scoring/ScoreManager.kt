package ru.aleshi.letsplaycities.base.scoring

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.aleshi.letsplaycities.Localization
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.combos.CityComboInfo
import ru.aleshi.letsplaycities.base.game.FinishEvent
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

/**
 * Check game winner.
 * Note, that now it works only for two users.
 */
class ScoreManager @Inject constructor(
    private val prefs: GamePreferences,
    private val cityStatDatabaseHelper: CityStatDatabaseHelper,
    @Localization("score_result_names")
    private val results: Map<GameResult, String>
) {

    private lateinit var gameSession: GameSession

    enum class GameResult {
        TIME_UP,
        WIN,
        WIN_BY_REMOTE,
        DRAW,
        BANNED_BY_OPP,
        BANNED_BY_SYSTEM
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

    private lateinit var allGroups: ScoringSet

    private val scoringType: ScoringType = ScoringType.values()[prefs.getCurrentScoringType()]
    private var lastTime: Long = 0

    /**
     * Sets the current session instance and loads scoring data from preferences.
     */
    fun init(session: GameSession) {
        gameSession = session
        allGroups = ScoringGroupsHelper.fromPreferences(prefs)
        loadStatGroups()
        saveStats()
    }

    /**
     * Initializes group* variables from allGroups
     */
    private fun loadStatGroups() {
        groupCombo = allGroups.getGroupAt(G_COMBO)
        groupParts = allGroups.getGroupAt(G_PARTS)
        groupOnline = allGroups.getGroupAt(G_ONLINE)
        groupHighScores = allGroups.getGroupAt(G_HISCORE)
        groupMostFrqCities = allGroups.getGroupAt(G_FRQ_CITIES)
        groupMostBigCities = allGroups.getGroupAt(G_BIG_CITIES)
    }

    /**
     * Saves statistics in preferences.
     */
    private fun saveStats() {
        prefs.putScoring(allGroups.storeStatsGroups())
    }

    /**
     * Call when move starts
     */
    fun moveStarted() {
        // For by_time, save time
        lastTime = System.currentTimeMillis()
    }

    /**
     * Call when move ends
     */
    fun moveEnded(current: User, word: String): Completable {
        val deltaTime = System.currentTimeMillis() - lastTime

        if (gameSession.gameMode == GameMode.MODE_NET)
            groupOnline.findField(F_TIME).add(deltaTime.toInt() / 1000)

        lastTime += deltaTime

        return mostChecker(word).andThen(
            Completable.fromAction {
                val points = when (scoringType) {
                    ScoringType.LAST_MOVE -> 0
                    ScoringType.BY_SCORE -> word.length
                    ScoringType.BY_TIME -> {
                        val dt = ((40000 - deltaTime.toInt()) / 2000)
                        2 + if (dt > 0) dt else 0
                    }
                }

                current.increaseScore(points)
                saveStats()
            }
        ).andThen(checkCombos(current, deltaTime, word))
    }

    /**
     * Updates combos for [current] user.
     * @param deltaTime move duration
     * @param word current user's new word
     */
    private fun checkCombos(current: User, deltaTime: Long, word: String) =
        if (scoringType == ScoringType.LAST_MOVE)
            Completable.complete()
        else
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
                    if (isCurrentIsPlayer()) {
                        current.comboSystem.activeCombosList.forEach {
                            groupCombo.child[it.key.ordinal].max(it.value)
                        }
                    }
                })

    /**
     * Checks stats fro most-category (most biggest cities, most frequent cities)
     */
    private fun mostChecker(word: String): Completable {
        return if (!isCurrentIsPlayer())
            Completable.complete()
        else
            Completable.fromAction {
                //Most biggest cities
                val wordLength = word.length
                for (i in groupMostBigCities.child.indices) {
                    val f = groupMostBigCities.child[i]
                    if (f.hasValue() && f.value() != V_EMPTY_S) {
                        if (wordLength > f.value().length) {
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

    /**
     * Call when the game ends. Returns winning message.
     */
    fun getWinner(finishEvent: FinishEvent): String {
        val mode = gameSession.gameMode
        val me = gameSession.requirePlayer()

        groupParts.main.increase()
        groupParts.child[mode.ordinal].increase()

        groupHighScores.main.max(me.score)
        groupHighScores.child[mode.ordinal].max(me.score)

        saveStats()

        if (finishEvent.reason == FinishEvent.Reason.Kicked) {
            return results.getValue(
                if (finishEvent.target == me) GameResult.BANNED_BY_SYSTEM
                else GameResult.BANNED_BY_OPP
            )
        }

        if (finishEvent.reason == FinishEvent.Reason.TimeOut) {
            return if (mode == GameMode.MODE_NET) {
                updWinsForNetMode(gameSession.prevUser)
                String.format(
                    results.getValue(GameResult.TIME_UP),
                    gameSession.prevUser.name,
                    finishEvent.target.name
                )
            } else String.format(
                results.getValue(GameResult.TIME_UP),
                gameSession.prevUser.name,
                StringUtils.formatName(finishEvent.target.name)
            )
        }

        if (scoringType == ScoringType.LAST_MOVE) {
            if (finishEvent.reason == FinishEvent.Reason.Disconnected) {
                updWinsForNetMode(gameSession.requirePlayer())
                return results.getValue(GameResult.WIN_BY_REMOTE)
            }

            // Всегда побеждает тот, кто ожидает ответа
            val prev = gameSession.prevUser
            updWinsForNetMode(prev)
            return String.format(results.getValue(GameResult.WIN), prev.name)
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
            if (gameSession.requirePlayer() == winner)
                groupOnline.findField(F_WINS).increase()
            else
                groupOnline.findField(F_LOSE).increase()
        }
        saveStats()
    }

    private fun isCurrentIsPlayer() = gameSession.currentUser is Player

}