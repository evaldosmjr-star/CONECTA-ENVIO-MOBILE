package br.com.conectatecnologia.conectadisparos.feature.preview

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.navigation.Routes

@Composable
fun PreviewScreen(repository: BatchRepository, nav: NavController) {
    val batches by repository.observeBatches().collectAsState(initial = emptyList())
    val batch = batches.firstOrNull()
    Screen("Pre-visualizacao") {
        if (batch == null) Text("Nenhum lote importado.") else {
            Text(batch.nome, style = MaterialTheme.typography.headlineSmall)
            Text("Contatos: ${batch.contatos.size}")
            Text("Mensagens: ${batch.mensagens.size}")
            Text("Intervalo: ${batch.configuracao.intervaloMinimoSegundos}-${batch.configuracao.intervaloMaximoSegundos}s")
            Text("Bloco: ${batch.configuracao.quantidadePorBloco} contatos")
            Button(onClick = { nav.navigate(Routes.EXECUTION) }) { Text("Ir para execucao") }
        }
    }
}
