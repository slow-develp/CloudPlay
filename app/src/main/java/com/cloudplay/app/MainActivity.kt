
package com.cloudplay.app

import android.graphics.Bitmap
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

private val BackgroundColor = Color(0xFF090B10)
private val SurfaceColor = Color(0xFF151922)
private val SurfaceLight = Color(0xFF202633)
private val AccentColor = Color(0xFF35C878)
private val MainText = Color(0xFFF5F6F8)
private val SecondaryText = Color(0xFF9AA3B2)

class MainActivity : ComponentActivity() {

    private lateinit var xboxAuth: XboxNetworkAuth

    var xboxConnected by mutableStateOf(false)
        private set
    var xboxUserHash by mutableStateOf("")
        private set
    var xboxNotice by mutableStateOf("Conecte sua conta Xbox.")
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        xboxAuth = XboxNetworkAuth(
            activity = this,
            onStatus = { message -> xboxNotice = message },
            onConnected = { userHash ->
                xboxConnected = true
                xboxUserHash = userHash
            },
            onDisconnected = {
                xboxConnected = false
                xboxUserHash = ""
            }
        )
        xboxAuth.handleIntent(intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            CloudPlayApp(
                xboxAuth = xboxAuth,
                xboxConnected = xboxConnected,
                xboxNotice = xboxNotice
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        xboxAuth.handleIntent(intent)
    }

    override fun onDestroy() {
        if (::xboxAuth.isInitialized) xboxAuth.close()
        super.onDestroy()
    }
}

@Composable
fun CloudPlayApp(
    xboxAuth: XboxNetworkAuth,
    xboxConnected: Boolean,
    xboxNotice: String
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity
    val configuration = LocalConfiguration.current
    val isTv = configuration.screenWidthDp >= 700

    var account by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf<Bitmap?>(null) }
    var currentPage by remember { mutableStateOf("Início") }

    val displayAccount = account ?: if (xboxConnected) "Xbox conectado" else null

    val auth = remember {
        MicrosoftAuth(
            activity = activity,
            onSignedIn = { username ->
                account = username
            },
            onSignedOut = {
                account = null
                profilePhoto = null
            },
            onMessage = { message ->
                notice = message
            },
            onPhotoLoaded = { bitmap ->
                profilePhoto = bitmap
            }
        )
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BackgroundColor,
            surface = SurfaceColor,
            primary = AccentColor,
            onPrimary = Color.Black,
            onBackground = MainText,
            onSurface = MainText
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                if (isTv) {
                    TvInterface(
                        page = currentPage,
                        onPageChange = { currentPage = it },
                        account = displayAccount,
                        photo = profilePhoto,
                        notice = notice,
                        xboxConnected = xboxConnected,
                        xboxNotice = xboxNotice,
                        onXboxLogin = { xboxAuth.signIn() },
                        onXboxLogout = { xboxAuth.signOut() },
                        onLogin = { auth.signIn() },
                        onLogout = { auth.signOut() }
                    )
                } else {
                    MobileInterface(
                        page = currentPage,
                        onPageChange = { currentPage = it },
                        account = displayAccount,
                        photo = profilePhoto,
                        notice = notice,
                        xboxConnected = xboxConnected,
                        xboxNotice = xboxNotice,
                        onXboxLogin = { xboxAuth.signIn() },
                        onXboxLogout = { xboxAuth.signOut() },
                        onLogin = { auth.signIn() },
                        onLogout = { auth.signOut() }
                    )
                }
            }
        }
    }
}

private val menuItems = listOf(
    "Início",
    "Minha Biblioteca",
    "Game Pass",
    "Pesquisar",
    "Loja",
    "Amigos",
    "Notificações",
    "Perfil",
    "Configurações"
)

@Composable
private fun TvInterface(
    page: String,
    onPageChange: (String) -> Unit,
    account: String?,
    photo: Bitmap?,
    notice: String,
    xboxConnected: Boolean,
    xboxNotice: String,
    onXboxLogin: () -> Unit,
    onXboxLogout: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .width(190.dp)
                .fillMaxHeight()
                .background(Color(0xFF10141C))
                .padding(20.dp)
        ) {
            CloudLogo()

            Spacer(modifier = Modifier.height(35.dp))

            menuItems.forEach { item ->
                NavigationItem(
                    title = item,
                    selected = page == item,
                    onClick = { onPageChange(item) },
                    isTv = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "CLOUDPLAY",
                color = SecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sua experiência de jogos",
                color = SecondaryText,
                fontSize = 10.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            Header(
                account = account,
                photo = photo,
                onProfile = { onPageChange("Perfil") },
                onLogin = onXboxLogin
            )

            Spacer(modifier = Modifier.height(22.dp))

            when (page) {
                "Início" -> HomeScreen(isTv = true, xboxConnected = xboxConnected)

                "Minha Biblioteca" -> CatalogPage(
                    title = "Minha Biblioteca"
                )

                "Game Pass" -> GamePassPage()

                "Loja" -> StorePage()

                "Amigos" -> FriendsPage()

                "Notificações" -> NotificationsPage()

                "Pesquisar" -> SearchPage()

                "Perfil" -> ProfileScreen(
                    account = account,
                    photo = photo,
                    notice = notice,
                    xboxConnected = xboxConnected,
                    xboxNotice = xboxNotice,
                    onXboxLogin = onXboxLogin,
                    onXboxLogout = onXboxLogout,
                    onLogin = onLogin,
                    onLogout = onLogout
                )

                "Configurações" -> SettingsPage()

                else -> HomeScreen(isTv = true, xboxConnected = xboxConnected)
            }
        }
    }
}

@Composable
private fun MobileInterface(
    page: String,
    onPageChange: (String) -> Unit,
    account: String?,
    photo: Bitmap?,
    notice: String,
    xboxConnected: Boolean,
    xboxNotice: String,
    onXboxLogin: () -> Unit,
    onXboxLogout: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Header(
            account = account,
            photo = photo,
            onProfile = { onPageChange("Perfil") },
            onLogin = onXboxLogin,
            onFriends = { onPageChange("Amigos") },
            onNotifications = { onPageChange("Notificações") }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (page) {
                "Início" -> HomeScreen(isTv = false, xboxConnected = xboxConnected)

                "Minha Biblioteca" -> CatalogPage(
                    title = "Minha Biblioteca"
                )

                "Game Pass" -> GamePassPage()

                "Loja" -> StorePage()

                "Amigos" -> FriendsPage()

                "Notificações" -> NotificationsPage()

                "Pesquisar" -> SearchPage()

                "Perfil" -> ProfileScreen(
                    account = account,
                    photo = photo,
                    notice = notice,
                    xboxConnected = xboxConnected,
                    xboxNotice = xboxNotice,
                    onXboxLogin = onXboxLogin,
                    onXboxLogout = onXboxLogout,
                    onLogin = onLogin,
                    onLogout = onLogout
                )

                "Configurações" -> SettingsPage()

                else -> HomeScreen(isTv = false, xboxConnected = xboxConnected)
            }
        }

        NavigationBar(
            containerColor = Color(0xFF11151D),
            contentColor = MainText
        ) {
            val mobileItems = listOf(
                "Início",
                "Biblioteca",
                "Pesquisar",
                "Perfil",
                "Config."
            )

            val icons = listOf(
                Icons.Default.Home,
                Icons.Default.SportsEsports,
                Icons.Default.Search,
                Icons.Default.Person,
                Icons.Default.Settings
            )

            mobileItems.forEachIndexed { index, item ->
                val destination = when (item) {
                    "Biblioteca" -> "Minha Biblioteca"
                    "Avisos" -> "Notificações"
                    "Config." -> "Configurações"
                    else -> item
                }

                NavigationBarItem(
                    selected = page == destination,
                    onClick = { onPageChange(destination) },
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item
                        )
                    },
                    label = {
                        Text(item, fontSize = 10.sp)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MainText,
                        indicatorColor = Color.White,
                        unselectedIconColor = SecondaryText,
                        unselectedTextColor = SecondaryText
                    )
                )
            }
        }
    }
}

@Composable
private fun CloudLogo() {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTv = configuration.screenWidthDp >= 700

    Image(
        painter = androidx.compose.ui.res.painterResource(
            id = R.drawable.cloudplay_logo
        ),
        contentDescription = "CloudPlay",
        modifier = Modifier
            .width(if (isTv) 190.dp else 150.dp)
            .height(if (isTv) 65.dp else 52.dp),
        contentScale = androidx.compose.ui.layout.ContentScale.Fit
    )
}

@Composable
private fun Header(
    account: String?,
    photo: Bitmap?,
    onProfile: () -> Unit,
    onLogin: () -> Unit,
    onFriends: (() -> Unit)? = null,
    onNotifications: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CloudLogo()

        Spacer(modifier = Modifier.weight(1f))

        if (onFriends != null) {
            IconButton(onClick = onFriends) {
                Icon(Icons.Default.People, contentDescription = "Amigos", tint = MainText)
            }
        }
        if (onNotifications != null) {
            IconButton(onClick = onNotifications) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = MainText)
            }
        }

        if (account != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .clickable { onProfile() }
                    .background(SurfaceColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(photo, 34.dp)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = account,
                    color = MainText,
                    fontSize = 12.sp
                )
            }
        } else {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.Black
                )
            ) {
                Text("Entrar")
            }
        }
    }
}

@Composable
private fun NavigationItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    isTv: Boolean
) {
    val icon = when (title) {
        "Início" -> Icons.Default.Home
        "Minha Biblioteca" -> Icons.Default.SportsEsports
        "Game Pass" -> Icons.Default.Cloud
        "Pesquisar" -> Icons.Default.Search
        "Loja" -> Icons.Default.Store
        "Amigos" -> Icons.Default.People
        "Notificações" -> Icons.Default.Notifications
        "Perfil" -> Icons.Default.Person
        else -> Icons.Default.Settings
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Color.White else Color.Transparent
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .then(if (isTv) Modifier.focusable() else Modifier)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) Color.Black else SecondaryText
        )

        Spacer(modifier = Modifier.width(13.dp))

        Text(
            text = title,
            color = if (selected) Color.Black else MainText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun HomeScreen(isTv: Boolean, xboxConnected: Boolean = false) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 25.dp,
            top = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HeroBanner(isTv)
        }

        item {
            Column {
                Text(
                    text = "Explore o CloudPlay",
                    color = MainText,
                    fontSize = if (isTv) 24.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Descubra jogos e acesse seu catálogo.",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }
        }

        if (xboxConnected) {
            item {
                Text(
                    text = "Jogados recentemente",
                    color = MainText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                ActionCard(
                    title = "Sua atividade Xbox",
                    description = "Seus jogos recentes aparecerão aqui quando a integração de atividade Xbox estiver disponível.",
                    icon = Icons.Default.History
                )
            }
            item {
                ActionCard(
                    title = "Minha biblioteca",
                    description = "Acesse seus jogos associados à conta conectada.",
                    icon = Icons.Default.SportsEsports
                )
            }
        } else {
            item {
                Text(
                    text = "Catálogo Xbox",
                    color = MainText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                XboxCatalogScreen()
            }
            item {
                ActionCard(
                    title = "Sua biblioteca",
                    description = "Conecte sua conta Xbox para personalizar sua biblioteca.",
                    icon = Icons.Default.SportsEsports
                )
            }
        }
    }
}

@Composable
private fun HeroBanner(isTv: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isTv) 250.dp else 190.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF202D3D),
                        Color(0xFF10151E),
                        Color(0xFF17251F)
                    )
                )
            )
            .padding(22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CLOUD GAMING",
                color = AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "SEUS JOGOS.\nSUA NUVEM.",
                color = Color.White,
                fontSize = if (isTv) 32.sp else 25.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = if (isTv) 37.sp else 30.sp
            )

            Text(
                text = "Bem-vindo ao CloudPlay.",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CatalogPage(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = MainText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        XboxCatalogScreen()
    }
}

@Composable
private fun SearchPage() {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "Pesquisar",
            color = MainText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Pesquisar jogos...")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Pesquisar"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpar"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        XboxCatalogSearchScreen(query = query)
    }
}

@Composable
private fun ProfileScreen(
    account: String?,
    photo: Bitmap?,
    notice: String,
    xboxConnected: Boolean,
    xboxNotice: String,
    onXboxLogin: () -> Unit,
    onXboxLogout: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    val signedIn = xboxConnected || (!account.isNullOrBlank() && account != "Xbox conectado")
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        contentPadding = PaddingValues(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Perfil", color = MainText, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text("Sua conta e sua atividade no Xbox", color = SecondaryText, fontSize = 13.sp)
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceColor)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(photo, if (signedIn) 96.dp else 82.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (signedIn) (account ?: "Conta Xbox") else "Entre para continuar",
                    color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (xboxConnected) "Conta Xbox conectada" else if (signedIn) "Conta Microsoft conectada" else "Conecte sua conta Microsoft e Xbox em um só fluxo",
                    color = SecondaryText, fontSize = 12.sp
                )
                Spacer(Modifier.height(20.dp))
                if (xboxConnected) {
                    Button(onClick = onXboxLogout, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Desconectar conta")
                    }
                } else {
                    Button(
                        onClick = onXboxLogin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null)
                        Spacer(Modifier.width(9.dp))
                        Text("Entrar com Microsoft + Xbox", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Você usará a autenticação oficial da Microsoft. Não é necessário adicionar uma segunda conta.",
                        color = SecondaryText, fontSize = 11.sp
                    )
                }
                val status = listOf(xboxNotice, notice).filter { it.isNotBlank() }.distinct().joinToString("\n")
                if (status.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(status, color = SecondaryText, fontSize = 12.sp)
                }
            }
        }
        if (signedIn) {
            item {
                Text("Sua atividade", color = MainText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                ActionCard(
                    "Jogos recentes",
                    "O histórico real será exibido quando a consulta de atividade Xbox estiver integrada.",
                    Icons.Default.History
                )
            }
        } else {
            item {
                ActionCard(
                    "Uma conta. Uma experiência.",
                    "Conecte sua conta para acessar seu perfil Xbox, biblioteca e recursos disponíveis.",
                    Icons.Default.VerifiedUser
                )
            }
        }
    }
}

@Composable
private fun GamePassPage() {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Xbox Game Pass", color = MainText, fontSize = 27.sp, fontWeight = FontWeight.Bold) }
        item { Text("Consulte os planos, benefícios e valores atualizados diretamente na Microsoft.", color = SecondaryText, fontSize = 13.sp) }
        item { ActionCard("Planos e preços", "Veja os planos disponíveis no Brasil e os valores oficiais.", Icons.Default.CardMembership) }
        item { Button(onClick = { openOfficial(context, "https://www.xbox.com/pt-BR/xbox-game-pass") }, modifier = Modifier.fillMaxWidth()) { Text("Ver planos oficiais") } }
        item { ActionCard("Jogos na nuvem", "Confira quais jogos podem ser transmitidos com sua assinatura.", Icons.Default.Cloud) }
        item { OutlinedButton(onClick = { openOfficial(context, "https://www.xbox.com/pt-BR/play") }, modifier = Modifier.fillMaxWidth()) { Text("Abrir Xbox Cloud Gaming") } }
        item { Text("A disponibilidade e os recursos dependem do plano, região e conta Microsoft.", color = SecondaryText, fontSize = 11.sp) }
    }
}

@Composable
private fun StorePage() {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Loja Xbox", color = MainText, fontSize = 27.sp, fontWeight = FontWeight.Bold) }
        item { Text("Explore jogos, ofertas e páginas oficiais de compra. Os preços são consultados no site da Microsoft para evitar valores desatualizados.", color = SecondaryText, fontSize = 13.sp) }
        item { ActionCard("Jogos Xbox", "Catálogo oficial de jogos e detalhes.", Icons.Default.SportsEsports) }
        item { Button(onClick = { openOfficial(context, "https://www.xbox.com/pt-BR/games/all-games") }, modifier = Modifier.fillMaxWidth()) { Text("Explorar jogos") } }
        item { ActionCard("Ofertas", "Confira promoções e descontos ativos.", Icons.Default.LocalOffer) }
        item { OutlinedButton(onClick = { openOfficial(context, "https://www.xbox.com/pt-BR/games/store") }, modifier = Modifier.fillMaxWidth()) { Text("Abrir Microsoft Store") } }
    }
}

@Composable
private fun FriendsPage() {
    var gamertag by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf(listOf<String>()) }
    var selected by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }
    var chats by remember { mutableStateOf(mapOf<String, List<String>>()) }
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Amigos e grupos", color = MainText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Contatos e conversas locais do CloudPlay. Ainda não sincronizados com a rede Xbox.", color = SecondaryText, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = gamertag, onValueChange = { gamertag = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Gamertag ou nome do amigo") }, singleLine = true)
        Button(onClick = { val v = gamertag.trim(); if (v.isNotEmpty() && v !in friends) friends = friends + v; gamertag = "" }, modifier = Modifier.fillMaxWidth()) { Text("Adicionar contato local") }
        Spacer(Modifier.height(12.dp))
        if (selected == null) {
            if (friends.isEmpty()) Text("Sua lista está vazia. Adicione contatos para organizar conversas neste aparelho.", color = SecondaryText, fontSize = 13.sp)
            friends.forEach { friend ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceColor).clickable { selected = friend }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, tint = AccentColor)
                    Spacer(Modifier.width(10.dp)); Text(friend, color = MainText, modifier = Modifier.weight(1f)); Text("Abrir chat", color = AccentColor)
                }
            }
            OutlinedButton(onClick = { openOfficial(context, "https://www.xbox.com/pt-BR/play") }, modifier = Modifier.fillMaxWidth()) { Text("Abrir Xbox para recursos de amigos") }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, "Voltar") }
                Text(selected ?: "Conversa", color = MainText, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                (chats[selected.orEmpty()].orEmpty()).forEach { msg -> Text(msg, color = MainText, modifier = Modifier.padding(8.dp).background(SurfaceColor, RoundedCornerShape(10.dp)).padding(10.dp)) }
                if (chats[selected.orEmpty()].isNullOrEmpty()) Text("Inicie uma conversa local. Essas mensagens não são enviadas à Xbox.", color = SecondaryText, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f), placeholder = { Text("Mensagem") })
                IconButton(onClick = { val who = selected; val msg = draft.trim(); if (who != null && msg.isNotEmpty()) { chats = chats + (who to (chats[who].orEmpty() + "Você: $msg")); draft = "" } }) { Icon(Icons.Default.Send, "Enviar") }
            }
        }
    }
}

@Composable
private fun NotificationsPage() {
    var notices by remember { mutableStateOf(listOf<String>()) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Notificações", color = MainText, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = { notices = emptyList() }) { Text("Limpar") }
        }
        Text("Central local do CloudPlay. Alertas de conquistas, convites e atividades Xbox só aparecerão quando a integração oficial estiver disponível.", color = SecondaryText, fontSize = 12.sp)
        if (notices.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(10.dp)); Text("Nenhuma notificação", color = MainText, fontWeight = FontWeight.Bold)
                    Text("Você verá os avisos do CloudPlay aqui.", color = SecondaryText, fontSize = 12.sp)
                }
            }
        } else notices.forEach { Text(it, color = MainText, modifier = Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(12.dp)).padding(14.dp)) }
    }
}

private fun openOfficial(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

@Composable
private fun ProfileAvatar(
    photo: Bitmap?,
    size: androidx.compose.ui.unit.Dp
) {
    if (photo != null) {
        Image(
            bitmap = photo.asImageBitmap(),
            contentDescription = "Foto do perfil",
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

@Composable
private fun SettingsPage() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("cloudplay_settings", 0) }

    var resolution by remember { mutableStateOf(prefs.getString("resolution", "Automática") ?: "Automática") }
    var fsr by remember { mutableStateOf(prefs.getString("fsr", "Desativado") ?: "Desativado") }
    var videoFit by remember { mutableStateOf(prefs.getString("video_fit", "Ajustar à tela") ?: "Ajustar à tela") }
    var codec by remember { mutableStateOf(prefs.getString("codec", "Automático") ?: "Automático") }
    var fps by remember { mutableStateOf(prefs.getString("fps", "Até 60 FPS") ?: "Até 60 FPS") }
    var performancePanel by remember { mutableStateOf(prefs.getBoolean("performance_panel", true)) }
    var panelLayout by remember { mutableStateOf(prefs.getString("panel_layout", "Compacto") ?: "Compacto") }
    var sessionMenu by remember { mutableStateOf(prefs.getBoolean("session_menu", true)) }
    var engine by remember { mutableStateOf(prefs.getString("engine", "Automático") ?: "Automático") }
    var touchControls by remember { mutableStateOf(prefs.getBoolean("touch_controls", true)) }
    var vibration by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
    var interfaceSound by remember { mutableStateOf(prefs.getBoolean("interface_sound", true)) }
    var leftHanded by remember { mutableStateOf(prefs.getBoolean("left_handed", false)) }
    var controllerLayout by remember { mutableStateOf(prefs.getString("controller_layout", "Padrão") ?: "Padrão") }
    var stickSensitivity by remember { mutableStateOf(prefs.getString("stick_sensitivity", "Normal") ?: "Normal") }
    var audioQuality by remember { mutableStateOf(prefs.getString("audio_quality", "Alta") ?: "Alta") }
    var audioOutput by remember { mutableStateOf(prefs.getString("audio_output", "Automática") ?: "Automática") }
    var audioBalance by remember { mutableStateOf(prefs.getString("audio_balance", "Centralizado") ?: "Centralizado") }
    var region by remember { mutableStateOf(prefs.getString("region", "Automática") ?: "Automática") }
    var networkMode by remember { mutableStateOf(prefs.getString("network_mode", "Automático") ?: "Automático") }
    var gameLanguage by remember { mutableStateOf(prefs.getString("game_language", "Português") ?: "Português") }
    var dataSaver by remember { mutableStateOf(prefs.getBoolean("data_saver", false)) }
    var appLanguage by remember { mutableStateOf(prefs.getString("app_language", "Português (Brasil)") ?: "Português (Brasil)") }
    var theme by remember { mutableStateOf(prefs.getString("theme", "Escuro") ?: "Escuro") }
    var startupPage by remember { mutableStateOf(prefs.getString("startup_page", "Início") ?: "Início") }
    var notifications by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var friendNotifications by remember { mutableStateOf(prefs.getBoolean("friend_notifications", true)) }
    var promotionalNotifications by remember { mutableStateOf(prefs.getBoolean("promo_notifications", false)) }
    var analytics by remember { mutableStateOf(prefs.getBoolean("analytics", false)) }
    var autoUpdates by remember { mutableStateOf(prefs.getBoolean("auto_updates", true)) }
    var clearCacheOnExit by remember { mutableStateOf(prefs.getBoolean("clear_cache_exit", false)) }
    var showOnlineStatus by remember { mutableStateOf(prefs.getBoolean("show_online_status", true)) }
    var confirmExit by remember { mutableStateOf(prefs.getBoolean("confirm_exit", true)) }
    // Preferências adicionais preparadas para os módulos de streaming e controle.
    var bitrate by remember { mutableStateOf(prefs.getString("stream_bitrate", "Automático") ?: "Automático") }
    var reconnect by remember { mutableStateOf(prefs.getBoolean("auto_reconnect", true)) }
    var reconnectAttempts by remember { mutableStateOf(prefs.getString("reconnect_attempts", "5 tentativas") ?: "5 tentativas") }
    var showNetworkStats by remember { mutableStateOf(prefs.getBoolean("network_stats", true)) }
    var showPing by remember { mutableStateOf(prefs.getBoolean("show_ping", true)) }
    var packetBuffer by remember { mutableStateOf(prefs.getString("packet_buffer", "Automático") ?: "Automático") }
    var controllerVibration by remember { mutableStateOf(prefs.getBoolean("controller_vibration", true)) }
    var triggerSensitivity by remember { mutableStateOf(prefs.getString("trigger_sensitivity", "Normal") ?: "Normal") }
    var stickDeadzone by remember { mutableStateOf(prefs.getString("stick_deadzone", "Padrão (10%)") ?: "Padrão (10%)") }
    var touchSize by remember { mutableStateOf(prefs.getString("touch_size", "Normal") ?: "Normal") }
    var touchOpacity by remember { mutableStateOf(prefs.getString("touch_opacity", "75%") ?: "75%") }
    var showButtonLabels by remember { mutableStateOf(prefs.getBoolean("button_labels", true)) }
    var adaptiveTriggers by remember { mutableStateOf(prefs.getBoolean("adaptive_triggers", false)) }
    var audioMuteOnBackground by remember { mutableStateOf(prefs.getBoolean("mute_background", true)) }
    var spatialAudio by remember { mutableStateOf(prefs.getBoolean("spatial_audio", false)) }
    var subtitles by remember { mutableStateOf(prefs.getBoolean("subtitles", false)) }
    var subtitleSize by remember { mutableStateOf(prefs.getString("subtitle_size", "Médio") ?: "Médio") }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean("reduce_motion", false)) }
    var highContrast by remember { mutableStateOf(prefs.getBoolean("high_contrast", false)) }
    var screenReaderHints by remember { mutableStateOf(prefs.getBoolean("screen_reader_hints", true)) }
    var keepScreenOn by remember { mutableStateOf(prefs.getBoolean("keep_screen_on", true)) }
    var launchFullscreen by remember { mutableStateOf(prefs.getBoolean("launch_fullscreen", true)) }
    var allowBackgroundAudio by remember { mutableStateOf(prefs.getBoolean("background_audio", false)) }
    var showResetDialog by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("Todas as alterações são salvas automaticamente neste dispositivo.") }

    fun saveString(key: String, value: String) { prefs.edit().putString(key, value).apply(); feedback = "Configuração salva: $value" }
    fun saveBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply(); feedback = "Configuração atualizada." }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restaurar configurações?") },
            text = { Text("Todas as preferências do CloudPlay voltarão aos valores padrão. Sua conta e seus dados de login não serão apagados.") },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit().clear().apply()
                    resolution = "Automática"; fsr = "Desativado"; videoFit = "Ajustar à tela"
                    codec = "Automático"; fps = "Até 60 FPS"; performancePanel = true
                    panelLayout = "Compacto"; sessionMenu = true; engine = "Automático"
                    touchControls = true; vibration = true; interfaceSound = true; leftHanded = false
                    controllerLayout = "Padrão"; stickSensitivity = "Normal"
                    audioQuality = "Alta"; audioOutput = "Automática"; audioBalance = "Centralizado"
                    region = "Automática"; networkMode = "Automático"; gameLanguage = "Português"; dataSaver = false
                    appLanguage = "Português (Brasil)"; theme = "Escuro"; startupPage = "Início"
                    notifications = true; friendNotifications = true; promotionalNotifications = false
                    analytics = false; autoUpdates = true; clearCacheOnExit = false
                    showOnlineStatus = true; confirmExit = true
                    bitrate = "Automático"; reconnect = true; reconnectAttempts = "5 tentativas"
                    showNetworkStats = true; showPing = true; packetBuffer = "Automático"
                    controllerVibration = true; triggerSensitivity = "Normal"; stickDeadzone = "Padrão (10%)"
                    touchSize = "Normal"; touchOpacity = "75%"; showButtonLabels = true; adaptiveTriggers = false
                    audioMuteOnBackground = true; spatialAudio = false; subtitles = false; subtitleSize = "Médio"
                    reduceMotion = false; highContrast = false; screenReaderHints = true
                    keepScreenOn = true; launchFullscreen = true; allowBackgroundAudio = false
                    feedback = "Configurações restauradas para o padrão."
                    showResetDialog = false
                }) { Text("Restaurar") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Configurações", color = MainText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Controle cada detalhe da sua experiência no CloudPlay", color = SecondaryText, fontSize = 12.sp)
                Text(feedback, color = AccentColor, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        item { SettingsSection("Imagem e vídeo") {
            ChoiceSetting("Resolução preferida", resolution, listOf("Automática", "720p", "1080p", "1440p")) { resolution = it; saveString("resolution", it) }
            ChoiceSetting("Aprimoramento de imagem (FSR)", fsr, listOf("Desativado", "Qualidade", "Equilibrado", "Desempenho")) { fsr = it; saveString("fsr", it) }
            ChoiceSetting("Ajuste da imagem", videoFit, listOf("Ajustar à tela", "Preencher tela", "Proporção original")) { videoFit = it; saveString("video_fit", it) }
            ChoiceSetting("Codec de vídeo preferido", codec, listOf("Automático", "H.264", "H.265 / HEVC", "AV1")) { codec = it; saveString("codec", it) }
            ChoiceSetting("Limite de quadros", fps, listOf("Até 30 FPS", "Até 60 FPS", "Até 120 FPS", "Automático")) { fps = it; saveString("fps", it) }
        } }
        item { SettingsSection("Desempenho e sessão") {
            SettingSwitch("Painel de desempenho", performancePanel) { performancePanel = it; saveBoolean("performance_panel", it) }
            ChoiceSetting("Layout do painel", panelLayout, listOf("Completo", "Compacto", "Mínimo")) { panelLayout = it; saveString("panel_layout", it) }
            SettingSwitch("Menu durante a sessão", sessionMenu) { sessionMenu = it; saveBoolean("session_menu", it) }
            ChoiceSetting("Motor de streaming", engine, listOf("Automático", "Compatibilidade", "Desempenho")) { engine = it; saveString("engine", it) }
            SettingSwitch("Confirmar antes de sair de uma sessão", confirmExit) { confirmExit = it; saveBoolean("confirm_exit", it) }
        } }
        item { SettingsSection("Gamepad e controles") {
            SettingSwitch("Controles virtuais no celular", touchControls) { touchControls = it; saveBoolean("touch_controls", it) }
            SettingSwitch("Vibração", vibration) { vibration = it; saveBoolean("vibration", it) }
            SettingSwitch("Sons da interface", interfaceSound) { interfaceSound = it; saveBoolean("interface_sound", it) }
            SettingSwitch("Layout para canhotos", leftHanded) { leftHanded = it; saveBoolean("left_handed", it) }
            ChoiceSetting("Layout do controle", controllerLayout, listOf("Padrão", "Alternativo", "Personalizado")) { controllerLayout = it; saveString("controller_layout", it) }
            ChoiceSetting("Sensibilidade dos analógicos", stickSensitivity, listOf("Baixa", "Normal", "Alta")) { stickSensitivity = it; saveString("stick_sensitivity", it) }
            Text("Em celulares, os controles virtuais poderão ser exibidos durante o jogo. Em Android TV, o foco é controle físico ou controle remoto.", color = SecondaryText, fontSize = 11.sp)
        } }
        item { SettingsSection("Áudio") {
            ChoiceSetting("Qualidade preferida", audioQuality, listOf("Baixa", "Média", "Alta")) { audioQuality = it; saveString("audio_quality", it) }
            ChoiceSetting("Saída de áudio", audioOutput, listOf("Automática", "Alto-falante", "Fones de ouvido")) { audioOutput = it; saveString("audio_output", it) }
            ChoiceSetting("Balanço", audioBalance, listOf("Centralizado", "Priorizar esquerda", "Priorizar direita")) { audioBalance = it; saveString("audio_balance", it) }
        } }
        item { SettingsSection("Cloud e rede") {
            ChoiceSetting("Região preferida", region, listOf("Automática", "Brasil", "América do Sul", "Estados Unidos", "Europa", "Ásia")) { region = it; saveString("region", it) }
            ChoiceSetting("Modo de conexão", networkMode, listOf("Automático", "Estável", "Baixa latência", "Economizar dados")) { networkMode = it; saveString("network_mode", it) }
            SettingSwitch("Economia de dados", dataSaver) { dataSaver = it; saveBoolean("data_saver", it) }
            ChoiceSetting("Idioma preferido dos jogos", gameLanguage, listOf("Português", "English", "Español", "Francês", "Alemão", "Japonês")) { gameLanguage = it; saveString("game_language", it) }
        } }
        item { SettingsSection("Idioma e aparência") {
            ChoiceSetting("Idioma do aplicativo", appLanguage, listOf("Português (Brasil)", "English", "Español")) { appLanguage = it; saveString("app_language", it) }
            ChoiceSetting("Tema", theme, listOf("Escuro", "Claro", "Automático")) { theme = it; saveString("theme", it) }
            ChoiceSetting("Página inicial", startupPage, listOf("Início", "Minha Biblioteca", "Pesquisar")) { startupPage = it; saveString("startup_page", it) }
        } }
        item { SettingsSection("Notificações e privacidade") {
            SettingSwitch("Notificações do aplicativo", notifications) { notifications = it; saveBoolean("notifications", it) }
            SettingSwitch("Avisos de amigos", friendNotifications) { friendNotifications = it; saveBoolean("friend_notifications", it) }
            SettingSwitch("Novidades e promoções", promotionalNotifications) { promotionalNotifications = it; saveBoolean("promo_notifications", it) }
            SettingSwitch("Compartilhar dados de diagnóstico", analytics) { analytics = it; saveBoolean("analytics", it) }
            SettingSwitch("Mostrar meu status online", showOnlineStatus) { showOnlineStatus = it; saveBoolean("show_online_status", it) }
        } }
        item { SettingsSection("Armazenamento e atualizações") {
            SettingSwitch("Verificar atualizações automaticamente", autoUpdates) { autoUpdates = it; saveBoolean("auto_updates", it) }
            SettingSwitch("Limpar cache ao fechar o aplicativo", clearCacheOnExit) { clearCacheOnExit = it; saveBoolean("clear_cache_exit", it) }
            Text("O gerenciamento e a limpeza real do cache serão conectados ao módulo de armazenamento do aplicativo.", color = SecondaryText, fontSize = 11.sp)
        } }
        item { SettingsSection("Qualidade avançada da transmissão") {
            ChoiceSetting("Taxa de bits preferida", bitrate, listOf("Automático", "4 Mbps", "6 Mbps", "8 Mbps", "10 Mbps", "15 Mbps", "20 Mbps", "30 Mbps")) { bitrate = it; saveString("stream_bitrate", it) }
            ChoiceSetting("Buffer de rede", packetBuffer, listOf("Automático", "Mínimo (baixa latência)", "Balanceado", "Maior estabilidade")) { packetBuffer = it; saveString("packet_buffer", it) }
            SettingSwitch("Reconectar automaticamente após queda", reconnect) { reconnect = it; saveBoolean("auto_reconnect", it) }
            ChoiceSetting("Tentativas de reconexão", reconnectAttempts, listOf("3 tentativas", "5 tentativas", "10 tentativas", "Ilimitadas")) { reconnectAttempts = it; saveString("reconnect_attempts", it) }
            SettingSwitch("Exibir estatísticas de rede na sessão", showNetworkStats) { showNetworkStats = it; saveBoolean("network_stats", it) }
            SettingSwitch("Exibir ping/latência", showPing) { showPing = it; saveBoolean("show_ping", it) }
            Text("As escolhas ficam salvas para o futuro módulo de transmissão; bitrate, buffer e reconexão só terão efeito quando o player estiver conectado.", color = SecondaryText, fontSize = 11.sp)
        } }
        item { SettingsSection("Personalização do controle") {
            ChoiceSetting("Zona morta dos analógicos", stickDeadzone, listOf("Pequena (5%)", "Padrão (10%)", "Média (15%)", "Alta (20%)")) { stickDeadzone = it; saveString("stick_deadzone", it) }
            ChoiceSetting("Sensibilidade dos gatilhos", triggerSensitivity, listOf("Baixa", "Normal", "Alta")) { triggerSensitivity = it; saveString("trigger_sensitivity", it) }
            SettingSwitch("Vibração do controle conectado", controllerVibration) { controllerVibration = it; saveBoolean("controller_vibration", it) }
            SettingSwitch("Gatilhos adaptáveis (se compatível)", adaptiveTriggers) { adaptiveTriggers = it; saveBoolean("adaptive_triggers", it) }
            Text("Mapeamento de botões: Padrão do Xbox. Editor de mapeamento personalizado será conectado ao módulo de entrada.", color = SecondaryText, fontSize = 11.sp)
        } }
        item { SettingsSection("Controles virtuais na tela") {
            ChoiceSetting("Tamanho dos botões", touchSize, listOf("Pequeno", "Normal", "Grande", "Extra grande")) { touchSize = it; saveString("touch_size", it) }
            ChoiceSetting("Opacidade dos controles", touchOpacity, listOf("25%", "50%", "75%", "100%")) { touchOpacity = it; saveString("touch_opacity", it) }
            SettingSwitch("Mostrar letras dos botões", showButtonLabels) { showButtonLabels = it; saveBoolean("button_labels", it) }
            Text("Inclui opções para posição, tamanho e transparência. O editor visual e o arraste dos botões serão ligados ao overlay de controles virtuais.", color = SecondaryText, fontSize = 11.sp)
        } }
        item { SettingsSection("Áudio e legendas") {
            SettingSwitch("Silenciar áudio ao sair do aplicativo", audioMuteOnBackground) { audioMuteOnBackground = it; saveBoolean("mute_background", it) }
            SettingSwitch("Áudio espacial (se compatível)", spatialAudio) { spatialAudio = it; saveBoolean("spatial_audio", it) }
            SettingSwitch("Ativar legendas (se disponíveis no jogo)", subtitles) { subtitles = it; saveBoolean("subtitles", it) }
            ChoiceSetting("Tamanho das legendas", subtitleSize, listOf("Pequeno", "Médio", "Grande", "Muito grande")) { subtitleSize = it; saveString("subtitle_size", it) }
        } }
        item { SettingsSection("Acessibilidade") {
            SettingSwitch("Reduzir animações", reduceMotion) { reduceMotion = it; saveBoolean("reduce_motion", it) }
            SettingSwitch("Alto contraste", highContrast) { highContrast = it; saveBoolean("high_contrast", it) }
            SettingSwitch("Dicas para leitor de tela", screenReaderHints) { screenReaderHints = it; saveBoolean("screen_reader_hints", it) }
        } }
        item { SettingsSection("Tela e comportamento do aplicativo") {
            SettingSwitch("Manter tela ligada durante o jogo", keepScreenOn) { keepScreenOn = it; saveBoolean("keep_screen_on", it) }
            SettingSwitch("Iniciar em tela cheia", launchFullscreen) { launchFullscreen = it; saveBoolean("launch_fullscreen", it) }
            SettingSwitch("Permitir áudio em segundo plano", allowBackgroundAudio) { allowBackgroundAudio = it; saveBoolean("background_audio", it) }
            Text("A aplicação dessas preferências será feita pelos módulos de janela, ciclo de vida e player.", color = SecondaryText, fontSize = 11.sp)
        } }
        item { SettingsSection("Conta, suporte e aplicativo") {
            Text("Conta Microsoft e Xbox", color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Acesse Perfil para conectar ou desconectar sua conta.", color = SecondaryText, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("Permissões e informações do Android", color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Abra as configurações do CloudPlay no Android para gerenciar permissões e notificações do sistema.", color = SecondaryText, fontSize = 12.sp)
            OutlinedButton(onClick = {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }, modifier = Modifier.fillMaxWidth()) { Text("Abrir configurações do Android") }
            Spacer(Modifier.height(5.dp))
            Text("Sobre o CloudPlay", color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Aplicativo de interface de jogos em nuvem. As integrações de streaming e serviços externos serão conectadas em etapas posteriores.", color = SecondaryText, fontSize = 12.sp)
        } }
        item {
            OutlinedButton(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Restaurar todas as configurações")
            }
            Text("As preferências são salvas automaticamente neste dispositivo. Opções como resolução, FSR, codec, FPS, região e qualidade de áudio ficam preparadas e salvas, mas só controlarão a transmissão quando o player e o servidor de streaming forem integrados. A tela de configurações não cria nem fornece acesso ao Xbox Cloud Gaming.", color = SecondaryText, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = AccentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        content()
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MainText,
            fontSize = 13.sp
        )

        Switch(
            checked = checked,
            onCheckedChange = onChange
        )
    }
}

@Composable
private fun ChoiceSetting(
    title: String,
    value: String,
    options: List<String>,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    ) {
        Text(
            text = title,
            color = MainText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(7.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(SurfaceColor)
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentColor
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column {
            Text(
                text = title,
                color = MainText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = SecondaryText,
                fontSize = 11.sp
            )
        }
    }
}

                  
