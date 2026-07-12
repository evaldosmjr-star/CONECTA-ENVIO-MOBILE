package br.com.conectatecnologia.conectadisparos.integration.macrodroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.conectatecnologia.conectadisparos.ConectaDisparosApp

object MacroDroidActions {
    const val CONTACT_OPENED = "br.com.conectatecnologia.conectadisparos.ACTION_CONTACT_OPENED"
    const val WAITING_EXTERNAL_CONFIRMATION = "br.com.conectatecnologia.conectadisparos.ACTION_WAITING_EXTERNAL_CONFIRMATION"
    const val BATCH_PAUSED = "br.com.conectatecnologia.conectadisparos.ACTION_BATCH_PAUSED"
    const val BATCH_FINISHED = "br.com.conectatecnologia.conectadisparos.ACTION_BATCH_FINISHED"
    const val MESSAGE_SENT = "br.com.conectatecnologia.conectadisparos.ACTION_MESSAGE_SENT"
    const val MESSAGE_FAILED = "br.com.conectatecnologia.conectadisparos.ACTION_MESSAGE_FAILED"
    const val SKIP_CONTACT = "br.com.conectatecnologia.conectadisparos.ACTION_SKIP_CONTACT"
}

class MacroDroidBroadcaster(private val context: Context) {
    fun contactOpened(batchId: String, contactId: String, nome: String, telefone: String, mensagem: String, posicao: Int, total: Int) =
        send(MacroDroidActions.CONTACT_OPENED, batchId, contactId, nome, telefone, mensagem, posicao, total)
    fun waitingConfirmation(batchId: String, contactId: String, nome: String, telefone: String, mensagem: String, posicao: Int, total: Int) =
        send(MacroDroidActions.WAITING_EXTERNAL_CONFIRMATION, batchId, contactId, nome, telefone, mensagem, posicao, total)
    fun batchPaused(batchId: String, nome: String) = send(MacroDroidActions.BATCH_PAUSED, batchId, "", nome, "", "", 0, 0)
    fun batchFinished(batchId: String, nome: String) = send(MacroDroidActions.BATCH_FINISHED, batchId, "", nome, "", "", 0, 0)

    private fun send(action: String, batchId: String, contactId: String, nome: String, telefone: String, mensagem: String, posicao: Int, total: Int) {
        context.sendBroadcast(Intent(action).apply {
            putExtra("batchId", batchId)
            putExtra("contactId", contactId)
            putExtra("nome", nome)
            putExtra("telefone", telefone)
            putExtra("mensagem", mensagem)
            putExtra("posicao", posicao)
            putExtra("total", total)
            putExtra("timestamp", System.currentTimeMillis())
        })
    }
}

class MacroDroidConfirmationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as ConectaDisparosApp
        val batchId = intent.getStringExtra("batchId")
        val contactId = intent.getStringExtra("contactId")
        when (intent.action) {
            MacroDroidActions.MESSAGE_SENT -> {
                if (batchId != null && contactId != null) app.confirmMessage(batchId, contactId) else app.confirmCurrentMessage()
            }
            MacroDroidActions.MESSAGE_FAILED -> {
                val error = intent.getStringExtra("erro") ?: "Falha informada pelo MacroDroid."
                if (batchId != null && contactId != null) app.failMessage(batchId, contactId, error) else app.failCurrentMessage(error)
            }
            MacroDroidActions.SKIP_CONTACT -> {
                val reason = intent.getStringExtra("motivo") ?: "Ignorado pelo MacroDroid."
                if (batchId != null && contactId != null) app.skipContact(batchId, contactId, reason) else app.skipCurrentContact(reason)
            }
        }
    }
}
