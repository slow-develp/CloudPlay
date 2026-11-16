
package com.cloudplay.app

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF080D09)
private val Panel = Color(0xFF121A14)
private val Green = Color(0xFF8BFF4D)
private val White = Color(0xFFF5F8F5)
private val Muted = Color(0xFF9AA69C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloudPlayApp()
        }
    }
}

private data class GameItem(
    val name: String,
    val category: String,
    val start: Color,
    val end: Color
)

private val games = listOf(
    GameItem("FORTNITE", "Battle Royale", Color(0xFF352A70), Color(0xFF111B48)),
    GameItem("FORZA", "Corrida", Color(0xFF176B50), Color(0xFF102A29)),
    GameItem("GTA V", "Ação e aventura", Color(0xFF79502B), Color(0xFF282018)),
    GameItem("MINECRAFT", "Sobrevivência", Color(0xFF47733C), Color(0xFF1C3520)),
    GameItem("FIFA", "Esportes", Color(0xFF176B78), Color(0xFF102B3A)),
    GameItem("ROBLOX", "Aventura", Color(0xFF733B3B), Color(0xFF321D25))
)

@Composable
private fun CloudPlayApp() {
    val activity = LocalContext.current as Activity
    val configuration = LocalConfiguration.current
    val isTv = configuration.screenWidthDp >= 700

    var account by remember { mutableStateOf<String?>(null) }
    var profilePhoto by remember { mutableStateOf<Bitmap?>(null) }
    var notice by remember { mutableStateOf("Bem-vindo ao CloudPlay!") }
    var page by remember { mutableStateOf("Início") }

    val microsoftAuth = remember(activity) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Header(
            account = account,
            photo = profilePhoto,
            onProfile = { page = "Perfil" },
            onLogin = { microsoftAuth.signIn() }
        )

        NavigationMenu(
            selected = page,
            onSelect = { page = it }
        )

        MainContent(
            page = page,
            account = account,
            photo = profilePhoto,
            notice = notice,
            isTv = isTv,
            onLogin = { microsoftAuth.signIn() },
            onLogout = { microsoftAuth.signOut() },
            onNavigate = { page = it }
        )
    }
}

@Composable
private fun ProfileAvatar(
    photo: Bitmap?,
    size: Int,
    modifier: Modifier = Modifier
) {
    if (photo != null) {
        Image(
            bitmap = photo.asImageBitmap(),
            contentDescription = "Foto da conta Microsoft",
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .border(2.dp, Green, CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color(0xFF26372A))
                .border(2.dp, Green, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Perfil",
                tint = Green,
                modifier = Modifier.size((size * 0.55f).dp)
            )
        }
    }
}

@Composable
private fun Header(
    account: String?,
    photo: Bitmap?,
    onProfile: () -> Unit,
    onLogin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D140F))
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Cloud,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(29.dp)
            )

            Spacer(Modifier.width(9.dp))

            Column {
                Text(
                    "CLOUDPLAY",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "JOGUE SEM LIMITES",
                    color = Green,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (account != null) {
            TextButton(onClick = onProfile) {
                ProfileAvatar(photo = photo, size = 34)
                Spacer(Modifier.width(7.dp))
                Text(
                    "PERFIL",
                    color = White,
                    fontSize = 11.sp
                )
            }
        } else {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(
                    horizontal = 13.dp,
                    vertical = 8.dp
                )
            ) {
                Text(
                    "ENTRAR",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun NavigationMenu(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("Início", "Biblioteca", "Cloud Gaming", "Perfil")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF0D140F))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            TextButton(onClick = { onSelect(option) }) {
                Text(
                    option.uppercase(),
                    color = if (selected == option) Green else Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    HorizontalDivider(color = Color(0xFF263229))
}

@Composable
private fun MainContent(
    page: String,
    account: String?,
    photo: Bitmap?,
    notice: String,
    isTv: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = if (isTv) 35.dp else 17.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        when (page) {
            "Início" -> HomeScreen(
                account = account,
                isTv = isTv,
                onLogin = onLogin,
                onNavigate = onNavigate
            )

            "Biblioteca" -> EmptyScreen(
                title = "Sua biblioteca",
                description = "Seus jogos aparecerão aqui quando houver uma conexão de biblioteca disponível.",
                state = "A biblioteca ainda não está conectada."
            )

            "Cloud Gaming" -> EmptyScreen(
                title = "Cloud Gaming",
                description = "Esta área será usada para acessar sessões de jogos na nuvem.",
                state = "O serviço de streaming ainda não está conectado."
            )

            "Perfil" -> ProfileScreen(
                account = account,
                photo = photo,
                notice = notice,
                onLogin = onLogin,
                onLogout = onLogout
            )

            else -> HomeScreen(
                account = account,
                isTv = isTv,
                onLogin = onLogin,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun HomeScreen(
    account: String?,
    isTv: Boolean,
    onLogin: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        HeroBanner(
            account = account,
            isTv = isTv,
            onLogin = onLogin
        )

        GameSection(
            title = "JOGOS EM DESTAQUE",
            games = games,
            isTv = isTv
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "EXPLORE O CLOUDPLAY",
                color = White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "BIBLIOTECA",
                    subtitle = "Seus jogos",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Biblioteca") }
                )

                ActionCard(
                    title = "CLOUD GAMING",
                    subtitle = "Jogar na nuvem",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Cloud Gaming") }
                )
            }
        }

        Text(
            "CloudPlay • Projeto em desenvolvimento",
            color = Muted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HeroBanner(
    account: String?,
    isTv: Boolean,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1D3824),
                        Color(0xFF101B13),
                        Color(0xFF111827)
                    )
                )
            )
            .border(
                1.dp,
                Color(0xFF34533A),
                RoundedCornerShape(22.dp)
            )
            .padding(if (isTv) 35.dp else 22.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Text(
            "SUA PRÓXIMA AVENTURA COMEÇA AQUI",
            color = Green,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
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

        if (account == null) {
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
        } else {
            Text(
                "Conectado como $account",
                color = Green,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GameSection(
    title: String,
    games: List<GameItem>,
    isTv: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                "DESTAQUES",
                color = Green,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(games) { game ->
                GameCard(game, isTv)
            }
        }

        Text(
            "Vitrine demonstrativa • jogos ainda não conectados",
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
    Column(
        modifier = Modifier.width(if (isTv) 185.dp else 145.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTv) 220.dp else 175.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.linearGradient(
                        listOf(game.start, game.end)
                    )
                )
                .border(
                    1.dp,
                    Color(0xFF39443B),
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
            modifier = Modifier.padding(15.dp),
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
private fun ProfileScreen(
    account: String?,
    photo: Bitmap?,
    notice: String,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(18.dp))
            .padding(23.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (account != null) {
            ProfileAvatar(
                photo = photo,
                size = 92
            )
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(42.dp)
            )
        }

        Text(
            "Sua conta",
            color = White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        if (account == null) {
            Text(
                "Entre com sua conta Microsoft para identificar seu perfil.",
                color = Muted,
                fontSize = 13.sp
            )

            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Color.Black
                )
            ) {
                Text("ENTRAR COM MICROSOFT")
            }
        } else {
            Text(
                account,
                color = Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Conta Microsoft conectada",
                color = Muted,
                fontSize = 12.sp
            )

            OutlinedButton(onClick = onLogout) {
                Text(
                    "SAIR DA CONTA",
                    color = White
                )
            }
        }

        HorizontalDivider(color = Color(0xFF303B32))

        Text(
            "INFORMAÇÕES DO XBOX",
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Text(
            "Gamertag, Gamerscore, conquistas e amigos serão exibidos quando a integração Xbox estiver disponível.",
            color = Muted,
            fontSize = 12.sp
        )

        HorizontalDivider(color = Color(0xFF303B32))

        Text(
            notice,
            color = Muted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun EmptyScreen(
    title: String,
    description: String,
    state: String
) {
    Column(
        modifier = Modifier
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

   
