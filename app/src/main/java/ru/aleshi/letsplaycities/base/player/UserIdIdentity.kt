package ru.aleshi.letsplaycities.base.player

/**
 * Compares users by [userId]
 * @param userId user ID to be compared with
 */
class UserIdIdentity(val userId: Int) : UserIdentity {

    /**
     * Returns `true` is [user] userID equals with [userId], `false` otherwise
     * @param user user for comparison
     * @return `true` is [user] userID equals with [userId], `false` otherwise
     */
    override fun isTheSameUser(user: User): Boolean = userId == user.credentials.userId

}