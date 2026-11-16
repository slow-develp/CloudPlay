
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    var page by rememberSaveable { mutableStateOf("Início") }

    // Preferências do CloudPlay
    var imageQuality by rememberSaveable { mutableStateOf("1080p") }
    var dataSaver by rememberSaveable { mutableStateOf(false) }
    var autoPlay by rememberSaveable { mutableStateOf(false) }
    var touchControls by rememberSaveable { mutableStateOf(true) }
    var vibration by rememberSaveable { mutableStateOf(true) }
    var soundEffects by rememberSaveable { mutableStateOf(true) }

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
            imageQuality = imageQuality,
            dataSaver = dataSaver,
            autoPlay = autoPlay,
            touchControls = touchControls,
            vibration = vibration,
            soundEffects = soundEffects,
            onImageQualityChange = { imageQuality = it },
            onDataSaverChange = { dataSaver = it },
            onAutoPlayChange = { autoPlay = it },
            onTouchControlsChange = { touchControls = it },
            onVibrationChange = { vibration = it },
            onSoundEffectsChange = { soundEffects = it },
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
        "Perfil",
        "Configurações"
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
                    horizontal = if (isTv) 18.dp else 10.dp,
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
    imageQuality: String,
    dataSaver: Boolean,
    autoPlay: Boolean,
    touchControls: Boolean,
    vibration: Boolean,
    soundEffects: Boolean,
    onImageQualityChange: (String) -> Unit,
    onDataSaverChange: (Boolean) -> Unit,
    onAutoPlayChange: (Boolean) -> Unit,
    onTouchControlsChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSoundEffectsChange: (Boolean) -> Unit,
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
                onLogout = onLogout,
                isTv = isTv
            )

            "Configurações" -> SettingsScreen(
                isTv = isTv,
                imageQuality = imageQuality,
                dataSaver = dataSaver,
                autoPlay = autoPlay,
                touchControls = touchControls,
                vibration = vibration,
                soundEffects = soundEffects,
                onImageQualityChange = onImageQualityChange,
                onDataSaverChange = onDataSaverChange,
                onAutoPlayChange = onAutoPlayChange,
                onTouchControlsChange = onTouchControlsChange,
                onVibrationChange = onVibrationChange,
                onSoundEffectsChange = onSoundEffectsChange
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

            ActionCard(
                title = "CONFIGURAÇÕES",
                subtitle = "Personalizar sua experiência",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate("Configurações") }
            )
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

// PERFIL

@Composable
private fun ProfileScreen(
    account: String?,
    photo: Bitmap?,
    notice: String,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    isTv: Boolean
) {
    val horizontalPadding = if (isTv) 30.dp else 18.dp

    Column(

        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            if (isTv) 25.dp else 18.dp
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                "PERFIL DO JOGADOR",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Seu universo Xbox",
                color = White,
                fontSize = if (isTv) 30.sp else 25.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Sua identidade, conquistas e atividade.",
                color = Muted,
                fontSize = 12.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF263D2A),
                            Color(0xFF17251B),
                            Color(0xFF111827)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFF405C43),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CLOUDPLAY PLAYER",
                    color = Green,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )

                StatusPill(
                    text = if (account != null) "CONECTADO" else "DESCONECTADO",
                    active = account != null
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileAvatar(
                    photo = photo,
                    size = if (isTv) 104 else 82
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        account ?: "Jogador CloudPlay",
                        color = White,
                        fontSize = if (isTv) 20.sp else 16.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        if (account != null)
                            "Conta Microsoft vinculada"
                        else
                            "Conecte sua conta para começar",
                        color = Color(0xFFD0DDD2),
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF526653))

            Text(
                "FOTO DE PERFIL",
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "A imagem exibida é a foto carregada da conta Microsoft. A alteração do gamerpic Xbox depende de uma integração autorizada.",
                color = Color(0xFFD0DDD2),
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }

        Text(
            "ESTATÍSTICAS XBOX",
            color = White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ProfileStatCard(
                title = "GAMERSCORE",
                value = "—",
                description = "Aguardando Xbox",
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                title = "CONQUISTAS",
                value = "—",
                description = "Aguardando Xbox",
                modifier = Modifier.weight(1f)
            )

            ProfileStatCard(
                title = "GAMERTAG",
                value = "—",
                description = "Aguardando Xbox",
                modifier = Modifier.weight(1f)
            )
        }

        ProfileSectionCard(
            title = "CONQUISTAS",
            subtitle = "Suas conquistas desbloqueadas aparecerão aqui.",
            status = "INTEGRAÇÃO XBOX PENDENTE"
        )

        ProfileSectionCard(
            title = "ATIVIDADE RECENTE",
            subtitle = "Seus jogos recentes, tempo de jogo e histórico serão exibidos aqui quando os dados Xbox estiverem disponíveis.",
            status = "AGUARDANDO DADOS"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Panel, RoundedCornerShape(20.dp))
                .padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "CONTA MICROSOFT",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                account ?: "Nenhuma conta conectada",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Use sua conta Microsoft para conectar sua identidade ao CloudPlay.",
                color = Muted,
                fontSize = 11.sp
            )

            if (account == null) {
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        "CONECTAR MICROSOFT",
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        "DESCONECTAR CONTA",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            notice,
            color = Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    active: Boolean
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (active) Color(0xFF24452A)
                else Color(0xFF3A3020)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            color = if (active) Green else Color(0xFFFFD27A),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Panel, RoundedCornerShape(15.dp))
            .padding(horizontal = 9.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            title,
            color = Muted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            value,
            color = White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            description,
            color = Green,
            fontSize = 8.sp
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    status: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            title,
            color = White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            subtitle,
            color = Muted,
            fontSize = 11.sp,
            lineHeight = 17.sp
        )

        Text(
            status,
            color = Green,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// CONFIGURAÇÕES

@Composable
private fun SettingsScreen(
    isTv: Boolean,
    imageQuality: String,
    dataSaver: Boolean,
    autoPlay: Boolean,
    touchControls: Boolean,
    vibration: Boolean,
    soundEffects: Boolean,
    onImageQualityChange: (String) -> Unit,
    onDataSaverChange: (Boolean) -> Unit,
    onAutoPlayChange: (Boolean) -> Unit,
    onTouchControlsChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSoundEffectsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(19.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                "PERSONALIZE",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Configurações",
                color = White,
                fontSize = if (isTv) 32.sp else 27.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                "Ajuste sua experiência no CloudPlay.",
                color = Muted,
                fontSize = 12.sp
            )
        }

        SettingsGroup(
            title = "QUALIDADE DE IMAGEM",
            subtitle = "Escolha sua preferência de resolução."
        ) {
            Text(
                "Resolução preferida",
                color = White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "A resolução real dependerá do serviço de streaming, do jogo e do dispositivo.",
                color = Muted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )

            val qualities = listOf("720p", "1080p", "1440p", "4K")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                qualities.forEach { quality ->
                    Surface(
                        onClick = { onImageQualityChange(quality) },
                        modifier = Modifier.weight(1f),
                        color = if (imageQuality == quality)
                            Color(0xFF29452B)
                        else
                            Color(0xFF1B241D),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (imageQuality == quality) Green
                            else Color(0xFF303B32)
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                quality,
                                color = if (imageQuality == quality)
                                    Green else White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                "Selecionado: $imageQuality",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        SettingsGroup(
            title = "ECONOMIA DE DADOS",
            subtitle = "Controle suas preferências de consumo."
        ) {
            SettingsSwitch(
                title = "Economizar dados",
                description = "Preferir menor consumo de internet durante o uso.",
                checked = dataSaver,
                onCheckedChange = onDataSaverChange
            )
        }

        SettingsGroup(
            title = "REPRODUÇÃO",
            subtitle = "Preferências para abrir conteúdos."
        ) {
            SettingsSwitch(
                title = "Reprodução automática",
                description = "Permitir reprodução automática quando houver suporte.",
                checked = autoPlay,
                onCheckedChange = onAutoPlayChange
            )
        }

        SettingsGroup(
            title = "CONTROLES",
            subtitle = "Personalize os controles durante os jogos."
        ) {
            SettingsSwitch(
                title = "Controles virtuais",
                description = if (isTv)
                    "Na TV, priorize controle físico."
                else
                    "Exibir controles na tela do celular quando o jogo oferecer suporte.",
                checked = touchControls,
                onCheckedChange = onTouchControlsChange
            )

            HorizontalDivider(color = Color(0xFF303B32))

            SettingsSwitch(
                title = "Vibração",
                description = "Ativar resposta tátil quando disponível.",
                checked = vibration,
                onCheckedChange = onVibrationChange
            )

            HorizontalDivider(color = Color(0xFF303B32))

            SettingsSwitch(
                title = "Efeitos sonoros",
                description = "Ativar sons da interface quando disponíveis.",
                checked = soundEffects,
                onCheckedChange = onSoundEffectsChange
            )
        }

        SettingsGroup(
            title = "APARÊNCIA",
            subtitle = "Visual do aplicativo."
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B241D))
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Background)
                        .border(
                            1.dp,
                            Color(0xFF344438),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = null,
                        tint = Green
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Tema CloudPlay",
                        color = White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Grafite e preto com detalhes verdes",
                        color = Muted,
                        fontSize = 10.sp
                    )
                }

                StatusPill(
                    text = "ATIVO",
                    active = true
                )
            }
        }

        SettingsGroup(
            title = "SOBRE O CLOUDPLAY",
            subtitle = "Informações do aplicativo."
        ) {
            SettingsInfoRow("Aplicativo", "CloudPlay")

            HorizontalDivider(color = Color(0xFF303B32))

            SettingsInfoRow("Versão", "0.2.0")

            HorizontalDivider(color = Color(0xFF303B32))

            SettingsInfoRow(
                "Plataforma",
                if (isTv) "Android TV" else "Android"
            )
        }

        Text(
            "Algumas opções são preferências preparadas para a futura integração de streaming e podem ainda não alterar a reprodução.",
            color = Muted,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )

        Text(
            "CloudPlay • Configurações",
            color = Muted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(18.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                title,
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                subtitle,
                color = Muted,
                fontSize = 10.sp
            )
        }

        content()
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                description,
                color = Muted,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = Muted,
            fontSize = 11.sp
        )

        Text(
            value,
            color = White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
