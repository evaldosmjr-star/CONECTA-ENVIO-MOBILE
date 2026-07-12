package br.com.conectatecnologia.conectadisparos.integration.whatsapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.delay

interface MessageTransport {
    suspend fun openConversation(phone: String, message: String): Result<Unit>
}

class WhatsAppBusinessIntentTransport(
    private val context: Context,
    private val preferBusiness: Boolean = true,
    private val allowConsumerFallback: Boolean = true
) : MessageTransport {
    private companion object {
        const val WHATSAPP_WARMUP_MILLIS = 3_000L
    }

    override suspend fun openConversation(phone: String, message: String): Result<Unit> = runCatching {
        val packageName = selectPackage() ?: error("WhatsApp Business nao instalado.")
        context.packageManager.getLaunchIntentForPackage(packageName)?.let { warmupIntent ->
            warmupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(warmupIntent)
            delay(WHATSAPP_WARMUP_MILLIS)
        }
        val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun selectPackage(): String? {
        val pm = context.packageManager
        val business = "com.whatsapp.w4b"
        val consumer = "com.whatsapp"
        return when {
            preferBusiness && installed(pm, business) -> business
            allowConsumerFallback && installed(pm, consumer) -> consumer
            installed(pm, business) -> business
            else -> null
        }
    }

    private fun installed(pm: PackageManager, packageName: String): Boolean = try {
        pm.getPackageInfo(packageName, 0); true
    } catch (_: Exception) { false }
}
