package br.com.conectatecnologia.conectadisparos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.conectatecnologia.conectadisparos.data.local.dao.BatchDao
import br.com.conectatecnologia.conectadisparos.data.local.entity.*

@Database(entities = [BatchEntity::class, MessageEntity::class, ContactEntity::class, HistoryEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
}
