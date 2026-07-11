package br.com.conectatecnologia.conectadisparos.data.parser

import br.com.conectatecnologia.conectadisparos.core.util.PhoneNormalizer
import br.com.conectatecnologia.conectadisparos.core.util.TimeWindowValidator
import br.com.conectatecnologia.conectadisparos.domain.model.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class ValidationIssue(val field: String, val message: String, val blocking: Boolean = true)
data class ParseResult(val batch: Batch?, val issues: List<ValidationIssue>) {
    val canImport: Boolean get() = batch != null && issues.none { it.blocking }
}

@Serializable private data class BatchDto(
    val id: String = "",
    val nome: String = "",
    val data: String = "",
    val configuracao: ConfigDto = ConfigDto(),
    val mensagens: List<String> = emptyList(),
    val contatos: List<ContactDto> = emptyList()
)

@Serializable private data class ConfigDto(
    val intervaloMinimoSegundos: Int = 0,
    val intervaloMaximoSegundos: Int = 0,
    val quantidadePorBloco: Int = 0,
    val pausaMinimaMinutos: Int = 0,
    val pausaMaximaMinutos: Int = 0,
    val limiteDiario: Int = 0,
    val horaInicio: String = "",
    val horaFim: String = "",
    val tempoMaximoAguardandoConfirmacaoSegundos: Int = 0,
    val maximoTentativasPorContato: Int = 0
)

@Serializable private data class ContactDto(val id: String = "", val nome: String = "", val telefone: String = "")

class BatchParser(private val json: Json = Json { ignoreUnknownKeys = true }) {
    fun parse(content: String, alreadyImported: Boolean = false, allowReplace: Boolean = false): ParseResult {
        val dto = try { json.decodeFromString<BatchDto>(content) } catch (e: Exception) {
            return ParseResult(null, listOf(ValidationIssue("json", "JSON malformado ou incompativel: ${e.message}")))
        }
        val issues = mutableListOf<ValidationIssue>()
        if (dto.id.isBlank()) issues += ValidationIssue("id", "Identificador do lote obrigatorio.")
        if (dto.nome.isBlank()) issues += ValidationIssue("nome", "Nome do lote obrigatorio.")
        if (dto.mensagens.isEmpty()) issues += ValidationIssue("mensagens", "Informe ao menos uma mensagem.")
        if (dto.contatos.isEmpty()) issues += ValidationIssue("contatos", "Informe ao menos um contato.")
        if (alreadyImported && !allowReplace) issues += ValidationIssue("id", "Lote ja importado. Confirme substituicao para importar novamente.")
        validateConfig(dto.configuracao, issues)

        val phones = mutableSetOf<String>()
        val ids = mutableSetOf<String>()
        val contacts = dto.contatos.mapIndexedNotNull { index, item ->
            val phone = PhoneNormalizer.normalize(item.telefone)
            if (item.id.isBlank()) issues += ValidationIssue("contatos[$index].id", "Contato sem id.")
            if (item.nome.isBlank()) issues += ValidationIssue("contatos[$index].nome", "Contato sem nome.")
            if (!PhoneNormalizer.isValidWithDdi(phone)) issues += ValidationIssue("contatos[$index].telefone", "Telefone invalido ou sem DDI: ${item.telefone}")
            if (!phones.add(phone)) issues += ValidationIssue("contatos[$index].telefone", "Telefone duplicado no lote: $phone", blocking = false)
            if (item.id.isNotBlank() && !ids.add(item.id)) issues += ValidationIssue("contatos[$index].id", "Contato repetido: ${item.id}", blocking = false)
            if (item.id.isBlank() || item.nome.isBlank() || !PhoneNormalizer.isValidWithDdi(phone)) null else Contact(item.id.trim(), item.nome.trim(), phone)
        }
        val batch = if (issues.any { it.blocking }) null else Batch(
            id = dto.id.trim(),
            nome = dto.nome.trim(),
            data = dto.data.trim(),
            configuracao = BatchConfig(
                dto.configuracao.intervaloMinimoSegundos,
                dto.configuracao.intervaloMaximoSegundos,
                dto.configuracao.quantidadePorBloco,
                dto.configuracao.pausaMinimaMinutos,
                dto.configuracao.pausaMaximaMinutos,
                dto.configuracao.limiteDiario,
                dto.configuracao.horaInicio,
                dto.configuracao.horaFim,
                dto.configuracao.tempoMaximoAguardandoConfirmacaoSegundos,
                dto.configuracao.maximoTentativasPorContato
            ),
            mensagens = dto.mensagens.map { MessageTemplate(it.trim()) }.filter { it.value.isNotBlank() },
            contatos = contacts,
            status = BatchStatus.PRONTO
        )
        return ParseResult(batch, issues)
    }

    private fun validateConfig(config: ConfigDto, issues: MutableList<ValidationIssue>) {
        if (config.intervaloMinimoSegundos <= 0) issues += ValidationIssue("configuracao.intervaloMinimoSegundos", "Intervalo minimo deve ser maior que zero.")
        if (config.intervaloMaximoSegundos < config.intervaloMinimoSegundos) issues += ValidationIssue("configuracao.intervaloMaximoSegundos", "Intervalo maximo deve ser maior ou igual ao minimo.")
        if (config.quantidadePorBloco <= 0) issues += ValidationIssue("configuracao.quantidadePorBloco", "Quantidade por bloco deve ser maior que zero.")
        if (config.pausaMaximaMinutos < config.pausaMinimaMinutos) issues += ValidationIssue("configuracao.pausaMaximaMinutos", "Pausa maxima deve ser maior ou igual a minima.")
        if (config.limiteDiario <= 0) issues += ValidationIssue("configuracao.limiteDiario", "Limite diario deve ser maior que zero.")
        if (!TimeWindowValidator.validRange(config.horaInicio, config.horaFim)) issues += ValidationIssue("configuracao.horarios", "Horario inicial e final invalidos.")
        if (config.tempoMaximoAguardandoConfirmacaoSegundos <= 0) issues += ValidationIssue("configuracao.confirmacao", "Tempo maximo de confirmacao deve ser maior que zero.")
        if (config.maximoTentativasPorContato <= 0) issues += ValidationIssue("configuracao.tentativas", "Maximo de tentativas deve ser maior que zero.")
    }
}
