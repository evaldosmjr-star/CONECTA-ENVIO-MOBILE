package br.com.conectatecnologia.conectadisparos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val data: String,
    val status: String,
    val configJson: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "messages", primaryKeys = ["batchId", "position"])
data class MessageEntity(val batchId: String, val position: Int, val value: String)

@Entity(tableName = "contacts", primaryKeys = ["batchId", "id"])
data class ContactEntity(
    val batchId: String,
    val id: String,
    val nome: String,
    val telefone: String,
    val status: String,
    val tentativas: Int,
    val erro: String?,
    val updatedAt: Long
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
