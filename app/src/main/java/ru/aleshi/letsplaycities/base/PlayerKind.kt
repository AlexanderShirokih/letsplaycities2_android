package ru.aleshi.letsplaycities.base


import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlayerKind(val kind: Type) {

    enum class Type {
        ANDROID,
        USER_PLAYER,
        LOCAL_PLAYER_1,
        LOCAL_PLAYER_2,

    }

}