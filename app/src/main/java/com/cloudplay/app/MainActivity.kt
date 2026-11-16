
package com.cloudplay.app

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.ComponentActivity
import android.os.Bundle
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import java.net.URL

private val Background = Color(0xFF080B0D)
private val Sidebar = Color(0xFF080D0B)
private val Panel = Color(0xFF151A1D)
private val White = Color(0xFFFFFFFF)
private val Muted = Color(0xFFADB5BA)
private val Green = Color(0xFF55E52D)
private val Border = Color(0xFF30383D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CloudPlayApp() }
    }
}

@Composable
fun CloudPlayApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity
    val uiManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager

    val isTv = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
            uiManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    var account by rememberSaveable { mutableStateOf<String?>(null) }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var notice by rememberSaveable { mutableStateOf("") }
    var page by rememberSaveable { mutableStateOf("Início") }

    var quality by rememberSaveable { mutableStateOf("1080p") }
    var dataSaver by rememberSaveable { mutableStateOf(false) }
    var autoPlay by rememberSaveable { mutableStateOf(false) }
    var touchControls by rememberSaveable { mutableStateOf(!isTv) }
    var vibration by rememberSaveable { mutableStateOf(true) }
    var sound by rememberSaveable { mutableStateOf(true) }
    var region by rememberSaveable { mutableStateOf("Brasil") }
    var language by rememberSaveable { mutableStateOf("Português") }
    var gameLanguage by rememberSaveable { mutableStateOf("Idioma do jogo") }

    val auth = remember(activity) {
        activity?.let {
            MicrosoftAuth(
                activity = it,
                onSignedIn = { username ->
                    account = username
                    notice = "Conta Microsoft conectada."
                },
                onSignedOut = {
                    account = null
                    photo = null
                    notice = "Sessão encerrada."
                },
                onMessage = { notice = it },
                onPhotoLoaded = { photo = it }
            )
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Green,
            background = Background,
            surface = Panel,
            onBackground = White,
            onSurface = White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            if (isTv) {
                TvInterface(
                    page, { page = it },
                    account, photo,
                    { auth?.signIn() },
                    { auth?.signOut() },
                    notice,
                    quality, { quality = it },
                    dataSaver, { dataSaver = it },
                    autoPlay, { autoPlay = it },
                    vibration, { vibration = it },
                    sound, { sound = it },
                    region, { region = it },
                    language, { language = it },
                    gameLanguage, { gameLanguage = it }
                )
            } else {
                MobileInterface(
                    page, { page = it },
                    account, photo,
                    { auth?.signIn() },
                    { auth?.signOut() },
                    notice,
                    quality, { quality = it },
                    dataSaver, { dataSaver = it },
                    autoPlay, { autoPlay = it },
                    touchControls, { touchControls = it },
                    vibration, { vibration = it },
                    sound, { sound = it },
                    region, { region = it },
                    language, { language = it },
                    gameLanguage, { gameLanguage = it }
                )
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
    "Configurações"
)

@Composable
private fun CloudLogo(size: Dp = 45.dp) {
    Image(
        painter = painterResource(R.drawable.cloudplay_icon),
        contentDescription = "CloudPlay",
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(9.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun TvInterface(
    page: String,
    onPage: (String) -> Unit,
    account: String?,
    photo: Bitmap?,
    login: () -> Unit,
    logout: () -> Unit,
    notice: String,
    quality: String,
    setQuality: (String) -> Unit,
    dataSaver: Boolean,
    setDataSaver: (Boolean) -> Unit,
    autoPlay: Boolean,
    setAutoPlay: (Boolean) -> Unit,
    vibration: Boolean,
    setVibration: (Boolean) -> Unit,
    sound: Boolean,
    setSound: (Boolean) -> Unit,
    region: String,
    setRegion: (String) -> Unit,
    language: String,
    setLanguage: (String) -> Unit,
    gameLanguage: String,
    setGameLanguage: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // MENU LATERAL DA TV
        Column(
            Modifier
                .width(166.dp)
                .fillMaxHeight()
                .background(Sidebar)
                .padding(horizontal = 11.dp, vertical = 15.dp)
        ) {
            CloudLogo(126.dp)

            Spacer(Modifier.height(27.dp))

            menuItems.forEach { item ->
                TvMenuItem(
                    item = item,
                    selected = page == item,
                    onClick = { onPage(item) }
                )
                Spacer(Modifier.height(7.dp))
            }

            Spacer(Modifier.weight(1f))

            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF24452A), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.SportsEsports,
                    null,
                    tint = Green,
                    modifier = Modifier.size(29.dp)
                )
                Text(
                    "CloudPlay",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    "Jogue mais.",
                    color = Muted,
                    fontSize = 10.sp
                )
            }
        }

        // CONTEÚDO PRINCIPAL
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 18.dp, end = 20.dp, top = 12.dp)
        ) {
            TvTopBar(account, photo, login)

            if (notice.isNotBlank()) {
                Text(
                    notice,
                    color = Green,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(Modifier.weight(1f)) {
                when (page) {
                    "Início" -> TvHome()
                    "Minha Biblioteca", "Game Pass", "Loja" -> TvCatalogPage(page)
                    "Pesquisar" -> SearchPage()
                    "Configurações" -> SettingsPage(
                        true, quality, setQuality,
                        dataSaver, setDataSaver,
                        autoPlay, setAutoPlay,
                        false, {},
                        vibration, setVibration,
                        sound, setSound,
                        region, setRegion,
                        language, setLanguage,
                        gameLanguage, setGameLanguage
                    )
                }
            }
        }
    }
}

@Composable
private fun TvMenuItem(
    item: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    val icon = when (item) {
        "Início" -> Icons.Default.Home
        "Minha Biblioteca" -> Icons.Default.GridView
        "Game Pass" -> Icons.Default.SportsEsports
        "Pesquisar" -> Icons.Default.Search
        "Loja" -> Icons.Default.ShoppingBag
        else -> Icons.Default.Settings
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    focused -> White
                    selected -> Color(0xFF252B2F)
                    else -> Color.Transparent
                }
            )
            .border(
                if (focused) 2.dp else 0.dp,
                if (focused) White else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (focused) Background else if (selected) White else Muted,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            item,
            color = if (focused) Background else White,
            fontSize = 11.sp,
            fontWeight = if (selected || focused) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun TvTopBar(
    account: String?,
    photo: Bitmap?,
    login: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Default.Search, null, tint = White)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.NotificationsNone, null, tint = White)
        }
        Spacer(Modifier.width(9.dp))
        Avatar(photo, 34.dp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                if (account == null) "Entrar na conta" else "Conta conectada",
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Microsoft",
                color = Muted,
                fontSize = 9.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = login) {
            Icon(Icons.Default.ChevronRight, null, tint = White)
        }
    }
}

@Composable
private fun TvHome() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp)
    ) {
        // BANNER PRINCIPAL
        Box(
            Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF071B13),
                            Color(0xFF17472A),
                            Color(0xFF07100D)
                        )
                    )
                )
        ) {
            Icon(
                Icons.Default.SportsEsports,
                null,
                tint = Color(0xFF7CCB65).copy(alpha = 0.16f),
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterEnd)
            )

            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 23.dp)
            ) {
                Text(
                    "Bem-vindo de volta!",
                    color = White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "Jogue seus jogos favoritos\nem qualquer lugar.",
                    color = Color(0xFFD0D7D2),
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green,
                            contentColor = Background
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 17.dp,
                            vertical = 7.dp
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Jogar Agora", fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = {},
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(9.dp)
                    ) {
                        Icon(Icons.Default.MoreHoriz, null, tint = White)
                    }
                }
            }
        }

        Spacer(Modifier.height(13.dp))

        GameRow(
            title = "Game Pass",
            games = listOf(
                "FORTNITE",
                "CALL OF DUTY\nBLACK OPS",
                "MINECRAFT",
                "ROBLOX",
                "GRAND THEFT\nAUTO V",
                "EA SPORTS\nFC 25",
                "STARFIELD"
            )
        )

        Spacer(Modifier.height(13.dp))

        GameRow(
            title = "Em Alta",
            games = listOf(
                "THE WITCHER",
                "RED DEAD\nREDEMPTION 2",
                "FORZA\nHORIZON 5",
                "HOGWARTS\nLEGACY",
                "FALL GUYS",
                "GENSHIN\nIMPACT",
                "UFC 5"
            )
        )
    }
}

@Composable
private fun GameRow(
    title: String,
    games: List<String>
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(19.dp)
                    .background(Green, RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(9.dp))
            Text(
                title,
                color = White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Ver todos  →",
                color = Muted,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            games.forEachIndexed { index, game ->
                GameTile(game, index)
            }
        }
    }
}

@Composable
private fun GameTile(
    title: String,
    index: Int
) {
    var focused by remember { mutableStateOf(false) }

    val backgrounds = listOf(
        Color(0xFF35216A),
        Color(0xFF36251B),
        Color(0xFF315C3C),
        Color(0xFF293B58),
        Color(0xFF67412A),
        Color(0xFF263C62),
        Color(0xFF27343B)
    )

    Box(
        Modifier
            .width(91.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(backgrounds[index % backgrounds.size])
            .border(
                if (focused) 3.dp else 1.dp,
                if (focused) White else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(9.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable { }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.SportsEsports,
                null,
                tint = White.copy(alpha = 0.65f),
                modifier = Modifier.size(25.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                color = White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TvCatalogPage(page: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            page,
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Explore os jogos disponíveis",
            color = Muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 5.dp, bottom = 14.dp)
        )
        XboxCatalogScreen()
    }
}

@Composable
private fun SearchPage() {
    var query by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Pesquisar jogos", color = White, fontSize = 24.sp)
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Digite o nome de um jogo...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
    }
}


@Composable
private fun MobileInterface(
    page: String,
    onPage: (String) -> Unit,
    account: String?,
    photo: Bitmap?,
    login: () -> Unit,
    logout: () -> Unit,
    notice: String,
    quality: String,
    setQuality: (String) -> Unit,
    dataSaver: Boolean,
    setDataSaver: (Boolean) -> Unit,
    autoPlay: Boolean,
    setAutoPlay: (Boolean) -> Unit,
    touchControls: Boolean,
    setTouchControls: (Boolean) -> Unit,
    vibration: Boolean,
    setVibration: (Boolean) -> Unit,
    sound: Boolean,
    setSound: (Boolean) -> Unit,
    region: String,
    setRegion: (String) -> Unit,
    language: String,
    setLanguage: (String) -> Unit,
    gameLanguage: String,
    setGameLanguage: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CloudLogo(42.dp)
            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "CLOUDPLAY",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Seu universo de jogos",
                    color = Muted,
                    fontSize = 10.sp
                )
            }

            if (account == null) {
                Button(
                    onClick = login,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = Background
                    )
                ) {
                    Text("Entrar")
                }
            } else {
                Avatar(photo, 37.dp)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            menuItems.forEach { item ->
                FilterChip(
                    selected = page == item,
                    onClick = { onPage(item) },
                    label = {
                        Text(item, fontSize = 11.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = White,
                        selectedLabelColor = Background
                    )
                )
            }

            FilterChip(
                selected = page == "Perfil",
                onClick = { onPage("Perfil") },
                label = { Text("Perfil", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = White,
                    selectedLabelColor = Background
                )
            )
        }

        if (notice.isNotBlank()) {
            Text(
                notice,
                color = Green,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 5.dp
                )
            )
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (page) {
                "Início" -> MobileHome()

                "Minha Biblioteca",
                "Game Pass",
                "Loja" -> TvCatalogPage(page)

                "Pesquisar" -> SearchPage()

                "Perfil" -> ProfileScreen(
                    account = account,
                    profilePhoto = photo,
                    onLogin = login,
                    onLogout = logout
                )

                "Configurações" -> SettingsPage(
                    false,
                    quality,
                    setQuality,
                    dataSaver,
                    setDataSaver,
                    autoPlay,
                    setAutoPlay,
                    touchControls,
                    setTouchControls,
                    vibration,
                    setVibration,
                    sound,
                    setSound,
                    region,
                    setRegion,
                    language,
                    setLanguage,
                    gameLanguage,
                    setGameLanguage
                )
            }
        }
    }
}

@Composable
private fun MobileHome() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0D311D),
                            Color(0xFF101915),
                            Color(0xFF253B29)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    "CLOUD GAMING",
                    color = Green,
                    fontSize = 10.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Bem-vindo\nde volta!",
                    color = White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    "Seu próximo jogo começa aqui.",
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Icon(
                Icons.Default.SportsEsports,
                null,
                tint = Green.copy(alpha = 0.18f),
                modifier = Modifier
                    .size(95.dp)
                    .align(Alignment.CenterEnd)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "Game Pass",
            color = White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(9.dp))

        MobileGameRow(
            listOf(
                "Fortnite",
                "Minecraft",
                "Roblox",
                "GTA V",
                "FC 25"
            )
        )

        Spacer(Modifier.height(18.dp))

        Text(
            "Em Alta",
            color = White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(9.dp))

        MobileGameRow(
            listOf(
                "The Witcher",
                "Red Dead 2",
                "Forza 5",
                "Hogwarts",
                "Fall Guys"
            )
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Catálogo completo",
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(Modifier.height(10.dp))

        XboxCatalogScreen()
    }
}

@Composable
private fun MobileGameRow(games: List<String>) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        games.forEachIndexed { index, game ->
            Box(
                Modifier
                    .width(105.dp)
                    .height(125.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        listOf(
                            Color(0xFF35216A),
                            Color(0xFF315C3C),
                            Color(0xFF293B58),
                            Color(0xFF67412A)
                        )[index % 4]
                    )
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    game,
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun Avatar(
    bitmap: Bitmap?,
    size: Dp
) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto da conta Microsoft",
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(Panel),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                null,
                tint = White
            )
        }
    }
}

@Composable
private fun SettingsPage(
    isTv: Boolean,
    quality: String,
    setQuality: (String) -> Unit,
    dataSaver: Boolean,
    setDataSaver: (Boolean) -> Unit,
    autoPlay: Boolean,
    setAutoPlay: (Boolean) -> Unit,
    touchControls: Boolean,
    setTouchControls: (Boolean) -> Unit,
    vibration: Boolean,
    setVibration: (Boolean) -> Unit,
    sound: Boolean,
    setSound: (Boolean) -> Unit,
    region: String,
    setRegion: (String) -> Unit,
    language: String,
    setLanguage: (String) -> Unit,
    gameLanguage: String,
    setGameLanguage: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Configurações",
            color = White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Personalize sua experiência.",
            color = Muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(
                top = 5.dp,
                bottom = 16.dp
            )
        )

        SettingSection("Qualidade de imagem") {
            Text(
                "Resolução preferida",
                color = White,
                fontSize = 13.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("720p", "1080p", "1440p").forEach {
                    FilterChip(
                        selected = quality == it,
                        onClick = { setQuality(it) },
                        label = { Text(it) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = White,
                            selectedLabelColor = Background
                        )
                    )
                }
            }

            SettingSwitch(
                "Economia de dados",
                dataSaver,
                setDataSaver
            )
        }

        SettingSection("Preferências de jogo") {
            SettingSwitch(
                "Reprodução automática",
                autoPlay,
                setAutoPlay
            )

            if (!isTv) {
                SettingSwitch(
                    "Controles virtuais",
                    touchControls,
                    setTouchControls
                )
            } else {
                Text(
                    "TV: use um controle físico ou o controle remoto.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            SettingSwitch(
                "Vibração",
                vibration,
                setVibration
            )

            SettingSwitch(
                "Efeitos sonoros",
                sound,
                setSound
            )
        }

        SettingSection("Região e idioma") {
            ChoiceSetting(
                "Região",
                region,
                listOf(
                    "Brasil",
                    "Portugal",
                    "Estados Unidos"
                ),
                setRegion
            )

            ChoiceSetting(
                "Idioma do CloudPlay",
                language,
                listOf(
                    "Português",
                    "English",
                    "Español"
                ),
                setLanguage
            )

            ChoiceSetting(
                "Idioma dos jogos",
                gameLanguage,
                listOf(
                    "Idioma do jogo",
                    "Português",
                    "English",
                    "Español"
                ),
                setGameLanguage
            )
        }

        Text(
            "Essas opções são preferências da interface. Algumas só terão efeito real quando a infraestrutura de streaming estiver integrada.",
            color = Muted,
            fontSize = 11.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 13.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Panel)
            .border(
                1.dp,
                Border,
                RoundedCornerShape(15.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            title,
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(10.dp))

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
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Background,
                checkedTrackColor = Green
            )
        )
    }
}

@Composable
private fun ChoiceSetting(
    title: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            title,
            color = White,
            fontSize = 12.sp
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selected,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.Default.ArrowDropDown,
                    null
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
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

   
