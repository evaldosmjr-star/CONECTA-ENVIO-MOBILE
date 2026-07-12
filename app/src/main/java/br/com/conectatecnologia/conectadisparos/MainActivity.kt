package br.com.conectatecnologia.conectadisparos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.conectatecnologia.conectadisparos.navigation.ConectaNavHost
import br.com.conectatecnologia.conectadisparos.ui.theme.ConectaDisparosTheme

class MainActivity : ComponentActivity() {
    private lateinit var app: ConectaDisparosApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ConectaDisparosApp
        setContent {
            ConectaDisparosTheme {
                ConectaNavHost(repository = app.repository, engine = app.engine)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::app.isInitialized) app.confirmCurrentMessageOnReturn()
    }
}
