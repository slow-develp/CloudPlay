
package com.cloudplay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration

private val Bg = Color(0xFF080B0A)
private val Panel = Color(0xFF121914)
private val Green = Color(0xFF19E63B)
private val Muted = Color(0xFF9BA69E)
private val White = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CloudPlayApp() }
    }
}

private data class GameItem(
    val name: String,
    val category: String,
    val start: Color,
    val end: Color
)

private val games = listOf(
    GameItem("FORTNITE", "Ação e batalha", Color(0xFF3927A8), Color(0xFF15102F)),
    GameItem("CALL OF DUTY", "Tiro e ação", Color(0xFF9B4B13), Color(0xFF24140B)),
    GameItem("MINECRAFT", "Aventura", Color(0xFF398CBA), Color(0xFF16442D)),
    GameItem("ROBLOX", "Experiências", Color(0xFF454545), Color(0xFF101010)),
    GameItem("GTA V", "Mundo aberto", Color(0xFF9B6A28), Color(0xFF24170E)),
    GameItem("FORZA", "Corrida", Color(0xFF176A9C), Color(0xFF101D38)),
    GameItem("STARFIELD", "Exploração", Color(0xFF39465B), Color(0xFF10141F))
)

@Composable
fun CloudPlayApp() {
    var page by remember { mutableStateOf("Início") }
    var notice by remember {
        mutableStateOf("Bem-vindo ao CloudPlay.")
    }

    val isTv = LocalConfiguration.current.screenWidthDp >= 700

    val phoneTabs = listOf("Início", "Biblioteca", "Explorar", "Perfil")
    val tvTabs = listOf(
        "Início", "Biblioteca", "Game Pass",
        "Pesquisar", "Perfil", "Configurações"
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Green,
            background = Bg,
            surface = Panel
        )
    ) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                if (!isTv) {
                    NavigationBar(
                        containerColor = Color(0xFF101510)
                    ) {
                        phoneTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = page == tab,
                                onClick = { page = tab },
                                icon = {
                                    Icon(
                                        navIcon(tab),
                                        contentDescription = tab
                                    )
                                },
                                label = {
                                    Text(tab, fontSize = 10.sp)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Green,
                                    selectedTextColor = Green,
                                    indicatorColor = Color(0xFF203B25),
                                    unselectedIconColor = Muted,
                                    unselectedTextColor = Muted
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->

            if (isTv) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Sidebar(
                        page = page,
                        tabs = tvTabs,
                        onSelect = { page = it },
                        isTv = true
                    )

                    MainContent(
                        page = page,
                        notice = notice,
                        isTv = true,
                        onLogin = {
                            notice =
                                "A autenticação Microsoft oficial ainda precisa ser configurada."
                        },
                        onExplore = { page = "Explorar" },
                        onLibrary = { page = "Biblioteca" }
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Header(
                        onLogin = {
                            notice =
                                "A autenticação Microsoft oficial ainda precisa ser configurada."
                        }
                    )

                    MainContent(
                        page = page,
                        notice = notice,
                        isTv = false,
                        onLogin = {
                            notice =
                                "A autenticação Microsoft oficial ainda precisa ser configurada."
                        },
                        onExplore = { page = "Explorar" },
                        onLibrary = { page = "Biblioteca" }
                    )
                }
            }
        }
    }
}

private fun navIcon(tab: String): ImageVector {
    return when (tab) {
        "Início" -> Icons.Filled.Home
        "Biblioteca" -> Icons.Filled.Apps
        "Game Pass" -> Icons.Filled.SportsEsports
        "Explorar", "Pesquisar" -> Icons.Filled.Search
        "Perfil" -> Icons.Filled.AccountCircle
        else -> Icons.Filled.Settings
    }
}

@Composable
private fun CloudLogo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.CloudQueue,
                contentDescription = "CloudPlay",
                tint = Green,
                modifier = Modifier.size(43.dp)
            )
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(22.dp)
            )
        }

        Row {
            Text(
                "Cloud",
                color = White,
                fontWeight = FontWeight.Black,
                fontSize = 23.sp
            )
            Text(
                "Play",
                color = Green,
                fontWeight = FontWeight.Black,
                fontSize = 23.sp
            )
        }
    }
}

@Composable
private fun Header(onLogin: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Bg)
            .padding(horizontal = 17.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CloudLogo()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Pesquisar",
                    tint = White
                )
            }

            Surface(
                onClick = onLogin,
                color = Panel,
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(27.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "Entrar",
                        color = White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    page: String,
    tabs: List<String>,
    onSelect: (String) -> Unit,
    isTv: Boolean
) {
    Column(
        Modifier
            .width(if (isTv) 225.dp else 180.dp)
            .fillMaxHeight()
            .background(Color(0xFF0C110E))
            .padding(horizontal = 13.dp, vertical = 23.dp)
    ) {
        CloudLogo()

        Spacer(Modifier.height(42.dp))

        tabs.forEach { tab ->
            var focused by remember { mutableStateOf(false) }
            val selected = page == tab

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (selected) Color(0xFF174D23)
                        else Color.Transparent
                    )
                    .border(
                        width = if (focused) 2.dp else 0.dp,
                        color = if (focused) Green else Color.Transparent,
                        shape = RoundedCornerShape(13.dp)
                    )
                    .onFocusChanged { focused = it.isFocused }
                    .clickable { onSelect(tab) }
                    .focusable()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    navIcon(tab),
                    contentDescription = tab,
                    tint = if (selected) Green else Muted,
                    modifier = Modifier.size(23.dp)
                )

                Spacer(Modifier.width(13.dp))

                Text(
                    tab,
                    color = if (selected) White else Muted,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Column(
            Modifier
                .fillMaxWidth()
                .background(Panel, RoundedCornerShape(15.dp))
                .padding(13.dp)
        ) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "CloudPlay",
                color = White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Sua experiência de jogos na nuvem.",
                color = Muted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MainContent(
    page: String,
    notice: String,
    isTv: Boolean,
    onLogin: () -> Unit,
    onExplore: () -> Unit,
    onLibrary: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = if (isTv) 25.dp else 15.dp,
                vertical = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (page) {
            "Início" -> HomeScreen(
                isTv,
                onLogin,
                onExplore,
                onLibrary
            )

            "Biblioteca" -> EmptyScreen(
                "Minha biblioteca",
                "Seus jogos aparecerão aqui quando uma fonte autorizada de dados estiver conectada.",
                "Biblioteca ainda não conectada"
            )

            "Explorar", "Pesquisar" -> EmptyScreen(
                "Explorar jogos",
                "O catálogo será conectado em uma próxima etapa.",
                "Catálogo ainda não conectado"
            )

            "Game Pass" -> EmptyScreen(
                "Game Pass",
                "Esta área poderá mostrar informações disponíveis por integrações autorizadas.",
                "Serviço ainda não conectado"
            )

            "Perfil" -> EmptyScreen(
                "Seu perfil",
                "Conecte sua conta Microsoft quando a autenticação oficial estiver configurada.",
                "Conta não conectada"
            )

            else -> EmptyScreen(
                "Configurações",
                "As configurações do CloudPlay serão adicionadas aqui.",
                "Em desenvolvimento"
            )
        }

        Text(
            notice,
            color = Muted,
            fontSize = 11.sp
        )

        Text(
            "CLOUDPLAY • VERSÃO 0.2.0",
            color = Color(0xFF536057),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HomeScreen(
    isTv: Boolean,
    onLogin: () -> Unit,
    onExplore: () -> Unit,
    onLibrary: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(21.dp)
    ) {
        HeroBanner(isTv, onLogin)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Sua experiência começa aqui",
                    color = White,
                    fontSize = if (isTv) 25.sp else 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Descubra o universo CloudPlay",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(30.dp)
            )
        }

        GameSection(
            "Jogos em destaque",
            games,
            isTv
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                "Minha biblioteca",
                "Seus jogos conectados",
                Modifier.weight(1f),
                onLibrary
            )

            ActionCard(
                "Explorar",
                "Descubra mais",
                Modifier.weight(1f),
                onExplore
            )
        }

        Surface(
            color = Panel,
            shape = RoundedCornerShape(17.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogin)
                    .padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "Conta Microsoft",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Conexão oficial ainda em configuração",
                        color = Muted,
                        fontSize = 11.sp
                    )
                }

                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Green
                )
            }
        }
    }
}

@Composable
private fun HeroBanner(
    isTv: Boolean,
    onLogin: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (isTv) 285.dp else 250.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF063D19),
                        Color(0xFF10291A),
                        Color(0xFF080F0B),
                        Color(0xFF15251B)
                    )
                )
            )
            .border(
                1.dp,
                Color(0xFF285D32),
                RoundedCornerShape(20.dp)
            )
            .padding(if (isTv) 30.dp else 20.dp)
    ) {
        Icon(
            Icons.Filled.CloudQueue,
            contentDescription = null,
            tint = Color(0x3325F34A),
            modifier = Modifier
                .size(if (isTv) 260.dp else 180.dp)
                .align(Alignment.CenterEnd)
        )

        Column(
            Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(
                "BEM-VINDO AO CLOUDPLAY",
                color = Green,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp
            )

            Text(
                "Seus jogos.\nSeu universo.",
                color = White,
                fontWeight = FontWeight.Black,
                fontSize = if (isTv) 39.sp else 31.sp,
                lineHeight = if (isTv) 44.sp else 35.sp
            )

            Text(
                "Uma nova experiência de jogos na nuvem.",
                color = Color(0xFFD0DDD2),
                fontSize = if (isTv) 14.sp else 11.sp
            )

            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "COMEÇAR",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun GameSection(
    title: String,
    items: List<GameItem>,
    isTv: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(25.dp)
                        .background(Green, RoundedCornerShape(5.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    color = White,
                    fontSize = if (isTv) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "VER TODOS  →",
                color = Green,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { game ->
                GameCard(game, isTv)
            }
        }

        Text(
            "Vitrine demonstrativa • jogos não conectados à biblioteca",
            color = Color(0xFF657168),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun GameCard(
    game: GameItem,
    isTv: Boolean
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        Modifier
            .width(if (isTv) 185.dp else 145.dp)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (isTv) 220.dp else 175.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.linearGradient(
                        listOf(game.start, game.end)
                    )
                )
                .border(
                    if (focused) 3.dp else 1.dp,
                    if (focused) Green else Color(0xFF39443B),
                    RoundedCornerShape(13.dp)
                )
                .padding(12.dp)
        ) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = Color(0x55FFFFFF),
                modifier = Modifier
                    .size(55.dp)
                    .align(Alignment.Center)
            )

            Text(
                game.name,
                color = White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        Text(
            game.category,
            color = Muted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(27.dp)
            )
            Text(
                title,
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                subtitle,
                color = Muted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun EmptyScreen(
    title: String,
    description: String,
    state: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(18.dp))
            .padding(23.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Icon(
            Icons.Filled.CloudQueue,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(42.dp)
        )

        Text(
            title,
            color = White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            description,
            color = Muted,
            fontSize = 13.sp
        )

        HorizontalDivider(color = Color(0xFF303B32))

        Text(
            state,
            color = Color(0xFFFFD27A),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
