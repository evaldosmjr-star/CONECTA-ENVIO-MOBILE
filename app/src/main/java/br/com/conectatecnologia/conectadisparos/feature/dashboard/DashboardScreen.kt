package br.com.conectatecnologia.conectadisparos.feature.dashboard

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.*
import br.com.conectatecnologia.conectadisparos.domain.model.ContactStatus
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.domain.usecase.ExecutionEngine
import br.com.conectatecnologia.conectadisparos.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(repository: BatchRepository, engine: ExecutionEngine, nav: NavController) {
    val batches by repository.observeBatches().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    Screen("Conecta Disparos") {
        Text("Operacao local para lotes do Conecta Loja", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (batches.isEmpty()) {
                Text("Nenhum lote importado ainda.")
            } else {
                batches.forEach { batch ->
                    val sent = batch.contatos.count { it.status == ContactStatus.ENVIADO }
                    val pending = batch.contatos.count { it.status == ContactStatus.PENDENTE || it.status == ContactStatus.AGUARDANDO_ENVIO }
                    val errors = batch.contatos.count { it.status == ContactStatus.ERRO }
                    val ignored = batch.contatos.count { it.status == ContactStatus.IGNORADO }
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(batch.nome, style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${batch.status}")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Metric("Enviados", sent.toString(), Modifier.weight(1f))
                                Metric("Pendentes", pending.toString(), Modifier.weight(1f))
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Metric("Erros", errors.toString(), Modifier.weight(1f))
                                Metric("Ignorados", ignored.toString(), Modifier.weight(1f))
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        if (pending == 0) repository.resetBatchForResend(batch.id)
                                        engine.start(batch.id)
                                        nav.navigate(Routes.EXECUTION)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (pending == 0) "Enviar de novo" else "Continuar lote")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.resetBatchForResend(batch.id)
                                        engine.start(batch.id)
                                        nav.navigate(Routes.EXECUTION)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Reiniciar do primeiro contato") }
                        }
                    }
                }
            }
        }
        Button(onClick = { nav.navigate(Routes.IMPORT) }, Modifier.fillMaxWidth()) { Text("Importar lote") }
        OutlinedButton(onClick = { nav.navigate(Routes.HISTORY) }, Modifier.fillMaxWidth()) { Text("Historico") }
        OutlinedButton(onClick = { nav.navigate(Routes.SETTINGS) }, Modifier.fillMaxWidth()) { Text("Configuracoes") }
    }
}
