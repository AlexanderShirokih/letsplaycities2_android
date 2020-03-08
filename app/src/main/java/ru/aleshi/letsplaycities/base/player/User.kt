package ru.aleshi.letsplaycities.base.player

import io.reactivex.Observable
import ru.aleshi.letsplaycities.base.combos.ComboSystem
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.game.GameFacade
import ru.aleshi.letsplaycities.base.game.PictureSource
import ru.aleshi.letsplaycities.base.game.Position
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.quandastudio.lpsclient.model.PlayerData

/**
 * Base class that keeps users data and defines user behaviour.
 * @property playerData users data model class
 * @property pictureSource represents users picture
 */
abstract class User(
    private val playerData: PlayerData,
    private val pictureSource: PictureSource
) {

    /**
     * Delegate for image request
     */
    val imageRequest = pictureSource.imageRequest

    /**
     * Delegate for user credentials
     */
    val credentials = playerData.authData.credentials

    /**
     * Delegate for user name
     */
    val name = playerData.authData.login

    /**
     * Current user position
     */
    var position: Position = Position.UNKNOWN
        internal set

    /**
     * System that can calculate score multiplier
     */
    lateinit var comboSystem: ComboSystem
        private set

    /**
     * Facade for access game fields and methods such as game dictionary
     */
    protected lateinit var game: GameFacade

    /**
     * Returns `true` is this user allows to receive messages from other users, `false` otherwise
     */
    val isMessagesAllowed = playerData.canReceiveMessages

    /**
     * User score points
     */
    var score: Int = 0
        internal set

    /**
     * Formatted string representation of score and user name
     */
    val info: String
        get() = if (score == 0) name else "$name: $score"

    /**
     * Called by system when game starts to initialize internal state.
     */
    fun init(comboSystemView: ComboSystemView, game: GameFacade) {
        this.game = game
        this.comboSystem = onInit(comboSystemView)
    }

    /**
     * Called internally when game starts to initialize internal state.
     */
    internal abstract fun onInit(comboSystemView: ComboSystemView): ComboSystem

    /**
     * Called by system when users turn begins
     * @param firstChar first letter of that the city should begin.
     * Will be [Char.MIN_VALUE] if it's should be the first word in game.
     * @return [Observable] with word response [ResultWithCity]. To finish move, [User]
     * should emit `onComplete` event
     */
    internal abstract fun onMakeMove(firstChar: Char): Observable<ResultWithCity>

    /**
     * Called by system to increase score points.
     * @param [points] amount of points to be increased. [points] will be multiplied
     * by current combo multiplier.
     */
    fun increaseScore(points: Int) {
        score += (points * comboSystem.multiplier).toInt()
    }
}