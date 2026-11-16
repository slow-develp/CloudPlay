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

## Login Microsoft — etapa seguinte
A base ainda não inclui autenticação ativa. Para habilitá-la:
1. Registrar o aplicativo no Microsoft Entra.
2. Escolher suporte a contas pessoais Microsoft conforme o cenário.
3. Configurar plataforma Android com package `com.cloudplay.app` e assinatura/hash da chave de assinatura usada para o APK.
4. Obter o `client_id` e o `redirect_uri` gerados para o app.
5. Adicionar MSAL Android, arquivo de configuração e `BrowserTabActivity`/intent-filter conforme a documentação oficial.

Não peça nem armazene senha Microsoft dentro do CloudPlay. MSAL usa o fluxo oficial de autenticação. Entrar na Microsoft não significa automaticamente ter acesso às APIs privadas do Xbox, biblioteca de compras ou streaming.

## Limite de infraestrutura
GitHub Actions compila o APK; não hospeda servidores de jogos. Uma cloud gaming real exige máquinas com GPU, encoder de vídeo, rede de baixa latência, controle remoto, gestão de sessões e direitos/licenças para executar cada jogo. Este repositório não inclui servidor GPU nem streaming.
