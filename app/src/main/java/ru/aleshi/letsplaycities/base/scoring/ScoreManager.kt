package ru.aleshi.letsplaycities.base.scoring

import android.content.Context
import ru.aleshi.letsplaycities.LPSApplication
import ru.aleshi.letsplaycities.R
import ru.aleshi.letsplaycities.base.combos.CityComboInfo
import ru.aleshi.letsplaycities.base.combos.ComboSystem
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

class ScoreManager(
    private val gameSession: GameSession,
    private val mode: GameMode,
    private val comboSystem: ComboSystem,
    val context: Context
) {

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


    private val allGroups: ScoringSet
    private val cityStatDatabaseHelper = CityStatDatabaseHelper(context)
    private val prefs = (context.applicationContext as LPSApplication).gamePreferences
    private val scoringType: ScoringType = ScoringType.values()[prefs.getCurrentScoringType()]
    private var lastTime: Long = 0

    init {
        allGroups = ScoringGroupsHelper.fromPreferences(prefs)
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

    fun moveEnded(word: String) {
        val deltaTime = System.currentTimeMillis() - lastTime

        if (mode == GameMode.MODE_NET)
            groupOnline.findField(F_TIME).add(deltaTime.toInt() / 1000)
        mostChecker(word)


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
        gameSession.currentPlayer.score += (points * comboSystem.multiplier).toInt()

        saveStats()
    }

    private fun checkCombos(deltaTime: Long, word: String) {
        if (currentIsPlayer()) {
            comboSystem.addCity(
                CityComboInfo.create(
                    deltaTime,
                    word,
                    gameSession.dictionary().getCountryCode(word)
                )
            )
            comboSystem.activeCombosList.forEach {
                groupCombo.child[it.key.ordinal].max(it.value)
            }
        }
    }

    private fun mostChecker(word: String) {
        if (!currentIsPlayer())
            return
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

        //Most frequent words
        cityStatDatabaseHelper.updateFrequentWords(word, groupMostFrqCities, this)
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
            return context.getString(
                R.string.timeup,
                next.name,
                StringUtils.formatName(gameSession.currentPlayer.name)
            )
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

    private fun currentIsPlayer() = gameSession.currentPlayer == getPlayer()

    private fun getPlayer(): User {
        return gameSession.players.first { it is Player }
    }
}