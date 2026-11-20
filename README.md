# CloudPlay — projeto Android nativo (base 0.1.0)

Projeto inicial Kotlin + Jetpack Compose. Não é um serviço de streaming funcional e não finge login, catálogo, perfil ou jogos reais.

## Estrutura
- `app/`: aplicativo Android nativo.
- `.github/workflows/build-apk.yml`: compila APK de debug no GitHub Actions.
- `applicationId`: `com.cloudplay.app`.
- `minSdk`: 26 (Android 8.0+).

## Desenvolver pelo celular
1. Crie uma conta gratuita no GitHub.
2. Crie um repositório público chamado `CloudPlay`.
3. Envie todos os arquivos desta pasta para o repositório, mantendo as pastas.
4. Abra a aba **Actions** e execute **Build CloudPlay APK** (ou faça um commit na branch `main`).
5. Quando terminar, abra a execução e baixe o artefato `CloudPlay-debug`.
6. Extraia o ZIP do artefato e instale `app-debug.apk` no Android.

O editor web do GitHub permite editar arquivos no celular, mas não compila. O GitHub Actions compila remotamente. Cotas e limites podem mudar; confira a página de cobrança antes de depender do serviço.

## Login Microsoft (MSAL)
O login usa a biblioteca oficial MSAL Android (`com.microsoft.identity.client:msal`), em modo de conta única. Tokens ficam apenas no cache criptografado do próprio MSAL.

Configuração (Microsoft Entra → Registros de aplicativo):
1. Suporte a contas: "contas em qualquer diretório organizacional e contas Microsoft pessoais".
2. Plataforma **Android**: package `com.cloudplay.app` + Signature Hash da chave que assina o APK.
3. Permissão delegada do Microsoft Graph: `User.Read`.
4. Copie o `client_id` e o `redirect_uri` para `app/src/main/res/raw/auth_config_single_account.json`.
5. Coloque o mesmo Signature Hash em `android:path="/<HASH>"` da `BrowserTabActivity` em `AndroidManifest.xml`.

O login Microsoft não é autenticação Xbox Network. Xbox Live, XSTS e GSSV exigem uma etapa separada, não implementada aqui; o app não gera nem simula tokens Xbox.

## Limite de infraestrutura
GitHub Actions compila o APK; não hospeda servidores de jogos. Uma cloud gaming real exige máquinas com GPU, encoder de vídeo, rede de baixa latência, controle remoto, gestão de sessões e direitos/licenças para executar cada jogo. Este repositório não inclui servidor GPU nem streaming.
