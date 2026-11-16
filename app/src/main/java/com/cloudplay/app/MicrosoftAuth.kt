
package com.cloudplay.app

import android.app.Activity
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException

class MicrosoftAuth(
    private val activity: Activity,
    private val onSignedIn: (String) -> Unit,
    private val onSignedOut: () -> Unit,
    private val onMessage: (String) -> Unit
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
                    val username = result.account.username
                        ?: "Conta Microsoft"

                    onSignedIn(username)
                    onMessage("Login realizado com sucesso!")
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
                    }
                }

                override fun onAccountChanged(
                    priorAccount: com.microsoft.identity.client.IAccount?,
                    currentAccount: com.microsoft.identity.client.IAccount?
                ) {
                    if (currentAccount == null) {
                        onSignedOut()
                    } else {
                        onSignedIn(
                            currentAccount.username ?: "Conta Microsoft"
                        )
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
}
