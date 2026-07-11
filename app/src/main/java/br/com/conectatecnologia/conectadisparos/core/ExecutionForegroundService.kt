package br.com.conectatecnologia.conectadisparos.core

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.com.conectatecnologia.conectadisparos.MainActivity
import br.com.conectatecnologia.conectadisparos.R

class ExecutionForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL, "Execucao de disparos", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(10, notification("Acompanhando fila local", "Abra o app para ver progresso, pausas e proximas acoes."))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun notification(title: String, text: String): Notification {
        val pending = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_cd_foreground)
            .setContentTitle("Conecta Disparos")
            .setContentText("$title - $text")
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    companion object { const val CHANNEL = "conecta_disparos_execution" }
}
