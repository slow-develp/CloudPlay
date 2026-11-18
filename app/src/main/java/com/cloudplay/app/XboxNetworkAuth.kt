package com.cloudplay.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Xbox Network sign-in through the CloudPlay Worker.
 *
 * Microsoft credentials are entered only on Microsoft's secure sign-in page.
 * The app receives a short-lived one-time handoff, then redeems it with the
 * CloudPlay Worker. The XSTS token stays on the server.
 *
 * This class deliberately keeps the returned session ID in memory only.
 */
class XboxNetworkAuth(
    private val activity: Activity,
    private val onStatus: (String) -> Unit,
    private val onConnected: (String) -> Unit,
    private val onDisconnected: () -> Unit
) {
    companion object {
        private const val API = "https://cloudplay-api.jpzinx44.workers.dev"
        private const val CALLBACK_SCHEME = "cloudplay"
        private const val CALLBACK_HOST = "auth"
    }

    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var sessionId: String? = null

    fun signIn() {
        try {
            val authUri = Uri.parse("$API/auth/start")
            activity.startActivity(Intent(Intent.ACTION_VIEW, authUri))
            onStatus("Conclua a autenticação Microsoft para conectar o Xbox.")
        } catch (e: Exception) {
            onStatus("Não foi possível abrir a autenticação: ${e.localizedMessage ?: "erro desconhecido"}")
        }
    }

    /**
     * Call from MainActivity.onCreate() and onNewIntent().
     * Returns true if this intent is the CloudPlay OAuth callback.
     */
    fun handleIntent(intent: Intent?): Boolean {
        val uri = intent?.data ?: return false
        if (uri.scheme != CALLBACK_SCHEME || uri.host != CALLBACK_HOST) return false

        val handoff = uri.getQueryParameter("handoff")
        if (handoff.isNullOrBlank() || !handoff.matches(Regex("[a-fA-F0-9]{64}"))) {
            onStatus("O retorno de autenticação não contém um código válido.")
            return true
        }

        redeemHandoff(handoff)
        return true
    }

    private fun redeemHandoff(handoff: String) {
        onStatus("Verificando sua conexão com a Xbox Network…")
        executor.execute {
            try {
                val connection = (URL("$API/auth/redeem").openConnection() as HttpURLConnection)
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use {
                    it.write(JSONObject().put("handoff", handoff).toString().toByteArray(Charsets.UTF_8))
                }

                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (code !in 200..299) {
                    throw IllegalStateException("O servidor recusou a autenticação (HTTP $code).")
                }

                val json = JSONObject(body)
                if (!json.optBoolean("success", false)) {
                    throw IllegalStateException(json.optString("message", "Não foi possível conectar ao Xbox."))
                }

                val newSession = json.optString("sessionId")
                if (newSession.isBlank()) {
                    throw IllegalStateException("O servidor não retornou uma sessão válida.")
                }

                sessionId = newSession
                val userHash = json.optString("userHash", "")
                activity.runOnUiThread {
                    onConnected(userHash)
                    onStatus("Xbox conectado com sucesso.")
                }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    onStatus(e.localizedMessage ?: "Falha ao validar a conta Xbox.")
                }
            }
        }
    }

    fun checkSession() {
        val currentSession = sessionId
        if (currentSession.isNullOrBlank()) {
            onStatus("Nenhuma sessão Xbox ativa neste aplicativo.")
            return
        }

        executor.execute {
            try {
                val connection = (URL("$API/session/status").openConnection() as HttpURLConnection)
                connection.requestMethod = "GET"
                connection.connectTimeout = 12000
                connection.readTimeout = 12000
                connection.setRequestProperty("Authorization", "Bearer $currentSession")

                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                val json = JSONObject(body)
                val authenticated = code in 200..299 && json.optBoolean("authenticated", false)
                activity.runOnUiThread {
                    if (authenticated) {
                        onStatus("Xbox conectado.")
                    } else {
                        sessionId = null
                        onDisconnected()
                        onStatus("A sessão Xbox expirou. Conecte novamente.")
                    }
                }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    onStatus("Não foi possível verificar a sessão Xbox.")
                }
            }
        }
    }

    fun signOut() {
        sessionId = null
        onDisconnected()
        onStatus("Sessão Xbox desconectada deste aplicativo.")
    }

    fun hasSession(): Boolean = !sessionId.isNullOrBlank()

    fun close() {
        executor.shutdown()
    }
}
