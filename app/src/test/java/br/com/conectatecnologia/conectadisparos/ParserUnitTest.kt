package br.com.conectatecnologia.conectadisparos

import br.com.conectatecnologia.conectadisparos.core.util.*
import br.com.conectatecnologia.conectadisparos.data.parser.BatchParser
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ParserUnitTest {
    @Test fun normalizesPhone() { assertEquals("5599999999999", PhoneNormalizer.normalize("+55 (99) 99999-9999")) }
    @Test fun validatesBrazilianPhone() { assertTrue(PhoneNormalizer.isValidWithDdi("5599999999999")) }
    @Test fun firstName() { assertEquals("Joao", NameTools.firstName("Joao da Silva")) }
    @Test fun messageReplacement() {
        val selected = MessageSelector(Random(1)).choose(listOf("Oi {nome}", "Ola"), "Maria Souza")
        assertFalse(selected.contains("{nome}"))
    }
    @Test fun parsesValidBatch() {
        val text = javaClass.getResource("/sample-lote.json")!!.readText()
        val result = BatchParser().parse(text)
        assertTrue(result.issues.joinToString { it.message }, result.canImport)
    }
    @Test fun rejectsMalformedJson() { assertFalse(BatchParser().parse("{").canImport) }
    @Test fun rejectsInvalidHours() { assertFalse(TimeWindowValidator.validRange("18:00", "08:00")) }
}
