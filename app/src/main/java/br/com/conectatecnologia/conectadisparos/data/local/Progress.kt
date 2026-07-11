package br.com.conectatecnologia.conectadisparos.data.local

data class ProgressSnapshot(val batchId: String, val contactId: String?, val position: Int, val blockPosition: Int, val remainingMillis: Long)
