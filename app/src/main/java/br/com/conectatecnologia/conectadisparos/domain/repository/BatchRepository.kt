package br.com.conectatecnologia.conectadisparos.domain.repository

import br.com.conectatecnologia.conectadisparos.domain.model.*
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    fun observeSummary(): Flow<ExecutionSummary?>
    fun observeBatches(): Flow<List<Batch>>
    suspend fun getBatch(batchId: String): Batch?
    suspend fun isBatchImported(batchId: String): Boolean
    suspend fun importBatch(batch: Batch, replaceExisting: Boolean = false)
    suspend fun updateContactStatus(batchId: String, contactId: String, status: ContactStatus, attempts: Int, error: String? = null)
    suspend fun updateBatchStatus(batchId: String, status: BatchStatus)
    suspend fun addHistory(event: HistoryEvent)
    suspend fun history(batchId: String): List<HistoryEvent>
    suspend fun deleteBatch(batchId: String, keepReport: Boolean)
}
