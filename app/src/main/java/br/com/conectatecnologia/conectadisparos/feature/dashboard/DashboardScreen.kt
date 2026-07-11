package br.com.conectatecnologia.conectadisparos.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.*
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.domain.usecase.ExecutionEngine
import br.com.conectatecnologia.conectadisparos.navigation.Routes

@Composable
fun DashboardScreen(repository: BatchRepository, engine: ExecutionEngine, nav: NavController) {
    val summary by repository.observeSummary().collectAsState(initial = null)
    Screen("Conecta Disparos") {
        Text("Operacao local para lotes do Conecta Loja", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Enviados", summary?.enviados?.toString() ?: "0", Modifier.weight(1f))
            Metric("Pendentes", summary?.pendentes?.toString() ?: "0", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Erros", summary?.erros?.toString() ?: "0", Modifier.weight(1f))
            Metric("Ignorados", summary?.ignorados?.toString() ?: "0", Modifier.weight(1f))
        }
        Text("Status: ${summary?.status ?: "sem lote ativo"}")
        Button(onClick = { nav.navigate(Routes.IMPORT) }, Modifier.fillMaxWidth()) { Text("Importar lote") }
        Button(onClick = { summary?.batchId?.let(engine::start); nav.navigate(Routes.EXECUTION) }, Modifier.fillMaxWidth(), enabled = summary?.batchId != null) { Text("Iniciar ou continuar") }
        OutlinedButton(onClick = { engine.pause() }, Modifier.fillMaxWidth()) { Text("Pausar") }
        OutlinedButton(onClick = { nav.navigate(Routes.HISTORY) }, Modifier.fillMaxWidth()) { Text("Historico") }
        OutlinedButton(onClick = { nav.navigate(Routes.SETTINGS) }, Modifier.fillMaxWidth()) { Text("Configuracoes") }
    }
}
