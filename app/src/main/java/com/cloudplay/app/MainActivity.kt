
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
            onSignedIn = { username -> account = username },
            onSignedOut = {
                account = null
                profilePhoto = null
            },
            onMessage = { message -> notice = message },
            onPhotoLoaded = { bitmap -> profilePhoto = bitmap }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            // Respeita a barra de status, recortes e barra de navegação.
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Header(
            account = account,
            photo = profilePhoto,
            isTv = isTv,
            onProfile = { page = "Perfil" },
            onLogin = { microsoftAuth.signIn() }
        )

        NavigationMenu(
            selected = page,
            isTv = isTv,
            onSelect = { page = it }
        )

        MainContent(
            modifier = Modifier.weight(1f),
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
    isTv: Boolean,
    onProfile: () -> Unit,
    onLogin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D140F))
            .padding(
                horizontal = if (isTv) 30.dp else 16.dp,
                vertical = if (isTv) 18.dp else 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Cloud,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(if (isTv) 36.dp else 27.dp)
            )

            Spacer(Modifier.width(9.dp))

            Column {
                Text(
                    "CLOUDPLAY",
                    color = White,
                    fontSize = if (isTv) 22.sp else 17.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    "JOGUE SEM LIMITES",
                    color = Green,
                    fontSize = if (isTv) 10.sp else 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (account != null) {
            TextButton(onClick = onProfile) {
                ProfileAvatar(
                    photo = photo,
                    size = if (isTv) 40 else 32
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    "PERFIL",
                    color = White,
                    fontSize = if (isTv) 13.sp else 10.sp
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
                    horizontal = if (isTv) 19.dp else 12.dp,
                    vertical = if (isTv) 11.dp else 7.dp
                )
            ) {
                Text(
                    "ENTRAR",
                    fontWeight = FontWeight.Black,
                    fontSize = if (isTv) 13.sp else 10.sp
                )
            }
        }
    }
}

@Composable
private fun NavigationMenu(
    selected: String,
    isTv: Boolean,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        "Início",
        "Biblioteca",
        "Cloud Gaming",
        "Perfil"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF0D140F))
            .padding(horizontal = if (isTv) 22.dp else 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            TextButton(
                onClick = { onSelect(option) },
                contentPadding = PaddingValues(
                    horizontal = if (isTv) 20.dp else 11.dp,
                    vertical = if (isTv) 12.dp else 7.dp
                )
            ) {
                Text(
                    option.uppercase(),
                    color = if (selected == option) Green else Muted,
                    fontSize = if (isTv) 13.sp else 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    HorizontalDivider(color = Color(0xFF263229))
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = if (isTv) 35.dp else 15.dp,
                vertical = if (isTv) 28.dp else 15.dp
            ),
        verticalArrangement = Arrangement.spacedBy(
            if (isTv) 30.dp else 19.dp
        )
    ) {
        when (page) {
            "Início" -> HomeScreen(
                account = account,
                isTv = isTv,
                onLogin = onLogin,
                onNavigate = onNavigate
            )

            "Biblioteca" -> LibraryScreen()

            "Cloud Gaming" -> CloudGamingScreen()

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
        verticalArrangement = Arrangement.spacedBy(
            if (isTv) 30.dp else 20.dp
        )
    ) {
        HeroBanner(
            account = account,
            isTv = isTv,
            onLogin = onLogin
        )

        XboxCatalogScreen()

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "EXPLORE O CLOUDPLAY",
                color = White,
                fontSize = if (isTv) 21.sp else 15.sp,
                fontWeight = FontWeight.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    title = "BIBLIOTECA",
                    subtitle = "Explorar jogos",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("Biblioteca") }
                )

                ActionCard(
                    title = "CLOUD GAMING",
                    subtitle = "Área de streaming",
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
            .clip(RoundedCornerShape(if (isTv) 28.dp else 20.dp))
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
                RoundedCornerShape(if (isTv) 28.dp else 20.dp)
            )
            .padding(
                horizontal = if (isTv) 35.dp else 19.dp,
                vertical = if (isTv) 35.dp else 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(
            if (isTv) 17.dp else 11.dp
        )
    ) {
        Text(
            "SUA PRÓXIMA AVENTURA COMEÇA AQUI",
            color = Green,
            fontSize = if (isTv) 13.sp else 9.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            "Seus jogos.\nSeu universo.",
            color = White,
            fontWeight = FontWeight.Black,
            fontSize = if (isTv) 43.sp else 29.sp,
            lineHeight = if (isTv) 49.sp else 34.sp
        )

        Text(
            "Uma nova experiência de jogos na nuvem.",
            color = Color(0xFFD0DDD2),
            fontSize = if (isTv) 16.sp else 11.sp
        )

        if (account == null) {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(30.dp),
                contentPadding = PaddingValues(
                    horizontal = 17.dp,
                    vertical = 10.dp
                )
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    "ENTRAR COM MICROSOFT",
                    fontWeight = FontWeight.Black,
                    fontSize = if (isTv) 14.sp else 11.sp
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
private fun LibraryScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "SUA BIBLIOTECA",
            color = Green,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            "Explore os jogos",
            color = White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            "Jogos encontrados no catálogo online. Isso ainda não representa os jogos vinculados à sua conta Xbox.",
            color = Muted,
            fontSize = 12.sp
        )

        XboxCatalogScreen()
    }
}

@Composable
private fun CloudGamingScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(18.dp))
            .padding(23.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            Icons.Filled.CloudQueue,
            contentDescription = null,
            tint = Green,
            modifier = Modifier.size(42.dp)
        )

        Text(
            "CLOUD GAMING",
            color = Green,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            "Jogue na nuvem",
            color = White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            "Esta área será responsável por iniciar e transmitir os jogos dentro do CloudPlay.",
            color = Muted,
            fontSize = 13.sp
        )

        HorizontalDivider(color = Color(0xFF303B32))

        Text(
            "Transmissão ainda não conectada",
            color = Color(0xFFFFD27A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "O catálogo de jogos já tem uma tela própria, mas ainda precisamos de uma integração de streaming compatível e autorizada. O login Microsoft sozinho não inicia uma sessão de jogo.",
            color = Muted,
            fontSize = 12.sp
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
