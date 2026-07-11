package br.com.conectatecnologia.conectadisparos.feature.importbatch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen
import br.com.conectatecnologia.conectadisparos.data.parser.BatchParser
import br.com.conectatecnologia.conectadisparos.domain.repository.BatchRepository
import br.com.conectatecnologia.conectadisparos.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun ImportBatchScreen(repository: BatchRepository, nav: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var log by remember { mutableStateOf("Selecione um arquivo JSON para validar.") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> selectedUri = uri }
    Screen("Importar lote") {
        Button(onClick = { launcher.launch(arrayOf("application/json", "text/*")) }, Modifier.fillMaxWidth()) { Text("Selecionar JSON") }
        selectedUri?.let { uri ->
            Button(onClick = {
                scope.launch {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                    val provisional = BatchParser().parse(text)
                    val imported = provisional.batch?.let { repository.isBatchImported(it.id) } ?: false
                    val result = BatchParser().parse(text, imported)
                    log = buildString {
                        appendLine("Valido: ${result.canImport}")
                        appendLine("Avisos/erros: ${result.issues.size}")
                        result.issues.forEach { appendLine("${it.field}: ${it.message}") }
                    }
                    if (result.canImport) { repository.importBatch(result.batch!!); nav.navigate(Routes.PREVIEW) }
                }
            }, Modifier.fillMaxWidth()) { Text("Validar e importar") }
        }
        Text(log)
    }
}
