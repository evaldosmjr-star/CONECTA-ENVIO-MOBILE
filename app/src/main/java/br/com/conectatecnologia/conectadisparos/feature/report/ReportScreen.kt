package br.com.conectatecnologia.conectadisparos.feature.report

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository

@Composable
fun ReportScreen(repository: BatchRepository, nav: NavController) {
    Screen("Relatorio") { Text("Resumo final, taxa de conclusao e exportacao JSON ficam centralizados aqui.") }
}
