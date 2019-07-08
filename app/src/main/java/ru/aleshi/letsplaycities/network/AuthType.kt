package ru.aleshi.letsplaycities.network

enum class AuthType constructor(var snName: String) {

    nv("nv"), gl("gl"), vk("vk"), ok("ok"), fb("fb");

    fun type(): String {
        return snName
    }
}