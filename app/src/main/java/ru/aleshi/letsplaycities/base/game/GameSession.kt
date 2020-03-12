package ru.aleshi.letsplaycities.base.game

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.aleshi.letsplaycities.base.combos.ComboSystemView
import ru.aleshi.letsplaycities.base.player.Player
import ru.aleshi.letsplaycities.base.player.User
import ru.aleshi.letsplaycities.base.player.UserIdIdentity
import ru.aleshi.letsplaycities.base.player.UserIdentity
import ru.aleshi.letsplaycities.base.server.BaseServer
import ru.aleshi.letsplaycities.base.server.ResultWithCity
import ru.aleshi.letsplaycities.base.server.ResultWithMessage
import ru.aleshi.letsplaycities.utils.StringUtils

class GameSession(
    val users: Array<User>,
    val server: BaseServer,
    val gameMode: GameMode
) {

    init {
        users.forEachIndexed { i, user ->
            user.position = Position.values()[i]
        }
    }

    /**
     * Facade containing access to dictionary and other game fields.
     */
    lateinit var game: GameFacade
        private set

    /**
     * Returns index of next [User] in array [users]. Index looped in range 0..users.size.
     */
    private val nextIndex: Int
        get() = (_currentUserIndex + 1) % users.size

    /**
     * Keeps index of current user
     */
    private var _currentUserIndex = 0

    /**
     * Returns current [User] or `null` if current user not defined yet.
     */
    val currentUser: User
        get() = users[_currentUserIndex]

    /**
     * Returns previous [User] in queue
     */
    val prevUser: User
        get() = users[(_currentUserIndex + 1) % users.size]

    /**
     * Last accepted word
     */
    private var lastWord: String = ""

    /**
     * Used to redirect player's messages to [getIncomingMessages].
     */
    private val playerMessageSubject = PublishSubject.create<ResultWithMessage>()

    /**
     * Should be called on start to init all [User]s.
     * @param comboSystemView single [ComboSystemView] for [Player]
     * @param game [GameFacade]
     */
    fun start(comboSystemView: ComboSystemView, game: GameFacade) {
        lastWord = ""
        _currentUserIndex = 0
        this.game = game
        users.forEach { user -> user.init(comboSystemView, game) }
    }

    /**
     * Call to start game turn for current user.
     */
    fun makeMoveForCurrentUser(): Observable<ResultWithCity> =
        Observable.just(0L)
            .flatMap { currentUser.onMakeMove(StringUtils.findLastSuitableChar(lastWord)) }
            .doOnNext {
                if (it.isSuccessful()) {
                    lastWord = it.city
                    _currentUserIndex = nextIndex
                }
            }
            .mergeWith(server.getDisconnections().map {
                it.takeIf { msg -> msg.ownerId == 0 }
                    ?: it.copy(ownerId = requirePlayer().credentials.userId)
            }
                .flatMap {
                    findUserByIdentity(UserIdIdentity(it.ownerId))?.run {
                        Observable.error<ResultWithCity>(
                            SurrenderException(this, !it.leaved)
                        )
                    } ?: Observable.never()
                })

    /**
     * Called by system when user enters an input.
     * Subclasses should override this method to receive keyboard events from user.
     * @return [Observable.empty] when user can't handle input. [Observable] with [WordCheckingResult] when
     * user handles the input.
     */
    fun sendPlayersInput(userInput: String): Observable<WordCheckingResult> =
        if (currentUser is Player) (currentUser as Player).onUserInput(userInput)
        else Observable.empty()

    /**
     * Called by system when user enters input message.
     */
    fun sendPlayersMessage(message: String): Completable = requirePlayer().run {
        server.sendMessage(message, this).doOnComplete {
            playerMessageSubject.onNext(ResultWithMessage(message, UserIdIdentity(this)))
        }
    }

    /**
     * Finds associated user by its [userIdentity].
     * @param userIdentity wanted identity
     * @return [User] associated with [userIdentity] or `null` when its not found.
     */
    fun findUserByIdentity(userIdentity: UserIdentity): User? =
        users.firstOrNull { userIdentity.isTheSameUser(it) }

    /**
     * Checks whether current game mode local or not
     * @return `true` if current game mode is PVA or PVP, `false` otherwise.
     */
    fun isLocal() = gameMode == GameMode.MODE_PVA || gameMode == GameMode.MODE_PVP

    /**
     * Returns `true` if all users in this game allows sending messages
     * and current game mode is not local.
     */
    fun isMessagesAllowed(): Boolean = !isLocal() && users.all { it.isMessagesAllowed }

    /**
     * Returns [Observable] of messages from all users.
     */
    fun getIncomingMessages(): Observable<ResultWithMessage> =
        server.getIncomingMessages().filter { isMessagesAllowed() }.mergeWith(playerMessageSubject)

    /**
     * Returns [Player] among [users].
     * @throws NoSuchElementException if [Player] wasn't found in [users] list.
     */
    fun requirePlayer() = users.first { it is Player } as Player

    /**
     * Emits random word from player only if it current player now
     */
    fun useHintForPlayer(): Completable =
        requirePlayer().takeIf { it == currentUser }?.useHint(game) ?: Completable.complete()

/*
            server.leave.subscribe({ leaved ->
                if (leaved) showToastAndDisconnect(R.string.player_leaved, false)
                else showToastAndDisconnect(R.string.opp_disconnect, false)
            }, view::showError, {
                showToastAndDisconnect(R.string.lost_connection, false)
            })
*/

}