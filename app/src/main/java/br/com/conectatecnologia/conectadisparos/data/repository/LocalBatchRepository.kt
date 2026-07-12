package br.com.conectatecnologia.conectadisparos.data.repository

import br.com.conectatecnologia.conectadisparos.data.local.dao.BatchDao
import br.com.conectatecnologia.conectadisparos.data.local.entity.*
import br.com.conectatecnologia.conectadisparos.domain.model.*
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalBatchRepository(private val dao: BatchDao, private val json: Json = Json) : BatchRepository {
    override fun observeSummary(): Flow<ExecutionSummary?> = dao.observeActiveBatch().map { entity ->
        entity?.let { batch ->
            val contacts = dao.contacts(batch.id)
            val sent = contacts.count { it.status == ContactStatus.ENVIADO.name }
            val errors = contacts.count { it.status == ContactStatus.ERRO.name }
            val ignored = contacts.count { it.status == ContactStatus.IGNORADO.name }
            val pending = contacts.count { it.status == ContactStatus.PENDENTE.name }
            ExecutionSummary(batch.id, contacts.size, sent, pending, errors, ignored, sent + errors + ignored + 1, 1, BatchStatus.valueOf(batch.status), "Aguardando acao", null)
        }
    }

    override fun observeBatches(): Flow<List<Batch>> = dao.observeBatches().map { entities -> entities.mapNotNull { getBatch(it.id) } }
    override suspend fun getBatch(batchId: String): Batch? {
        val batch = dao.batch(batchId) ?: return null
        val config = json.decodeFromString<BatchConfig>(batch.configJson)
        val messages = dao.messages(batchId).map { MessageTemplate(it.value) }
        val contacts = dao.contacts(batchId).map { Contact(it.id, it.nome, it.telefone, ContactStatus.valueOf(it.status), it.tentativas, it.erro) }
        return Batch(batch.id, batch.nome, batch.data, config, messages, contacts, BatchStatus.valueOf(batch.status))
    }

    override suspend fun isBatchImported(batchId: String): Boolean = dao.batch(batchId) != null
    override suspend fun importBatch(batch: Batch, replaceExisting: Boolean) {
        if (!replaceExisting && isBatchImported(batch.id)) error("Lote ja importado.")
        val now = System.currentTimeMillis()
        dao.upsertBatch(BatchEntity(batch.id, batch.nome, batch.data, batch.status.name, json.encodeToString(batch.configuracao), now, now))
        dao.upsertMessages(batch.mensagens.mapIndexed { index, message -> MessageEntity(batch.id, index, message.value) })
        dao.upsertContacts(batch.contatos.map { ContactEntity(batch.id, it.id, it.nome, it.telefone, it.status.name, it.tentativas, it.erro, now) })
    }

    override suspend fun updateContactStatus(batchId: String, contactId: String, status: ContactStatus, attempts: Int, error: String?) =
        dao.updateContact(batchId, contactId, status.name, attempts, error, System.currentTimeMillis())

    override suspend fun updateBatchStatus(batchId: String, status: BatchStatus) = dao.updateBatchStatus(batchId, status.name, System.currentTimeMillis())
    override suspend fun resetBatchForResend(batchId: String) {
        val now = System.currentTimeMillis()
        dao.resetContacts(batchId, ContactStatus.PENDENTE.name, now)
        dao.updateBatchStatus(batchId, BatchStatus.PRONTO.name, now)
    }

    override suspend fun addHistory(event: HistoryEvent) = dao.addHistory(HistoryEntity(event.id, event.batchId, event.contactId, event.telefone, event.mensagem, event.status, event.tentativas, event.erro, event.bloco, event.posicao, event.timestamp))
    override suspend fun history(batchId: String): List<HistoryEvent> = dao.history(batchId).map { HistoryEvent(it.id, it.batchId, it.contactId, it.telefone, it.mensagem, it.status, it.tentativas, it.erro, it.bloco, it.posicao, it.timestamp) }
    override suspend fun deleteBatch(batchId: String, keepReport: Boolean) {
        dao.deleteContacts(batchId); dao.deleteMessages(batchId); if (!keepReport) dao.deleteHistory(batchId); dao.deleteBatch(batchId)
    }
}
