package com.cloudplay.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer

private val CatalogBackground = Color(0xFF101114)
private val CatalogPanel = Color(0xFF1C1E22)
private val CatalogWhite = Color(0xFFF5F5F5)
private val CatalogMuted = Color(0xFF999DA5)
private val CatalogAccent = Color(0xFF8BFF4D)

/*
 * Cache compartilhado entre Início, Biblioteca e Pesquisa.
 * As listas não são baixadas novamente ao trocar de tela.
 */
private object XboxCatalogStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var cloudGames by mutableStateOf<List<XboxGame>>(emptyList())
        private set
    var recentGames by mutableStateOf<List<XboxGame>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set

    private var hasLoaded = false

    fun load(force: Boolean = false) {
        if (loading || (hasLoaded && !force)) return

        scope.launch {
            loading = true
            error = ""

            try {
                val cloudTask = async(Dispatchers.IO) {
                    runCatching { XboxCatalog.loadCloudGames() }
                }
                val recentTask = async(Dispatchers.IO) {
                    runCatching { XboxCatalog.loadRecentlyAdded() }
                }

                val cloudResult = cloudTask.await()
                val recentResult = recentTask.await()

                cloudGames = cloudResult.getOrDefault(emptyList())
                recentGames = recentResult.getOrDefault(emptyList())

                val messages = mutableListOf<String>()
                cloudResult.exceptionOrNull()?.let {
                    messages.add("Catálogo: ${it.localizedMessage ?: "erro"}")
                }
                recentResult.exceptionOrNull()?.let {
                    messages.add("Novidades: ${it.localizedMessage ?: "erro"}")
                }

                error = messages.joinToString("\n")
                if (cloudGames.isEmpty() && recentGames.isEmpty() && error.isBlank()) {
                    error = "A Microsoft não retornou jogos."
                }
                hasLoaded = cloudGames.isNotEmpty() || recentGames.isNotEmpty()
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Não foi possível carregar o catálogo."
            } finally {
                loading = false
            }
        }
    }
}

/* Cache de imagens em memória para evitar downloads repetidos. */
private object GameImageCache {
    private val cache = object : LruCache<String, Bitmap>(24 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun get(url: String): Bitmap? = cache.get(url)
    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }
}

@Composable
fun XboxCatalogScreen() {
    var selectedGame by remember { mutableStateOf<XboxGame?>(null) }

    LaunchedEffect(Unit) {
        XboxCatalogStore.load()
    }

    val cloudGames = XboxCatalogStore.cloudGames
    val recentGames = XboxCatalogStore.recentGames
    val loading = XboxCatalogStore.loading
    val error = XboxCatalogStore.error

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CatalogBackground)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            "Xbox Cloud Gaming",
            color = CatalogWhite,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(5.dp))
        Text(
            "Explore os jogos disponíveis no catálogo",
            color = CatalogMuted,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(20.dp))

        if (loading && cloudGames.isEmpty() && recentGames.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CatalogWhite, strokeWidth = 3.dp)
                    Spacer(Modifier.height(13.dp))
                    Text("Carregando catálogo...", color = CatalogMuted, fontSize = 13.sp)
                }
            }
        } else if (cloudGames.isEmpty() && recentGames.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Refresh, null, tint = CatalogMuted, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text("Catálogo indisponível", color = CatalogWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(error.ifBlank { "Não foi possível consultar os jogos." }, color = CatalogMuted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { XboxCatalogStore.load(force = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CatalogWhite, contentColor = Color.Black)
                ) {
                    Text("Tentar novamente")
                }
            }
        } else {
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = CatalogWhite,
                    trackColor = CatalogPanel
                )
                Spacer(Modifier.height(10.dp))
            }

            if (error.isNotBlank()) {
                Text(error, color = CatalogMuted, fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
            }

            if (recentGames.isNotEmpty()) {
                CatalogSection("Adicionados recentemente", recentGames) { selectedGame = it }
                Spacer(Modifier.height(25.dp))
            }
            if (cloudGames.isNotEmpty()) {
                CatalogSection("Jogos para jogar", cloudGames) { selectedGame = it }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "A disponibilidade pode variar conforme região e assinatura.",
                color = CatalogMuted,
                fontSize = 10.sp
            )
        }
    }

    selectedGame?.let { game ->
        GameDetailsDialog(game) { selectedGame = null }
    }
}

/*
 * Tela de pesquisa. A filtragem acontece localmente na lista já carregada,
 * sem consultar a Microsoft a cada letra digitada.
 */
@Composable
fun XboxCatalogSearchScreen(query: String) {
    var selectedGame by remember { mutableStateOf<XboxGame?>(null) }

    LaunchedEffect(Unit) {
        XboxCatalogStore.load()
    }

    val cloudGames = XboxCatalogStore.cloudGames
    val recentGames = XboxCatalogStore.recentGames
    val loading = XboxCatalogStore.loading
    val allGames = remember(cloudGames, recentGames) {
        (cloudGames + recentGames).distinctBy { it.productId }
    }
    val normalizedQuery = remember(query) { normalizeSearchText(query) }
    val results = remember(allGames, normalizedQuery) {
        if (normalizedQuery.isBlank()) emptyList()
        else allGames.filter { game ->
            normalizeSearchText(game.title).contains(normalizedQuery) ||
                normalizeSearchText(game.description).contains(normalizedQuery)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        when {
            normalizedQuery.isBlank() -> {
                Text(
                    "Digite o nome de um jogo para pesquisar.",
                    color = CatalogMuted,
                    fontSize = 13.sp
                )
            }
            loading && allGames.isEmpty() -> {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CatalogWhite)
                }
            }
            results.isEmpty() -> {
                Text("Nenhum jogo encontrado.", color = CatalogMuted, fontSize = 14.sp)
            }
            else -> {
                CatalogSection("Resultados encontrados (${results.size})", results) {
                    selectedGame = it
                }
            }
        }
    }

    selectedGame?.let { game ->
        GameDetailsDialog(game) { selectedGame = null }
    }
}

private fun normalizeSearchText(value: String): String {
    return Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
}

@Composable
private fun CatalogSection(
    title: String,
    games: List<XboxGame>,
    onGameClick: (XboxGame) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = CatalogWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(games.size.toString(), color = CatalogMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            items(games, key = { it.productId }) { game ->
                CatalogGameCard(game) { onGameClick(game) }
            }
        }
    }
}

@Composable
private fun CatalogGameCard(game: XboxGame, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(145.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CatalogPanel)
            .clickable(onClick = onClick)
    ) {
        RemoteGameImage(
            url = game.imageUrl,
            title = game.title,
            modifier = Modifier.fillMaxWidth().height(190.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = game.title,
            color = CatalogWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 9.dp, end = 9.dp, bottom = 11.dp)
        )
    }
}

@Composable
private fun RemoteGameImage(
    url: String,
    title: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember(url) { mutableStateOf(GameImageCache.get(url)) }
    var loadingImage by remember(url) {
        mutableStateOf(url.isNotBlank() && bitmap == null)
    }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            bitmap = null
            loadingImage = false
        } else {
            val cached = GameImageCache.get(url)
            if (cached != null) {
                bitmap = cached
                loadingImage = false
            } else {
                loadingImage = true
                val loaded = withContext(Dispatchers.IO) { downloadGameImage(url) }
                bitmap = loaded
                if (loaded != null) GameImageCache.put(url, loaded)
                loadingImage = false
            }
        }
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF292B30)),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
            loadingImage -> CircularProgressIndicator(
                modifier = Modifier.size(23.dp),
                color = CatalogWhite,
                strokeWidth = 2.dp
            )
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("XBOX", color = CatalogWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Imagem indisponível", color = CatalogMuted, fontSize = 9.sp)
            }
        }
    }
}

private fun downloadGameImage(originalUrl: String): Bitmap? {
    val imageUrl = originalUrl.trim().replace("&amp;", "&").let {
        when {
            it.startsWith("//") -> "https:$it"
            it.startsWith("http://") -> it.replaceFirst("http://", "https://")
            else -> it
        }
    }

    return try {
        val connection = URL(imageUrl).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 12000
            connection.instanceFollowRedirects = true
            connection.useCaches = true
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36"
            )
            connection.setRequestProperty(
                "Accept",
                "image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
            )
            connection.setRequestProperty("Referer", "https://www.xbox.com/")

            val code = connection.responseCode
            if (code !in 200..299) return null
            val type = connection.contentType ?: ""
            if (type.contains("text/html", ignoreCase = true)) return null

            connection.inputStream.use { BitmapFactory.decodeStream(it) }
        } finally {
            connection.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun GameDetailsDialog(game: XboxGame, onBack: () -> Unit) {
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = CatalogBackground) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(250.dp)
                ) {
                    RemoteGameImage(
                        url = game.bannerUrl.ifBlank { game.imageUrl },
                        title = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    CatalogBackground
                                )
                            )
                        )
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                    Text(
                        game.title,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RemoteGameImage(
                        url = game.imageUrl,
                        title = game.title,
                        modifier = Modifier.width(105.dp).height(145.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(15.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(game.title, color = CatalogWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Catálogo Xbox", color = CatalogMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        if (game.genre.isNotBlank()) {
                            Text("Gênero: ${game.genre}", color = CatalogMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                        }
                        if (game.developer.isNotBlank()) {
                            Text("Desenvolvedora: ${game.developer}", color = CatalogMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                        }
                        if (game.publisher.isNotBlank()) {
                            Text("Publicadora: ${game.publisher}", color = CatalogMuted, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "Sobre este jogo",
                    color = CatalogWhite,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    game.description.ifBlank { "Não há descrição disponível para este jogo." },
                    color = CatalogMuted,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(25.dp))
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Streaming ainda não integrado")
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
