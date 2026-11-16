
package com.cloudplay.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import java.net.HttpURLConnection
import java.net.URL

class MicrosoftAuth(
    private val activity: Activity,
    private val onSignedIn: (String) -> Unit,
    private val onSignedOut: () -> Unit,
    private val onMessage: (String) -> Unit,
    private val onPhotoLoaded: (Bitmap?) -> Unit = {}
) {

    private var application:
        ISingleAccountPublicClientApplication? = null

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            activity,
            R.raw.auth_config_single_account,
            object :
                IPublicClientApplication.ISingleAccountApplicationCreatedListener {

                override fun onCreated(
                    app: ISingleAccountPublicClientApplication
                ) {
                    application = app
                    loadAccount()
                    onMessage("Microsoft pronta para conectar.")
                }

                override fun onError(exception: MsalException) {
                    onMessage(
                        "Erro ao iniciar Microsoft: ${exception.message}"
                    )
                }
            }
        )
    }

    fun signIn() {
        val app = application

        if (app == null) {
            onMessage("Aguarde a inicialização da Microsoft.")
            return
        }

        val parameters = SignInParameters.builder()
            .withActivity(activity)
            .withScopes(listOf("User.Read"))
            .withCallback(object : AuthenticationCallback {

                override fun onSuccess(
                    result: IAuthenticationResult
                ) {
                    val username =
                        result.account.username ?: "Conta Microsoft"

                    onSignedIn(username)
                    onMessage("Login realizado com sucesso!")

                    downloadProfilePhoto(result.accessToken)
                }

                override fun onError(exception: MsalException) {
                    onMessage(
                        "Falha no login: ${exception.message}"
                    )
                }

                override fun onCancel() {
                    onMessage("Login cancelado.")
                }
            })
            .build()

        app.signIn(parameters)
    }

    fun signOut() {
        val app = application

        if (app == null) {
            onMessage("Microsoft ainda não está pronta.")
            return
        }

        app.signOut(
            object :
                ISingleAccountPublicClientApplication.SignOutCallback {

                override fun onSignOut() {
                    onSignedOut()
                    onPhotoLoaded(null)
                    onMessage("Você saiu da conta Microsoft.")
                }

                override fun onError(exception: MsalException) {
                    onMessage(
                        "Erro ao sair: ${exception.message}"
                    )
                }
            }
        )
    }

    private fun loadAccount() {
        application?.getCurrentAccountAsync(
            object :
                ISingleAccountPublicClientApplication.CurrentAccountCallback {

                override fun onAccountLoaded(
                    activeAccount: com.microsoft.identity.client.IAccount?
                ) {
                    if (activeAccount != null) {
                        onSignedIn(
                            activeAccount.username ?: "Conta Microsoft"
                        )

                        loadPhotoSilently(activeAccount.authority)
                    } else {
                        onPhotoLoaded(null)
                    }
                }

                override fun onAccountChanged(
                    priorAccount: com.microsoft.identity.client.IAccount?,
                    currentAccount: com.microsoft.identity.client.IAccount?
                ) {
                    if (currentAccount == null) {
                        onSignedOut()
                        onPhotoLoaded(null)
                    } else {
                        onSignedIn(
                            currentAccount.username ?: "Conta Microsoft"
                        )

                        loadPhotoSilently(currentAccount.authority)
                    }
                }

                override fun onError(exception: MsalException) {
                    onMessage(
                        "Erro ao verificar conta: ${exception.message}"
                    )
                }
            }
        )
    }

    private fun loadPhotoSilently(authority: String) {
        application?.acquireTokenSilentAsync(
            arrayOf("User.Read"),
            authority,
            object : SilentAuthenticationCallback {

                override fun onSuccess(
                    result: IAuthenticationResult
                ) {
                    downloadProfilePhoto(result.accessToken)
                }

                override fun onError(exception: MsalException) {
                    activity.runOnUiThread {
                        onMessage(
                            "Não foi possível atualizar a foto Microsoft."
                        )
                    }
                }
            }
        )
    }

    private fun downloadProfilePhoto(accessToken: String) {
        Thread {
            var bitmap: Bitmap? = null
            var connection: HttpURLConnection? = null

            try {
                connection = URL(
                    "https://graph.microsoft.com/v1.0/me/photo/\$value"
                ).openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                if (connection.responseCode == 200) {
                    connection.inputStream.use { stream ->
                        bitmap = BitmapFactory.decodeStream(stream)
                    }
                }
            } catch (exception: Exception) {
                bitmap = null
            } finally {
                connection?.disconnect()
            }

            activity.runOnUiThread {
                onPhotoLoaded(bitmap)
            }
        }.start()
    }
}
