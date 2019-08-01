package ru.aleshi.letsplaycities.base.dictionary

class UpdateRequest {
    var dictionary: Dictionary = Dictionary()

    inner class Dictionary {
        var version: Int = 0
    }
}