package br.com.conectatecnologia.conectadisparos.domain.model

import kotlinx.serialization.Serializable

enum class ContactStatus { PENDENTE, PREPARANDO, AGUARDANDO_ENVIO, ENVIADO, IGNORADO, ERRO, CANCELADO }
enum class BatchStatus { IMPORTADO, PRONTO, EM_EXECUCAO, PAUSADO, AGUARDANDO_HORARIO, EM_PAUSA_DE_BLOCO, CONCLUIDO, CANCELADO, ERRO }

@Serializable
data class BatchConfig(
    val intervaloMinimoSegundos: Int,
    val intervaloMaximoSegundos: Int,
    val quantidadePorBloco: Int,
    val pausaMinimaMinutos: Int,
    val pausaMaximaMinutos: Int,
    val limiteDiario: Int,
    val horaInicio: String,
    val horaFim: String,
    val tempoMaximoAguardandoConfirmacaoSegundos: Int,
    val maximoTentativasPorContato: Int
)

@Serializable
data class Contact(
    val id: String,
    val nome: String,
    val telefone: String,
    val status: ContactStatus = ContactStatus.PENDENTE,
    val tentativas: Int = 0,
    val erro: String? = null
)

@Serializable
data class MessageTemplate(val value: String)

@Serializable
data class Batch(
    val id: String,
    val nome: String,
    val data: String,
    val configuracao: BatchConfig,
    val mensagens: List<MessageTemplate>,
    val contatos: List<Contact>,
    val status: BatchStatus = BatchStatus.IMPORTADO
)

@Serializable
data class HistoryEvent(
    val id: Long = 0,
    val batchId: String,
    val contactId: String?,
    val telefone: String?,
    val mensagem: String?,
    val status: String,
    val tentativas: Int,
    val erro: String?,
    val bloco: Int,
    val posicao: Int,
    val timestamp: Long
)

@Serializable
data class ExecutionSummary(
    val batchId: String,
    val total: Int,
    val enviados: Int,
    val pendentes: Int,
    val erros: Int,
    val ignorados: Int,
    val posicaoAtual: Int,
    val blocoAtual: Int,
    val status: BatchStatus,
    val proximaAcao: String,
    val horarioPrevisto: Long?
)

@Serializable
data class BatchResult(
    val batchId: String,
    val status: BatchStatus,
    val total: Int,
    val enviados: Int,
    val erros: Int,
    val ignorados: Int,
    val inicio: String?,
    val fim: String?,
    val resultados: List<HistoryEvent>
)
