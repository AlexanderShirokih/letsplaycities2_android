package ru.aleshi.letsplaycities.remote

import java.io.IOException

class LPSProtocolError(override val message: String = "LPS Protocol Error!") : IOException(message)