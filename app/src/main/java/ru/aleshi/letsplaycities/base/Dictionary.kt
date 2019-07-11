package ru.aleshi.letsplaycities.base

class Dictionary {
    enum class CityResult {
        OK, CITY_NOT_FOUND, ALREADY_USED
    }


    fun checkCity(city: String): Pair<String, CityResult> {
        //TODO:
        return city to CityResult.OK
    }
}