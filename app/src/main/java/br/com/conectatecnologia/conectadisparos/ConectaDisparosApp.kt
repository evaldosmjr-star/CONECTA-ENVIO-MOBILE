package br.com.conectatecnologia.conectadisparos

import android.app.Application
import androidx.room.Room
import br.com.conectatecnologia.conectadisparos.data.local.database.AppDatabase
import br.com.conectatecnologia.conectadisparos.data.repository.LocalBatchRepository
import br.com.conectatecnologia.conectadisparos.domain.usecase.ExecutionEngine
import br.com.conectatecnologia.conectadisparos.integration.macrodroid.MacroDroidBroadcaster
import br.com.conectatecnologia.conectadisparos.integration.whatsapp.WhatsAppBusinessIntentTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ConectaDisparosApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var repository: LocalBatchRepository
    lateinit var engine: ExecutionEngine

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "conecta-disparos.db").build()
        repository = LocalBatchRepository(db.batchDao())
        engine = ExecutionEngine(repository, WhatsAppBusinessIntentTransport(this), MacroDroidBroadcaster(this), appScope)
    }

    fun confirmMessage(batchId: String, contactId: String) { appScope.launch { engine.onExternalConfirmation(batchId, contactId, true) } }
    fun failMessage(batchId: String, contactId: String, error: String) { appScope.launch { engine.onExternalConfirmation(batchId, contactId, false, error) } }
    fun skipContact(batchId: String, contactId: String, reason: String) { appScope.launch { engine.skip(batchId, contactId, reason) } }
}
