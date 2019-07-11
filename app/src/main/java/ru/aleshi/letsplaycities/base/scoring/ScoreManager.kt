package ru.aleshi.letsplaycities.base.scoring

import android.content.Context
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.*
import ru.aleshi.letsplaycities.base.player.Android
import ru.aleshi.letsplaycities.base.player.NetworkUser
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.player.RemoteUser
import ru.aleshi.letsplaycities.utils.Utils
import kotlin.math.roundToInt


class ScoreManager(private val scoringType: ScoringType, private val mode: GameMode, val context: Context) {
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

        const val V_EMTPY_S = "--"
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

    private var lastTime: Long = 0
    private lateinit var playerA: User
    private lateinit var playerB: User
    private lateinit var currentPlayer: User

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
                    V_EMTPY_S
                )
            }
        )
        allGroups.set(3, groupMostFrqCities)

        groupMostBigCities = ScoringGroup(
            ScoringField(G_BIG_CITIES),
            Array(10) { i ->
                ScoringField(
                    F_P + i,
                    V_EMTPY_S
                )
            }
        )
        allGroups.set(4, groupMostBigCities)
    }

    fun saveStats() {
        prefs.putScoring(allGroups.storeStatsGroups())
    }


    fun reset(first: User, second: User) {
        playerA = first
        playerB = second
    }

    fun moveStarted(current: User) {
        //for by_time, save time
        lastTime = System.currentTimeMillis()
        this.currentPlayer = current
    }

    fun moveEnded(word: String) {
        val deltaTime = System.currentTimeMillis() - lastTime

        if (mode == GameMode.MODE_NET)
            groupOnline.findField(F_TIME).add(deltaTime.toInt() / 1000)
        mostChecker(word)

        lastTime += deltaTime

        var points = 0

        when (scoringType) {
            ScoringType.BY_SCORE -> points = word.length
            ScoringType.BY_TIME -> {
                val dt = ((40000 - deltaTime.toInt()) / 2000)
                points = 2 + if (dt > 0) dt else 0
            }
            ScoringType.LAST_MOVE -> {
            }
        }

        if (currentPlayer is Android) {
            points = (points * 0.9f).roundToInt()
        }
        currentPlayer.score += points
    }

    private fun mostChecker(word: String) {
        if (currentPlayer !== getPlayer())
            return
        //Most biggest cities
        val wlen = word.length
        for (i in 0 until groupMostBigCities.child.size) {
            val f = groupMostBigCities.child[i]
            if (f.hasValue() && f.value() != V_EMTPY_S) {
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
            val opp = getOpp(currentPlayer)
            if (mode == GameMode.MODE_NET) {
                updWinsForNetMode(opp)
                return context.getString(R.string.timeup, opp.name, currentPlayer.name)
            }
            return context.getString(R.string.timeup, opp.name, Utils.formatName(currentPlayer.name))
        }

        if (scoringType === ScoringType.LAST_MOVE) {
            if (remote) {
                updWinsForNetMode(getPlayer())
                return context.getString(R.string.win_by_remote)
            }

            //Всегда побеждает тот, кто ожидает ответа
            val opp = getOpp(currentPlayer)
            updWinsForNetMode(opp)
            return context.getString(R.string.win, opp.name)
        } else {

            if (playerA.score == playerB.score) {
                return context.getString(R.string.draw)
            }

            val winner = if (playerA.score > playerB.score) playerA else playerB
            updWinsForNetMode(winner)

            return context.getString(R.string.win, winner.name)
        }
    }

    private fun updWinsForNetMode(winner: User) {
        if (mode == GameMode.MODE_NET) {
            if (getPlayer() === winner)
                groupOnline.findField(F_WINS).increase()
            else
                groupOnline.findField(F_LOSE).increase()
        }
        saveStats()
    }

    private fun getPlayer(): User {
        return when (mode) {
            GameMode.MODE_PVA -> if (playerA is Android) playerB else playerA
            GameMode.MODE_PVP -> if (playerA.score > playerB.score) playerA else playerB
            GameMode.MODE_MUL -> if (playerA is RemoteUser) playerB else playerA
            GameMode.MODE_NET -> if (playerA is NetworkUser) playerB else playerA
        }
    }

    private fun getOpp(p: User): User {
        return if (p === playerA) playerB else playerA
    }
}