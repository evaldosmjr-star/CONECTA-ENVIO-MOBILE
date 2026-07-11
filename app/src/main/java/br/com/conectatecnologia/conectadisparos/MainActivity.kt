package br.com.conectatecnologia.conectadisparos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.conectatecnologia.conectadisparos.navigation.ConectaNavHost
import br.com.conectatecnologia.conectadisparos.ui.theme.ConectaDisparosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ConectaDisparosApp
        setContent {
            ConectaDisparosTheme {
                ConectaNavHost(repository = app.repository, engine = app.engine)
            }
        }
    }
}
