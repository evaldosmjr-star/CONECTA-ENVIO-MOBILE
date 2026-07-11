package br.com.conectatecnologia.conectadisparos.core.util

import kotlin.random.Random

object PhoneNormalizer {
    fun normalize(raw: String): String = raw.filter { it.isDigit() }
    fun isValidWithDdi(phone: String): Boolean {
        if (phone.length < 10 || phone.length > 15) return false
        if (!phone.all { it.isDigit() }) return false
        return if (phone.startsWith("55")) phone.length in 12..13 else phone.length in 10..15
    }
}

object NameTools {
    fun firstName(name: String): String = name.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
}

class MessageSelector(private val random: Random = Random.Default) {
    private var last: String? = null
    fun choose(messages: List<String>, name: String): String {
        val first = NameTools.firstName(name)
        val usable = if (first.isBlank()) messages.filterNot { it.contains("{nome}") } else messages
        require(usable.isNotEmpty()) { "Nenhuma mensagem valida para contato sem nome." }
        val pool = usable.filter { it != last }.ifEmpty { usable }
        val selected = pool[random.nextInt(pool.size)]
        last = selected
        return selected.replace("{nome}", first).replace(Regex("\\s+"), " ").trim()
    }
}

object TimeWindowValidator {
    fun validHour(value: String): Boolean = Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(value)
    fun validRange(start: String, end: String): Boolean = validHour(start) && validHour(end) && start < end
}
