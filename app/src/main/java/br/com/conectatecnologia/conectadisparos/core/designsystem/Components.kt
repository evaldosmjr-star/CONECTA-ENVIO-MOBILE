package br.com.conectatecnologia.conectadisparos.core.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Screen(title: String, actions: @Composable RowScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, actions = actions) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) { Column(Modifier.padding(14.dp)) { Text(value, style = MaterialTheme.typography.headlineSmall); Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
