package br.com.conectatecnologia.conectadisparos.feature.execution

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.domain.usecase.ExecutionEngine
import kotlinx.coroutines.launch

@Composable
fun ExecutionScreen(repository: BatchRepository, engine: ExecutionEngine, nav: NavController) {
    val summary by repository.observeSummary().collectAsState(initial = null)
    val state by engine.state.collectAsState()
    val scope = rememberCoroutineScope()
    val activeBatchId = state.batchId ?: summary?.batchId
    val activeContactId = state.contactId
    Screen("Execucao") {
        Text("Lote: ${activeBatchId ?: "-"}")
        Text("Contato atual: ${activeContactId ?: "-"}")
        Text("Status: ${state.status}")
        Text("Aguardando MacroDroid: ${if (state.waitingExternalConfirmation) "sim" else "nao"}")
        Text("Ultimo erro: ${state.lastError ?: "-"}")
        Button(
            onClick = { activeBatchId?.let(engine::continueOrOpenNext) },
            enabled = activeBatchId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Abrir proximo contato") }
        Button(
            onClick = {
                val batchId = activeBatchId
                val contactId = activeContactId
                if (batchId != null && contactId != null) {
                    scope.launch { engine.onExternalConfirmation(batchId, contactId, sent = true) }
                }
            },
            enabled = activeBatchId != null && activeContactId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Confirmar enviado") }
        OutlinedButton(
            onClick = {
                val batchId = activeBatchId
                val contactId = activeContactId
                if (batchId != null && contactId != null) {
                    scope.launch { engine.onExternalConfirmation(batchId, contactId, sent = false, error = "Falha confirmada manualmente.") }
                }
            },
            enabled = activeBatchId != null && activeContactId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrar falha") }
        OutlinedButton(
            onClick = {
                val batchId = activeBatchId
                val contactId = activeContactId
                if (batchId != null && contactId != null) {
                    scope.launch { engine.skip(batchId, contactId, "Ignorado manualmente.") }
                }
            },
            enabled = activeBatchId != null && activeContactId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ignorar contato") }
        OutlinedButton(onClick = { engine.pause() }, modifier = Modifier.fillMaxWidth()) { Text("Pausar") }
        OutlinedButton(onClick = { engine.cancel() }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar lote") }
    }
}
