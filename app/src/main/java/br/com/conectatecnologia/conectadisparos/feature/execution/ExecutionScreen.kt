package br.com.conectatecnologia.conectadisparos.feature.execution

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.domain.usecase.ExecutionEngine

@Composable
fun ExecutionScreen(repository: BatchRepository, engine: ExecutionEngine, nav: NavController) {
    val summary by repository.observeSummary().collectAsState(initial = null)
    val state by engine.state.collectAsState()
    Screen("Execucao") {
        Text("Lote: ${summary?.batchId ?: "-"}")
        Text("Contato atual: ${state.contactId ?: "-"}")
        Text("Status: ${state.status}")
        Text("Aguardando MacroDroid: ${if (state.waitingExternalConfirmation) "sim" else "nao"}")
        Text("Ultimo erro: ${state.lastError ?: "-"}")
        Button(onClick = { summary?.batchId?.let(engine::start) }, enabled = summary?.batchId != null) { Text("Continuar") }
        OutlinedButton(onClick = { engine.pause() }) { Text("Pausar") }
        OutlinedButton(onClick = { engine.cancel() }) { Text("Cancelar lote") }
    }
}
