package br.com.conectatecnologia.conectadisparos.feature.history

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository

@Composable
fun HistoryScreen(repository: BatchRepository, nav: NavController) {
    val batches by repository.observeBatches().collectAsState(initial = emptyList())
    Screen("Historico") {
        batches.forEach { Text("${it.nome} - ${it.status} - ${it.contatos.size} contatos") }
    }
}
