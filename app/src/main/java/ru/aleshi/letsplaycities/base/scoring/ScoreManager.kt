package ru.aleshi.letsplaycities.base.scoring

import android.content.Context
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.GamePreferences
import ru.aleshi.letsplaycities.base.game.GameMode
import ru.aleshi.letsplaycities.base.game.GameSession
import ru.aleshi.letsplaycities.base.player.Android
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.utils.StringUtils
import kotlin.math.roundToInt


class ScoreManager(private val gameSession: GameSession, private val mode: GameMode, val context: Context) {
    companion object {
        const val G_PARTS = "tt_n_pts"
        const val G_ONLINE = "tt_onl"
        const val G_HISCORE = "hscr"
        const val G_FRQ_CITIES = "mst_frq_cts"
        const val G_BIG_CITIES = "msg_big_cts"

        const val F_ANDROID = "pva"
        const val F_PLAYER = "pvp"
        const val F_NETWORK = "pvn"
        const val F_ONLINE = "pvo"
        const val F_TIME = "tim"
        const val F_WINS = "win"
        const val F_LOSE = "los"
        const val F_P = "pval"

        const val V_EMPTY_S = "--"
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
    // Самые загадываемые города
    private lateinit var groupMostFrqCities: ScoringGroup
    // Самые длинные города
    private lateinit var groupMostBigCities: ScoringGroup

    private val allGroups: ScoringSet
    private val cityStatDatabaseHelper: CityStatDatabaseHelper =
        CityStatDatabaseHelper(context)
    private val prefs: GamePreferences = (context.applicationContext as LPSApplication).gamePreferences
    private val scoringType: ScoringType = ScoringType.values()[prefs.getCurrentScoringType()]
    private var lastTime: Long = 0

    init {
        //Load or build stats
        val scrstr = prefs.getScoring()
        if (scrstr != null) {
            allGroups = ScoringSet.fromString(scrstr)
            loadStatGroups()
        } else {
            allGroups = ScoringSet(5)
            buildStatsGroups()
            saveStats()
        }
    }

    private fun loadStatGroups() {
        groupParts = allGroups.getGroupAt(G_PARTS)
        groupOnline = allGroups.getGroupAt(G_ONLINE)
        groupHighScores = allGroups.getGroupAt(G_HISCORE)
        groupMostFrqCities = allGroups.getGroupAt(G_FRQ_CITIES)
        groupMostBigCities = allGroups.getGroupAt(G_BIG_CITIES)
    }

    private fun buildStatsGroups() {

        groupParts = ScoringGroup(
            ScoringField(
                G_PARTS,
                0
            ),
            arrayOf(
                ScoringField(
                    F_ANDROID,
                    0
                ),
                ScoringField(
                    F_PLAYER,
                    0
                ),
                ScoringField(
                    F_NETWORK,
                    0
                ),
                ScoringField(
                    F_ONLINE,
                    0
                )
            )
        )
        allGroups.set(0, groupParts)

        groupOnline = ScoringGroup(
            ScoringField(G_ONLINE),
            arrayOf(
                ScoringField(
                    F_TIME,
                    0
                ),
                ScoringField(
                    F_WINS,
                    0
                ),
                ScoringField(
                    F_LOSE,
                    0
                )
            )
        )
        allGroups.set(1, groupOnline)

        groupHighScores = ScoringGroup(
            ScoringField(
                G_HISCORE,
                0
            ),
            arrayOf(
                ScoringField(
                    F_ANDROID,
                    0
                ),
                ScoringField(
                    F_PLAYER,
                    0
                ),
                ScoringField(
                    F_NETWORK,
                    0
                ),
                ScoringField(
                    F_ONLINE,
                    0
                )
            )
        )
        allGroups.set(2, groupHighScores)

        groupMostFrqCities = ScoringGroup(
            ScoringField(G_FRQ_CITIES),
            Array(10) { i ->
                ScoringField(
                    F_P + i,
                    V_EMPTY_S
                )
            }
        )
        allGroups.set(3, groupMostFrqCities)

        groupMostBigCities = ScoringGroup(
            ScoringField(G_BIG_CITIES),
            Array(10) { i ->
                ScoringField(
                    F_P + i,
                    V_EMPTY_S
                )
            }
        )
        allGroups.set(4, groupMostBigCities)
    }

    fun saveStats() {
        prefs.putScoring(allGroups.storeStatsGroups())
    }

    fun moveStarted() {
        //for by_time, save time
        lastTime = System.currentTimeMillis()
    }

    fun moveEnded(word: String) {
        val deltaTime = System.currentTimeMillis() - lastTime

        if (mode == GameMode.MODE_NET)
            groupOnline.findField(F_TIME).add(deltaTime.toInt() / 1000)
        mostChecker(word)

        lastTime += deltaTime

        var points = when (scoringType) {
            ScoringType.BY_SCORE -> word.length
            ScoringType.BY_TIME -> {
                val dt = ((40000 - deltaTime.toInt()) / 2000)
                2 + if (dt > 0) dt else 0
            }
            ScoringType.LAST_MOVE -> 0
        }

        if (gameSession.currentPlayer is Android) {
            points = (points * 0.9f).roundToInt()
        }
        gameSession.currentPlayer.score += points
    }

    private fun mostChecker(word: String) {
        if (gameSession.currentPlayer != getPlayer())
            return
        //Most biggest cities
        val wlen = word.length
        for (i in 0 until groupMostBigCities.child.size) {
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

        //Most frequent words
        cityStatDatabaseHelper.updateFrequentWords(word, groupMostFrqCities, this)

        saveStats()
    }

    //onGameEnded
    fun updateScore() {
        getWinner(timeIsUp = false, remote = false)
    }

    fun getWinner(timeIsUp: Boolean, remote: Boolean): String {
        groupParts.main.increase()
        groupParts.child[mode.ordinal].increase()

        val me = getPlayer()

        groupHighScores.main.max(me.score)
        groupHighScores.child[mode.ordinal].max(me.score)

        saveStats()

        if (timeIsUp) {
            val next = gameSession.nextPlayer
            if (mode == GameMode.MODE_NET) {
                updWinsForNetMode(next)
                return context.getString(R.string.timeup, next.name, gameSession.currentPlayer.name)
            }
            return context.getString(R.string.timeup, next.name, StringUtils.formatName(gameSession.currentPlayer.name))
        }

        if (scoringType == ScoringType.LAST_MOVE) {
            if (remote) {
                updWinsForNetMode(getPlayer())
                return context.getString(R.string.win_by_remote)
            }

            //Всегда побеждает тот, кто ожидает ответа
            val next = gameSession.nextPlayer
            updWinsForNetMode(next)
            return context.getString(R.string.win, next.name)
        } else {

            if (gameSession.players.all { it.score == gameSession.players.first().score }) {
                return context.getString(R.string.draw)
            }


            val winner = gameSession.players.maxBy { it.score }!!
            updWinsForNetMode(winner)

            return context.getString(R.string.win, winner.name)
        }
    }

    private fun updWinsForNetMode(winner: User) {
        if (mode == GameMode.MODE_NET) {
            if (getPlayer() == winner)
                groupOnline.findField(F_WINS).increase()
            else
                groupOnline.findField(F_LOSE).increase()
        }
        saveStats()
    }

    private fun getPlayer(): User {
        return gameSession.players.first { it is Player }
    }
}