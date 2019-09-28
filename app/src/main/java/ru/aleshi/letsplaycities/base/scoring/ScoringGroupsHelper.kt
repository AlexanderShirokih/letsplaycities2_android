package ru.aleshi.letsplaycities.base.scoring

import ru.aleshi.letsplaycities.base.GamePreferences

object ScoringGroupsHelper {

    const val G_COMBO = "combo"
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

    const val F_QUICK_TIME = "qt"
    const val F_SHORT_WORD = "sw"
    const val F_LONG_WORD = "lw"
    const val F_SAME_COUNTRY = "sc"
    const val F_DIFF_COUNTRY = "fc"

    const val V_EMPTY_S = "--"

    private fun initStatsGroups(allGroups: ScoringSet) {
        val groupParts = ScoringGroup(
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

        val groupOnline = ScoringGroup(
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

        val groupHighScores = ScoringGroup(
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

        val groupCombo = ScoringGroup(
            ScoringField(G_COMBO),
            arrayOf(
                ScoringField(F_QUICK_TIME, 0),
                ScoringField(F_SHORT_WORD, 0),
                ScoringField(F_LONG_WORD, 0),
                ScoringField(F_SAME_COUNTRY, 0)
            )
        )
        allGroups.set(3, groupCombo)

        val groupMostFrqCities = ScoringGroup(
            ScoringField(G_FRQ_CITIES),
            Array(10) { i ->
                ScoringField(
                    F_P + i,
                    V_EMPTY_S
                )
            }
        )
        allGroups.set(4, groupMostFrqCities)

        val groupMostBigCities = ScoringGroup(
            ScoringField(G_BIG_CITIES),
            Array(10) { i ->
                ScoringField(
                    F_P + i,
                    V_EMPTY_S
                )
            }
        )
        allGroups.set(5, groupMostBigCities)
    }

    fun fromPreferences(prefs: GamePreferences): ScoringSet {
        val allGroups = ScoringSet(6)
        initStatsGroups(allGroups)

        prefs.getScoring()?.run {
            allGroups.loadFromString(this)
        }

        return allGroups
    }

}