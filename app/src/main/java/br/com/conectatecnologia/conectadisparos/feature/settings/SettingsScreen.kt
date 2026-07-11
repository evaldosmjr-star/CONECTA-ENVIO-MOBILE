package br.com.conectatecnologia.conectadisparos.feature.settings

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import br.com.conectatecnologia.conectadisparos.core.designsystem.Screen

@Composable
fun SettingsScreen(nav: NavController, about: Boolean = false) {
    Screen(if (about) "Sobre" else "Configuracoes") {
        Text("WhatsApp preferencial: Business")
        Text("Fallback para WhatsApp comum: configuravel")
        Text("Orientacao: desative otimizacao de bateria, permita notificacoes e use aparelho dedicado.")
        Text("Teste de integracao: simule abertura, erro e confirmacao via broadcasts documentados no README.")
    }
}
