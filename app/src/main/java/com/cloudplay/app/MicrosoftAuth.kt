package com.cloudplay.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Informações básicas da conta Microsoft autenticada (sem tokens). */
data class MicrosoftAccountInfo(
    val displayName: String,
    val username: String,
    val email: String?,
    val tenantId: String?
)

/**
 * Login Microsoft nativo com MSAL (modo de conta única).
 *
 * - A configuração (client_id, redirect_uri) fica em res/raw/auth_config_single_account.json.
 * - Os tokens são guardados e renovados exclusivamente pelo cache criptografado do MSAL.
 *   Esta classe nunca persiste tokens; o access token só é usado em memória para chamar
 *   o Microsoft Graph (graph.microsoft.com).
 *
 * IMPORTANTE: isto é apenas o login Microsoft (Microsoft identity platform / Graph).
 * NÃO é autenticação Xbox Live, XSTS ou GSSV. Esses tokens não são gerados nem simulados aqui.
 */
class MicrosoftAuth(
    private val activity: Activity,
    private val onAccountChanged: (MicrosoftAccountInfo?) -> Unit,
    private val onMessage: (String) -> Unit,
    private val onPhotoLoaded: (Bitmap?) -> Unit = {},
    private val onSessionExpired: (Boolean) -> Unit = {}
) {

    companion object {
        /** Escopos do Microsoft Graph para ler o perfil básico. */
        private val SCOPES = listOf("User.Read")
        private const val GRAPH_ME =
            "https://graph.microsoft.com/v1.0/me?\$select=displayName,mail,userPrincipalName"
        private const val GRAPH_PHOTO =
            "https://graph.microsoft.com/v1.0/me/photo/\$value"
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    @Volatile
    private var application: ISingleAccountPublicClientApplication? = null

    @Volatile
    private var currentAccount: IAccount? = null

    @Volatile
    private var closed = false

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            activity.applicationContext,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(app: ISingleAccountPublicClientApplication) {
                    application = app
                    refreshSession()
                }

                override fun onError(exception: MsalException) {
                    ui {
                        onMessage(
                            "Não foi possível iniciar o login Microsoft. " +
                                "Verifique client_id e redirect_uri em auth_config_single_account.json. " +
                                "(${exception.errorCode})"
                        )
                    }
                }
            }
        )
    }

    /** Abre a autenticação oficial da Microsoft. */
    fun signIn() {
        val app = application
        if (app == null) {
            onMessage("Aguarde a inicialização do login Microsoft.")
            return
        }

        val account = currentAccount
        if (account != null) {
            // Já existe conta no cache (ex.: sessão expirada): reautentica a mesma conta.
            val parameters = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .forAccount(account)
                .withScopes(SCOPES)
                .withCallback(interactiveCallback())
                .build()
            app.acquireToken(parameters)
            return
        }

        val parameters = SignInParameters.builder()
            .withActivity(activity)
            .withScopes(SCOPES)
            .withCallback(interactiveCallback())
            .build()
        app.signIn(parameters)
    }

    /** Remove a conta e os tokens do cache do MSAL. */
    fun signOut() {
        val app = application
        if (app == null) {
            onMessage("O login Microsoft ainda não está pronto.")
            return
        }

        app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                ui {
                    clearState()
                    onMessage("Você saiu da conta Microsoft.")
                }
            }

            override fun onError(exception: MsalException) {
                ui { onMessage("Erro ao sair: ${describe(exception)}") }
            }
        })
    }

    /**
     * Verifica a conta em cache e renova o token silenciosamente.
     * Chame ao iniciar e ao voltar para o app (onResume).
     */
    fun refreshSession() {
        val app = application ?: return
        app.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                handleAccount(activeAccount)
            }

            override fun onAccountChanged(priorAccount: IAccount?, newAccount: IAccount?) {
                if (newAccount == null && priorAccount != null) {
                    ui { onMessage("A conta Microsoft foi removida deste dispositivo.") }
                }
                handleAccount(newAccount)
            }

            override fun onError(exception: MsalException) {
                ui { onMessage("Erro ao verificar a conta: ${describe(exception)}") }
            }
        })
    }

    fun close() {
        closed = true
        executor.shutdownNow()
    }

    private fun handleAccount(account: IAccount?) {
        currentAccount = account
        if (account == null) {
            ui { clearState() }
            return
        }
        ui { onAccountChanged(infoFromAccount(account)) }
        acquireSilently(account)
    }

    private fun acquireSilently(account: IAccount) {
        val app = application ?: return
        val parameters = AcquireTokenSilentParameters.Builder()
            .forAccount(account)
            .fromAuthority(account.authority)
            .withScopes(SCOPES)
            .withCallback(object : SilentAuthenticationCallback {
                override fun onSuccess(result: IAuthenticationResult) {
                    ui { onSessionExpired(false) }
                    loadProfile(result)
                }

                override fun onError(exception: MsalException) {
                    if (exception is MsalUiRequiredException) {
                        ui {
                            onSessionExpired(true)
                            onMessage("Sua sessão Microsoft expirou. Entre novamente.")
                        }
                    } else {
                        ui { onMessage("Não foi possível renovar a sessão: ${describe(exception)}") }
                    }
                }
            })
            .build()
        app.acquireTokenSilentAsync(parameters)
    }

    private fun interactiveCallback() = object : AuthenticationCallback {
        override fun onSuccess(result: IAuthenticationResult) {
            currentAccount = result.account
            ui {
                onSessionExpired(false)
                onAccountChanged(infoFromAccount(result.account))
                onMessage("Login Microsoft realizado com sucesso.")
            }
            loadProfile(result)
        }

        override fun onError(exception: MsalException) {
            ui { onMessage("Falha no login: ${describe(exception)}") }
        }

        override fun onCancel() {
            ui { onMessage("Login cancelado.") }
        }
    }

    /** Consulta perfil básico e foto no Microsoft Graph. O token fica apenas em memória. */
    private fun loadProfile(result: IAuthenticationResult) {
        val accessToken = result.accessToken
        val fallback = infoFromAccount(result.account)
        if (closed) return
        executor.execute {
            val info = runCatching {
                val body = httpGet(GRAPH_ME, accessToken)?.toString(Charsets.UTF_8)
                    ?: return@runCatching fallback
                val json = JSONObject(body)
                MicrosoftAccountInfo(
                    displayName = json.optString("displayName").ifBlank { fallback.displayName },
                    username = json.optString("userPrincipalName").ifBlank { fallback.username },
                    email = json.optString("mail").takeIf { it.isNotBlank() && it != "null" }
                        ?: fallback.email,
                    tenantId = fallback.tenantId
                )
            }.getOrDefault(fallback)

            val photo = runCatching {
                httpGet(GRAPH_PHOTO, accessToken)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            }.getOrNull()

            ui {
                if (currentAccount != null) {
                    onAccountChanged(info)
                    onPhotoLoaded(photo)
                }
            }
        }
    }

    private fun httpGet(url: String, accessToken: String): ByteArray? {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            if (connection.responseCode == 200) connection.inputStream.use { it.readBytes() } else null
        } finally {
            connection.disconnect()
        }
    }

    private fun infoFromAccount(account: IAccount): MicrosoftAccountInfo {
        val claims = account.claims
        val name = (claims?.get("name") as? String)?.takeIf { it.isNotBlank() }
        val email = (claims?.get("email") as? String)?.takeIf { it.isNotBlank() }
        val username = account.username?.takeIf { it.isNotBlank() } ?: "Conta Microsoft"
        return MicrosoftAccountInfo(
            displayName = name ?: username,
            username = username,
            email = email,
            tenantId = account.tenantId
        )
    }

    private fun clearState() {
        currentAccount = null
        onSessionExpired(false)
        onAccountChanged(null)
        onPhotoLoaded(null)
    }

    private fun describe(exception: MsalException): String = when (exception) {
        is MsalUiRequiredException -> "é necessário entrar novamente."
        is MsalServiceException -> "o serviço Microsoft recusou a solicitação (${exception.errorCode})."
        is MsalClientException ->
            if (exception.errorCode == "io_error" || exception.errorCode == "device_network_not_available")
                "sem conexão com a internet."
            else "erro no aplicativo (${exception.errorCode})."
        else -> exception.errorCode ?: (exception.message ?: "erro desconhecido")
    }

    private fun ui(block: () -> Unit) {
        if (!closed && !activity.isFinishing) activity.runOnUiThread(block)
    }
}
