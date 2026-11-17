
package com.cloudplay.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val CLOUDPLAY_API =
    "https://cloudplay-api.jpzinx44.workers.dev"

private val Bg = Color(0xFF080B0A)
private val Panel = Color(0xFF111714)
private val Green = Color(0xFF107C10)
private val BrightGreen = Color(0xFF31D158)
private val Muted = Color(0xFF9AA69E)

class MainActivity : ComponentActivity() {

    private var authNotice by mutableStateOf<String?>(null)
    private var sessionId by mutableStateOf<String?>(null)
    private var userHash by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleAuthIntent(intent)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = BrightGreen,
                    secondary = Green,
                    background = Bg,
                    surface = Panel
                )
            ) {
                CloudPlayApp(
                    connected = sessionId != null,
                    userHash = userHash,
                    notice = authNotice,
                    onLogin = { startXboxLogin() },
                    onLogout = {
                        sessionId = null
                        userHash = null
                        authNotice =
                            "Sessão desconectada deste dispositivo."
                    },
                    onDismissNotice = {
                        authNotice = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun startXboxLogin() {
        runCatching {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("$CLOUDPLAY_API/auth/start")
                )
            )
        }.onFailure {
            authNotice =
                "Não foi possível abrir a autenticação Microsoft."
        }
    }

    private fun handleAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return

        if (data.scheme != "cloudplay" || data.host != "auth") {
            return
        }

        val handoff =
            data.getQueryParameter("handoff") ?: return

        if (!handoff.matches(Regex("^[a-fA-F0-9]{64}$"))) {
            authNotice = "Código de autenticação inválido."
            return
        }

        Thread {
            try {
                val connection =
                    URL("$CLOUDPLAY_API/auth/redeem")
                        .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.outputStream.use {
                    it.write(
                        JSONObject()
                            .put("handoff", handoff)
                            .toString()
                            .toByteArray()
                    )
                }

                val responseCode = connection.responseCode

                val stream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val body =
                    stream.bufferedReader().use {
                        it.readText()
                    }

                val json = JSONObject(body)

                runOnUiThread {
                    if (
                        responseCode in 200..299 &&
                        json.optBoolean("success", true)
                    ) {
                        sessionId =
                            json.optString("sessionId")
                                .takeIf { it.isNotBlank() }

                        userHash =
                            json.optString("userHash")
                                .takeIf { it.isNotBlank() }

                        authNotice =
                            if (sessionId != null) {
                                "Sessão vinculada ao CloudPlay."
                            } else {
                                "A resposta não trouxe uma sessão válida."
                            }
                    } else {
                        authNotice =
                            json.optString(
                                "error",
                                "Não foi possível concluir o login."
                            )
                    }
                }

                connection.disconnect()

            } catch (_: Exception) {
                runOnUiThread {
                    authNotice =
                        "Falha ao validar o retorno da Microsoft."
                }
            }
        }.start()
    }
}

private enum class Page(val label: String) {
    HOME("Início"),
    LIBRARY("Minha Biblioteca"),
    GAME_PASS("Game Pass"),
    STORE("Loja"),
    SEARCH("Pesquisar"),
    FRIENDS("Amigos"),
    SETTINGS("Configurações"),
    PROFILE("Perfil")
}

@Composable
private fun CloudPlayApp(
    connected: Boolean,
    userHash: String?,
    notice: String?,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onDismissNotice: () -> Unit
) {
    val context = LocalContext.current

    val isTv = remember {
        context.resources.configuration.smallestScreenWidthDp >= 600 ||
            context.resources.configuration.screenWidthDp >= 700
    }

    var page by remember {
        mutableStateOf(Page.HOME)
    }

    var search by remember {
        mutableStateOf("")
    }

    var notificationsOpen by remember {
        mutableStateOf(false)
    }

    var profileOpen by remember {
        mutableStateOf(false)
    }

    BackHandler(
        page != Page.HOME ||
            profileOpen ||
            notificationsOpen
    ) {
        when {
            notificationsOpen -> notificationsOpen = false
            profileOpen -> profileOpen = false
            page != Page.HOME -> page = Page.HOME
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        if (isTv) {
            TvShell(
                page = page,
                onNavigate = { page = it },
                connected = connected,
                onProfile = { profileOpen = true },
                onNotifications = {
                    notificationsOpen = true
                },
                content = {
                    PageContent(
                        page,
                        search,
                        { search = it },
                        connected,
                        userHash,
                        onLogin,
                        onLogout
                    )
                }
            )
        } else {
            MobileShell(
                page = page,
                onNavigate = { page = it },
                onProfile = { profileOpen = true },
                content = {
                    PageContent(
                        page,
                        search,
                        { search = it },
                        connected,
                        userHash,
                        onLogin,
                        onLogout
                    )
                }
            )
        }

        if (notice != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(
                        onClick = onDismissNotice
                    ) {
                        Text("OK", color = BrightGreen)
                    }
                }
            ) {
                Text(notice)
            }
        }

        if (notificationsOpen) {
            NotificationPanel {
                notificationsOpen = false
            }
        }

        if (profileOpen) {
            ProfilePanel(
                connected = connected,
                userHash = userHash,
                onLogin = onLogin,
                onLogout = onLogout,
                onClose = {
                    profileOpen = false
                }
            )
        }
    }
}

@Composable
private fun TvShell(
    page: Page,
    onNavigate: (Page) -> Unit,
    connected: Boolean,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        Column(
            Modifier
                .width(204.dp)
                .fillMaxHeight()
                .background(Color(0xFF050806))
                .padding(horizontal = 14.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 8.dp,
                        bottom = 20.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        null,
                        tint = BrightGreen,
                        modifier = Modifier.size(29.dp)
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        "CLOUDPLAY",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                listOf(
                    Page.HOME,
                    Page.LIBRARY,
                    Page.GAME_PASS,
                    Page.STORE,
                    Page.FRIENDS,
                    Page.SETTINGS
                ).forEach {
                    TvNavItem(
                        it,
                        page == it
                    ) {
                        onNavigate(it)
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF14251A),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "XBOX",
                            color = BrightGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )

                        Text(
                            "Game Pass",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            "Descubra jogos",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    "CLOUDPLAY • BETA",
                    color = Muted,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    page.label,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        onNavigate(Page.SEARCH)
                    }
                ) {
                    Icon(
                        Icons.Default.Search,
                        "Pesquisar",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }

                IconButton(
                    onClick = onNotifications
                ) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        "Notificações",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    color = Panel,
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .clickable { onProfile() }
                        .focusable()
                ) {
                    Row(
                        Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 9.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint = BrightGreen
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (connected) "Conta conectada" else "Entrar",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
private fun TvNavItem(
    page: Page,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (selected || focused)
                    Color(0xFF17351F)
                else
                    Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .clickable(onClick = onClick)
            .focusable()
            .padding(
                horizontal = 13.dp,
                vertical = 13.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (page) {
            Page.HOME -> Icons.Default.Home
            Page.LIBRARY -> Icons.Default.VideogameAsset
            Page.GAME_PASS -> Icons.Default.SportsEsports
            Page.STORE -> Icons.Default.Storefront
            Page.FRIENDS -> Icons.Default.People
            else -> Icons.Default.Settings
        }

        Icon(
            icon,
            null,
            tint = if (selected || focused)
                BrightGreen
            else
                Muted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            page.label,
            color = if (selected || focused)
                Color.White
            else
                Muted,
            fontWeight = if (selected || focused)
                FontWeight.Bold
            else
                FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MobileShell(
    page: Page,
    onNavigate: (Page) -> Unit,
    onProfile: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cloud,
                null,
                tint = BrightGreen,
                modifier = Modifier.size(26.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "CLOUDPLAY",
                color = Color.White,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onProfile) {
                Icon(
                    Icons.Default.AccountCircle,
                    "Perfil",
                    tint = Color.White
                )
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }

        NavigationBar(
            containerColor = Color(0xFF090E0B)
        ) {
            listOf(
                Page.HOME,
                Page.LIBRARY,
                Page.SEARCH,
                Page.FRIENDS,
                Page.SETTINGS
            ).forEach { item ->

                NavigationBarItem(
                    selected = page == item,
                    onClick = {
                        onNavigate(item)
                    },
                    icon = {
                        Icon(
                            when (item) {
                                Page.HOME -> Icons.Default.Home
                                Page.LIBRARY -> Icons.Default.VideogameAsset
                                Page.SEARCH -> Icons.Default.Search
                                Page.FRIENDS -> Icons.Default.People
                                else -> Icons.Default.Settings
                            },
                            null
                        )
                    },
                    label = {
                        Text(
                            when (item) {
                                Page.HOME -> "Início"
                                Page.LIBRARY -> "Biblioteca"
                                Page.SEARCH -> "Buscar"
                                Page.FRIENDS -> "Amigos"
                                else -> "Config."
                            }
                        )
                    },
    
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrightGreen,
                        indicatorColor = Color(0xFF17351F),
                        selectedTextColor = BrightGreen
                    )
                )
            }
        }
    }
}

@Composable
private fun PageContent(
    page: Page,
    search: String,
    onSearch: (String) -> Unit,
    connected: Boolean,
    userHash: String?,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    when (page) {
        Page.HOME -> HomePage()

        Page.LIBRARY -> CatalogPage(
            "Sua biblioteca",
            "O catálogo Xbox em nuvem aparece abaixo. A biblioteca pessoal ainda depende da integração oficial."
        )

        Page.GAME_PASS -> GamePassPage()

        Page.STORE -> StorePage()

        Page.SEARCH -> SearchPage(
            search,
            onSearch
        )

        Page.FRIENDS -> FriendsPage()

        Page.SETTINGS -> SettingsPage()

        Page.PROFILE -> ProfilePanel(
            connected,
            userHash,
            onLogin,
            onLogout
        ) {}
    }
}

@Composable
private fun HomePage() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF163B22),
                                Color(0xFF0D1711),
                                Color(0xFF101412)
                            )
                        ),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        "JOGUE NA NUVEM",
                        color = BrightGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Seus jogos.\nSua nuvem.",
                        color = Color.White,
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        "Explore o catálogo disponível para jogar.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            CatalogPage(
                "Adicionados recentemente",
                "Jogos identificados no catálogo público Xbox."
            )
        }
    }
}

@Composable
private fun CatalogPage(
    title: String,
    subtitle: String
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            subtitle,
            color = Muted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(14.dp))

        XboxCatalogScreen()
    }
}

@Composable
private fun SearchPage(
    query: String,
    onSearch: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Pesquisar jogos")
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrightGreen,
                cursorColor = BrightGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(14.dp))

        XboxCatalogSearchScreen(query)
    }
}

@Composable
private fun GamePassPage() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Text(
                "Xbox Game Pass",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Consulte os planos e benefícios diretamente na página oficial. Preços e disponibilidade podem mudar.",
                color = Muted,
                fontSize = 13.sp
            )
        }

        item {
            InfoCard(
                "Planos e preços",
                "Veja os planos disponíveis no Brasil, benefícios e condições atualizadas."
            ) {
                OpenOfficial(
                    "https://www.xbox.com/pt-BR/xbox-game-pass"
                )
            }
        }

        item {
            InfoCard(
                "Catálogo de jogos",
                "Explore os jogos incluídos nos diferentes planos."
            ) {
                OpenOfficial(
                    "https://www.xbox.com/pt-BR/xbox-game-pass/games"
                )
            }
        }
    }
}

@Composable
private fun StorePage() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Text(
                "Loja Xbox",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Veja jogos, ofertas e detalhes na loja oficial da Xbox.",
                color = Muted,
                fontSize = 13.sp
            )
        }

        item {
            InfoCard(
                "Explorar jogos",
                "Confira lançamentos, jogos populares e ofertas atuais."
            ) {
                OpenOfficial(
                    "https://www.xbox.com/pt-BR/games"
                )
            }
        }

        item {
            InfoCard(
                "Ofertas Xbox",
                "Consulte os descontos vigentes na loja oficial."
            ) {
                OpenOfficial(
                    "https://www.xbox.com/pt-BR/games/all-games"
                )
            }
        }
    }
}

@Composable
private fun FriendsPage() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Text(
                "Amigos",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Área preparada para recursos sociais do CloudPlay.",
                color = Muted,
                fontSize = 13.sp
            )
        }

        item {
            InfoCard(
                "Lista de amigos",
                "A lista real de amigos Xbox exige integração compatível com os serviços e permissões oficiais."
            ) {}
        }

        item {
            InfoCard(
                "Mensagens e grupos",
                "Mensagens, grupos e convites próprios do CloudPlay precisam de um servidor e contas CloudPlay. Ainda não estão ativos."
            ) {}
        }

        item {
            InfoCard(
                "Convites Xbox",
                "Convites e presença Xbox não são simulados nesta versão."
            ) {}
        }
    }
}

@Composable
private fun SettingsPage() {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "cloudplay_settings",
            Context.MODE_PRIVATE
        )
    }

    var quality by remember {
        mutableStateOf(
            prefs.getString("quality", "Automática") ?: "Automática"
        )
    }

    var region by remember {
        mutableStateOf(
            prefs.getString("region", "Automática") ?: "Automática"
        )
    }

    var dataSaver by remember {
        mutableStateOf(
            prefs.getBoolean("data_saver", false)
        )
    }

    var vibration by remember {
        mutableStateOf(
            prefs.getBoolean("vibration", true)
        )
    }

    var touch by remember {
        mutableStateOf(
            prefs.getBoolean("touch", true)
        )
    }

    var hud by remember {
        mutableStateOf(
            prefs.getBoolean("hud", false)
        )
    }

    var animations by remember {
        mutableStateOf(
            prefs.getBoolean("animations", true)
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Text(
                "Configurações",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Preferências salvas neste dispositivo.",
                color = Muted,
                fontSize = 12.sp
            )
        }

        item {
            SettingDropdown(
                "Qualidade preferida",
                quality,
                listOf("Automática", "720p", "1080p", "1440p")
            ) {
                quality = it
                prefs.edit().putString("quality", it).apply()
            }
        }

        item {
            SettingDropdown(
                "Região de conexão",
                region,
                listOf(
                    "Automática",
                    "Brasil",
                    "América do Sul",
                    "América do Norte",
                    "Europa"
                )
            ) {
                region = it
                prefs.edit().putString("region", it).apply()
            }
        }

        item {
            SettingSwitch(
                "Economia de dados",
                "Preferência local; só terá efeito quando o streaming estiver integrado.",
                dataSaver
            ) {
                dataSaver = it
                prefs.edit().putBoolean("data_saver", it).apply()
            }
        }

        item {
            SettingSwitch(
                "Controles virtuais",
                "Exibir controles de toque durante uma sessão no celular.",
                touch
            ) {
                touch = it
                prefs.edit().putBoolean("touch", it).apply()
            }
        }

        item {
            SettingSwitch(
                "Vibração",
                "Preferência para dispositivos e jogos compatíveis.",
                vibration
            ) {
                vibration = it
                prefs.edit().putBoolean("vibration", it).apply()
            }
        }

        item {
            SettingSwitch(
                "HUD de desempenho",
                "Exibir informações de desempenho quando o streaming estiver disponível.",
                hud
            ) {
                hud = it
                prefs.edit().putBoolean("hud", it).apply()
            }
        }

        item {
            SettingSwitch(
                "Animações",
                "Ativar animações da interface do CloudPlay.",
                animations
            ) {
                animations = it
                prefs.edit().putBoolean("animations", it).apply()
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    prefs.edit().clear().apply()

                    quality = "Automática"
                    region = "Automática"
                    dataSaver = false
                    vibration = true
                    touch = true
                    hud = false
                    animations = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restaurar configurações padrão")
            }
        }

        item {
            Text(
                "Resolução, codec, FSR, região e HUD não alteram uma transmissão até que um servidor de streaming real seja conectado.",
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SettingDropdown(
    title: String,
    value: String,
    options: List<String>,
    onChange: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Surface(
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Box {
                TextButton(
                    onClick = {
                        expanded = true
                    }
                ) {
                    Text(
                        value,
                        color = BrightGreen
                    )

                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        tint = BrightGreen
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(option)
                            },
                            onClick = {
                                onChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Surface(
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    description,
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onChange
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        color = Panel,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    description,
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = BrightGreen
            )
        }
    }
}

@Composable
private fun ProfilePanel(
    connected: Boolean,
    userHash: String?,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Panel,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(
                Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            "Fechar",
                            tint = Color.White
                        )
                    }
                }

                Icon(
                    Icons.Default.AccountCircle,
                    null,
                    tint = BrightGreen,
                    modifier = Modifier.size(66.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Perfil CloudPlay",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (connected) "Sessão vinculada" else "Conta não conectada",
                    color = Muted
                )

                if (connected) {
                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Identificador Xbox: ${userHash ?: "indisponível"}",
                        color = Muted,
                        fontSize = 11.sp
                    )

                    Text(
                        "Gamertag, gamerpic, conquistas e estatísticas ainda não foram obtidos pela integração atual.",
                        color = Muted,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Desconectar")
                    }
                } else {
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Conectar conta Microsoft / Xbox")
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPanel(
    onClose: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Panel,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Notificações",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            "Fechar",
                            tint = Color.White
                        )
                    }
                }

                            Text(
                    "Nenhuma notificação recebida.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 24.dp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        "Fechar",
                        color = BrightGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenOfficial(url: String) {
    val context = LocalContext.current

    LaunchedEffect(url) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }
}
