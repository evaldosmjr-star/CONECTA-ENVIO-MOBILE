package br.com.conectatecnologia.conectadisparos.domain.usecase

import br.com.conectatecnologia.conectadisparos.core.util.MessageSelector
import br.com.conectatecnologia.conectadisparos.domain.model.*
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.integration.macrodroid.MacroDroidBroadcaster
import br.com.conectatecnologia.conectadisparos.integration.whatsapp.MessageTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

data class EngineState(
    val batchId: String? = null,
    val contactId: String? = null,
    val status: BatchStatus = BatchStatus.PAUSADO,
    val waitingExternalConfirmation: Boolean = false,
    val lastError: String? = null,
    val nextActionAt: Long? = null,
    val openedAtMillis: Long? = null
)

class ExecutionEngine(
    private val repository: BatchRepository,
    private val transport: MessageTransport,
    private val broadcaster: MacroDroidBroadcaster,
    private val scope: CoroutineScope,
    private val random: Random = Random.Default
) {
    private companion object {
        const val AUTO_CONFIRM_SENT_AFTER_OPEN_MILLIS = 8_000L
        const val MAX_NEXT_CONTACT_DELAY_MILLIS = 5_000L
        const val MIN_CONFIRM_ON_RETURN_MILLIS = 2_000L
    }

    private val selector = MessageSelector(random)
    private val _state = MutableStateFlow(EngineState())
    val state: StateFlow<EngineState> = _state
    private var job: Job? = null

    fun start(batchId: String) {
        job?.cancel()
        job = scope.launch { runBatch(batchId) }
    }

    fun continueOrOpenNext(batchId: String) {
        val current = _state.value
        if (current.batchId == batchId && current.waitingExternalConfirmation && current.contactId != null) {
            confirmCurrentMessage()
        } else {
            start(batchId)
        }
    }

    fun pause() {
        val current = _state.value
        current.batchId?.let { scope.launch { repository.updateBatchStatus(it, BatchStatus.PAUSADO) } }
        _state.value = current.copy(status = BatchStatus.PAUSADO, waitingExternalConfirmation = false)
    }

    fun cancel() {
        val current = _state.value
        job?.cancel()
        current.batchId?.let { scope.launch { repository.updateBatchStatus(it, BatchStatus.CANCELADO) } }
        _state.value = current.copy(status = BatchStatus.CANCELADO, waitingExternalConfirmation = false)
    }

    fun confirmCurrentMessage() {
        val current = _state.value
        val batchId = current.batchId ?: return
        val contactId = current.contactId ?: return
        scope.launch { onExternalConfirmation(batchId, contactId, true) }
    }

    fun confirmCurrentMessageOnReturn() {
        val current = _state.value
        val openedAt = current.openedAtMillis ?: return
        if (!current.waitingExternalConfirmation) return
        if (System.currentTimeMillis() - openedAt < MIN_CONFIRM_ON_RETURN_MILLIS) return
        confirmCurrentMessage()
    }

    fun failCurrentMessage(error: String) {
        val current = _state.value
        val batchId = current.batchId ?: return
        val contactId = current.contactId ?: return
        scope.launch { onExternalConfirmation(batchId, contactId, false, error) }
    }

    fun skipCurrentContact(reason: String) {
        val current = _state.value
        val batchId = current.batchId ?: return
        val contactId = current.contactId ?: return
        scope.launch { skip(batchId, contactId, reason) }
    }

    suspend fun onExternalConfirmation(batchId: String, contactId: String, sent: Boolean, error: String? = null) {
        val batch = repository.getBatch(batchId) ?: return
        val contact = batch.contatos.firstOrNull { it.id == contactId } ?: return
        if (!_state.value.waitingExternalConfirmation || _state.value.contactId != contactId) return
        val status = if (sent) ContactStatus.ENVIADO else if (contact.tentativas + 1 >= batch.configuracao.maximoTentativasPorContato) ContactStatus.ERRO else ContactStatus.PENDENTE
        repository.updateContactStatus(batchId, contactId, status, contact.tentativas + if (sent) 0 else 1, error)
        repository.addHistory(HistoryEvent(batchId = batchId, contactId = contactId, telefone = contact.telefone, mensagem = null, status = status.name, tentativas = contact.tentativas, erro = error, bloco = 0, posicao = 0, timestamp = System.currentTimeMillis()))
        _state.value = _state.value.copy(waitingExternalConfirmation = false, lastError = error, openedAtMillis = null)
        if (sent) delay(nextContactDelayMillis(batch))
        start(batchId)
    }

    suspend fun skip(batchId: String, contactId: String, reason: String) {
        repository.updateContactStatus(batchId, contactId, ContactStatus.IGNORADO, 0, reason)
        repository.addHistory(HistoryEvent(batchId = batchId, contactId = contactId, telefone = null, mensagem = null, status = ContactStatus.IGNORADO.name, tentativas = 0, erro = reason, bloco = 0, posicao = 0, timestamp = System.currentTimeMillis()))
        start(batchId)
    }

    private fun nextContactDelayMillis(batch: Batch): Long {
        val configuredMillis = random.nextInt(
            batch.configuracao.intervaloMinimoSegundos,
            batch.configuracao.intervaloMaximoSegundos + 1
        ) * 1000L
        return configuredMillis.coerceAtMost(MAX_NEXT_CONTACT_DELAY_MILLIS)
    }

    private suspend fun runBatch(batchId: String) {
        val batch = repository.getBatch(batchId) ?: return
        repository.updateBatchStatus(batchId, BatchStatus.EM_EXECUCAO)
        _state.value = EngineState(batchId = batchId, status = BatchStatus.EM_EXECUCAO)
        val pending = batch.contatos.firstOrNull { it.status == ContactStatus.PENDENTE }
        if (pending == null) {
            repository.updateBatchStatus(batchId, BatchStatus.CONCLUIDO)
            broadcaster.batchFinished(batchId, batch.nome)
            _state.value = _state.value.copy(status = BatchStatus.CONCLUIDO)
            return
        }
        val message = selector.choose(batch.mensagens.map { it.value }, pending.nome)
        repository.updateContactStatus(batchId, pending.id, ContactStatus.AGUARDANDO_ENVIO, pending.tentativas, null)
        val opened = transport.openConversation(pending.telefone, message)
        if (opened.isSuccess) {
            broadcaster.contactOpened(batchId, pending.id, pending.nome, pending.telefone, message, batch.contatos.indexOf(pending) + 1, batch.contatos.size)
            broadcaster.waitingConfirmation(batchId, pending.id, pending.nome, pending.telefone, message, batch.contatos.indexOf(pending) + 1, batch.contatos.size)
            _state.value = _state.value.copy(contactId = pending.id, waitingExternalConfirmation = true, openedAtMillis = System.currentTimeMillis())
            delay(AUTO_CONFIRM_SENT_AFTER_OPEN_MILLIS)
            if (_state.value.waitingExternalConfirmation && _state.value.contactId == pending.id) {
                onExternalConfirmation(batchId, pending.id, sent = true)
            }
        } else {
            repository.updateContactStatus(batchId, pending.id, ContactStatus.ERRO, pending.tentativas + 1, opened.exceptionOrNull()?.message)
            _state.value = _state.value.copy(lastError = opened.exceptionOrNull()?.message)
        }
    }
}
