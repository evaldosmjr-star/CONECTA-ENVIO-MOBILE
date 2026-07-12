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
    Screen("Conecta Disparos") {
        Text("Operacao local para lotes do Conecta Loja", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Enviados", "0", Modifier.weight(1f))
            Metric("Pendentes", "0", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Erros", "0", Modifier.weight(1f))
            Metric("Ignorados", "0", Modifier.weight(1f))
        }
        Text("Status: sem lote ativo")
        Button(onClick = { nav.navigate(Routes.IMPORT) }, Modifier.fillMaxWidth()) { Text("Importar lote") }
        Button(onClick = { nav.navigate(Routes.EXECUTION) }, Modifier.fillMaxWidth()) { Text("Iniciar ou continuar") }
        OutlinedButton(onClick = { engine.pause() }, Modifier.fillMaxWidth()) { Text("Pausar") }
        OutlinedButton(onClick = { nav.navigate(Routes.HISTORY) }, Modifier.fillMaxWidth()) { Text("Historico") }
        OutlinedButton(onClick = { nav.navigate(Routes.SETTINGS) }, Modifier.fillMaxWidth()) { Text("Configuracoes") }
    }
}
