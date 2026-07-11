package br.com.conectatecnologia.conectadisparos.data.local.dao

import androidx.room.*
import br.com.conectatecnologia.conectadisparos.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY createdAt DESC")
    fun observeBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun batch(id: String): BatchEntity?

    @Query("SELECT * FROM batches WHERE status IN ('EM_EXECUCAO','PAUSADO','AGUARDANDO_HORARIO','EM_PAUSA_DE_BLOCO','PRONTO') ORDER BY updatedAt DESC LIMIT 1")
    fun observeActiveBatch(): Flow<BatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBatch(entity: BatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(items: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContacts(items: List<ContactEntity>)

    @Query("SELECT * FROM messages WHERE batchId = :batchId ORDER BY position")
    suspend fun messages(batchId: String): List<MessageEntity>

    @Query("SELECT * FROM contacts WHERE batchId = :batchId ORDER BY rowid")
    suspend fun contacts(batchId: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE batchId = :batchId AND id = :contactId LIMIT 1")
    suspend fun contact(batchId: String, contactId: String): ContactEntity?

    @Query("UPDATE contacts SET status = :status, tentativas = :attempts, erro = :error, updatedAt = :updatedAt WHERE batchId = :batchId AND id = :contactId")
    suspend fun updateContact(batchId: String, contactId: String, status: String, attempts: Int, error: String?, updatedAt: Long)

    @Query("UPDATE batches SET status = :status, updatedAt = :updatedAt WHERE id = :batchId")
    suspend fun updateBatchStatus(batchId: String, status: String, updatedAt: Long)

    @Insert
    suspend fun addHistory(entity: HistoryEntity)

    @Query("SELECT * FROM history WHERE batchId = :batchId ORDER BY timestamp")
    suspend fun history(batchId: String): List<HistoryEntity>

    @Query("DELETE FROM contacts WHERE batchId = :batchId")
    suspend fun deleteContacts(batchId: String)

    @Query("DELETE FROM messages WHERE batchId = :batchId")
    suspend fun deleteMessages(batchId: String)

    @Query("DELETE FROM history WHERE batchId = :batchId")
    suspend fun deleteHistory(batchId: String)

    @Query("DELETE FROM batches WHERE id = :batchId")
    suspend fun deleteBatch(batchId: String)
}
