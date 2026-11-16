
package com.cloudplay.app

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private val BackgroundColor = Color(0xFF090B10)
private val SurfaceColor = Color(0xFF151922)
private val SurfaceLight = Color(0xFF202633)
private val AccentColor = Color(0xFF35C878)
private val MainText = Color(0xFFF5F6F8)
private val SecondaryText = Color(0xFF9AA3B2)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CloudPlayApp()
        }
    }
}

@Composable
fun CloudPlayApp() {
    val activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity
    val configuration = LocalConfiguration.current
    val isTv = configuration.screenWidthDp >= 700

    var account by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf("") }
    var profilePhoto by remember { mutableStateOf<Bitmap?>(null) }
    var currentPage by remember { mutableStateOf("Início") }

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
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing),
    color = BackgroundColor
) {
            if (isTv) {
                TvInterface(
                    page = currentPage,
                    onPageChange = { currentPage = it },
                    account = account,
                    photo = profilePhoto,
                    notice = notice,
                    onLogin = { auth.signIn() },
                    onLogout = { auth.signOut() }
                )
            } else {
                MobileInterface(
                    page = currentPage,
                    onPageChange = { currentPage = it },
                    account = account,
                    photo = profilePhoto,
                    notice = notice,
                    onLogin = { auth.signIn() },
                    onLogout = { auth.signOut() }
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
                .width(230.dp)
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
                onLogin = onLogin
            )

            Spacer(modifier = Modifier.height(22.dp))

            when (page) {
                "Início" -> HomeScreen(isTv = true)

                "Minha Biblioteca" -> CatalogPage(
                    title = "Minha Biblioteca"
                )

                "Game Pass" -> CatalogPage(
                    title = "Game Pass"
                )

                "Pesquisar" -> SearchPage()

                "Perfil" -> ProfileScreen(
                    account = account,
                    photo = photo,
                    notice = notice,
                    onLogin = onLogin,
                    onLogout = onLogout
                )

                "Configurações" -> SettingsPage()

                else -> HomeScreen(isTv = true)
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
            onLogin = onLogin
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (page) {
                "Início" -> HomeScreen(isTv = false)

                "Minha Biblioteca" -> CatalogPage(
                    title = "Minha Biblioteca"
                )

                "Game Pass" -> CatalogPage(
                    title = "Game Pass"
                )

                "Pesquisar" -> SearchPage()

                "Perfil" -> ProfileScreen(
                    account = account,
                    photo = photo,
                    notice = notice,
                    onLogin = onLogin,
                    onLogout = onLogout
                )

                "Configurações" -> SettingsPage()

                else -> HomeScreen(isTv = false)
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
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = androidx.compose.ui.res.painterResource(
                id = R.drawable.cloudplay_icon
            ),
            contentDescription = "CloudPlay",
            modifier = Modifier.size(42.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "CLOUDPLAY",
                color = MainText,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "CLOUD GAMING",
                color = SecondaryText,
                fontSize = 9.sp,
                letterSpacing = 2.sp
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CloudLogo()

        Spacer(modifier = Modifier.weight(1f))

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
private fun HomeScreen(isTv: Boolean) {
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
                description = "Acesse seus jogos e conteúdos.",
                icon = Icons.Default.SportsEsports
            )
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
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = if (query.isBlank()) {
                "Digite o nome de um jogo para pesquisar."
            } else {
                "Pesquisa: $query"
            },
            color = SecondaryText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        XboxCatalogScreen()
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
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Meu perfil",
            color = MainText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        ProfileAvatar(photo, 100.dp)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = account ?: "Conta não conectada",
            color = MainText,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (account != null) {
                "Conta Microsoft conectada"
            } else {
                "Conecte sua conta Microsoft"
            },
            color = SecondaryText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (account == null) {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conectar Microsoft")
            }
        } else {
            OutlinedButton(
                onClick = onLogout
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Desconectar")
            }
        }

        if (notice.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = notice,
                color = SecondaryText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        ActionCard(
            title = "Estatísticas Xbox",
            description = "Gamertag, conquistas e pontuação Xbox precisam da integração Xbox Live.",
            icon = Icons.Default.EmojiEvents
        )
    }
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
    var quality by remember { mutableStateOf("Alta") }
    var region by remember { mutableStateOf("Automática") }
    var language by remember { mutableStateOf("Português") }
    var dataSaver by remember { mutableStateOf(false) }
    var vibration by remember { mutableStateOf(true) }
    var sound by remember { mutableStateOf(true) }
    var touchControls by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Text(
                text = "Configurações",
                color = MainText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsSection("Imagem e desempenho") {
                ChoiceSetting(
                    title = "Qualidade da imagem",
                    value = quality,
                    options = listOf("Baixa", "Média", "Alta", "Máxima"),
                    onChange = { quality = it }
                )

                ChoiceSetting(
                    title = "Região",
                    value = region,
                    options = listOf(
                        "Automática",
                        "Brasil",
                        "Estados Unidos",
                        "Europa"
                    ),
                    onChange = { region = it }
                )

                SettingSwitch(
                    title = "Economia de dados",
                    checked = dataSaver,
                    onChange = { dataSaver = it }
                )
            }
        }

        item {
            SettingsSection("Controles") {
                SettingSwitch(
                    title = "Controles virtuais",
                    checked = touchControls,
                    onChange = { touchControls = it }
                )

                SettingSwitch(
                    title = "Vibração",
                    checked = vibration,
                    onChange = { vibration = it }
                )

                SettingSwitch(
                    title = "Sons",
                    checked = sound,
                    onChange = { sound = it }
                )
            }
        }

        item {
            SettingsSection("Idioma") {
                ChoiceSetting(
                    title = "Idioma do aplicativo",
                    value = language,
                    options = listOf(
                        "Português",
                        "English",
                        "Español"
                    ),
                    onChange = { language = it }
                )
            }
        }

        item {
            Text(
                text = "As opções desta tela são preferências locais. A qualidade e a região só controlarão a transmissão quando o serviço de streaming estiver integrado.",
                color = SecondaryText,
                fontSize = 12.sp
            )
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
            color = MainText,
            fontSize = 16.sp,
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MainText,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentColor
            )
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
