package br.com.conectatecnologia.conectadisparos.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.domain.usecase.ExecutionEngine
import br.com.conectatecnologia.conectadisparos.feature.dashboard.DashboardScreen
import br.com.conectatecnologia.conectadisparos.feature.execution.ExecutionScreen
import br.com.conectatecnologia.conectadisparos.feature.history.HistoryScreen
import br.com.conectatecnologia.conectadisparos.feature.importbatch.ImportBatchScreen
import br.com.conectatecnologia.conectadisparos.feature.preview.PreviewScreen
import br.com.conectatecnologia.conectadisparos.feature.report.ReportScreen
import br.com.conectatecnologia.conectadisparos.feature.settings.SettingsScreen

object Routes { const val DASHBOARD = "dashboard"; const val IMPORT = "import"; const val PREVIEW = "preview"; const val EXECUTION = "execution"; const val HISTORY = "history"; const val REPORT = "report"; const val SETTINGS = "settings"; const val ABOUT = "about" }

@Composable
fun ConectaNavHost(repository: BatchRepository, engine: ExecutionEngine) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) { DashboardScreen(repository, engine, nav) }
        composable(Routes.IMPORT) { ImportBatchScreen(repository, nav) }
        composable(Routes.PREVIEW) { PreviewScreen(repository, nav) }
        composable(Routes.EXECUTION) { ExecutionScreen(repository, engine, nav) }
        composable(Routes.HISTORY) { HistoryScreen(repository, nav) }
        composable(Routes.REPORT) { ReportScreen(repository, nav) }
        composable(Routes.SETTINGS) { SettingsScreen(nav) }
        composable(Routes.ABOUT) { SettingsScreen(nav, about = true) }
    }
}
