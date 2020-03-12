package ru.aleshi.letsplaycities.base.player

/**
 * Interface that used for comparing users
 */
interface UserIdentity {

    /**
     * Compares this user identity with other.
     * @param another another user identity to be compared with
     * @return `true` is this user identity considers the same, `false` otherwise.
     */
    fun isTheSameUser(user: User): Boolean

}