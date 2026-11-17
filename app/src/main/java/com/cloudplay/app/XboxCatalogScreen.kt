
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.net.HttpURLConnection
import java.net.URL

private val CatalogBackground = Color(0xFF101114)
private val CatalogPanel = Color(0xFF1C1E22)
private val CatalogWhite = Color(0xFFF5F5F5)
private val CatalogMuted = Color(0xFF999DA5)
private val CatalogAccent = Color(0xFF8BFF4D)

/*
 * Cache compartilhado.
 * Os dados permanecem disponíveis quando o usuário
 * sai de uma página e entra em outra.
 */
private object XboxCatalogStore {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

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

        if (loading) return
        if (hasLoaded && !force) return

        scope.launch {

            loading = true
            error = ""

            try {

                val results = withTimeout(45000L) {

                    coroutineScope {

                        val cloudRequest = async(Dispatchers.IO) {
                            runCatching {
                                XboxCatalog.loadCloudGames()
                            }
                        }

                        val recentRequest = async(Dispatchers.IO) {
                            runCatching {
                                XboxCatalog.loadRecentlyAdded()
                            }
                        }

                        Pair(
                            cloudRequest.await(),
                            recentRequest.await()
                        )
                    }
                }

                cloudGames = results.first.getOrDefault(emptyList())
                recentGames = results.second.getOrDefault(emptyList())

                val errors = mutableListOf<String>()

                results.first.exceptionOrNull()?.let {
                    errors.add("Catálogo: ${it.localizedMessage}")
                }

                results.second.exceptionOrNull()?.let {
                    errors.add("Novidades: ${it.localizedMessage}")
                }

                error = errors.joinToString("\n")

                if (cloudGames.isEmpty() && recentGames.isEmpty()) {
                    error = errors.firstOrNull()
                        ?: "A Microsoft não retornou jogos."
                }

                hasLoaded = true

            } catch (e: TimeoutCancellationException) {

                error = "O servidor demorou demais para responder."

            } catch (e: Exception) {

                error = e.localizedMessage
                    ?: "Não foi possível carregar o catálogo."

            } finally {

                loading = false
            }
        }
    }
}

/*
 * Cache de imagens em memória.
 * Evita baixar novamente as capas ao navegar.
 */
private object GameImageCache {

    private val cache = object : LruCache<String, Bitmap>(
        24 * 1024
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun get(url: String): Bitmap? {
        return cache.get(url)
    }

    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }
}

@Composable
fun XboxCatalogScreen() {

    var selectedGame by remember {
        mutableStateOf<XboxGame?>(null)
    }

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
            text = "Xbox Cloud Gaming",
            color = CatalogWhite,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Explore os jogos disponíveis no catálogo",
            color = CatalogMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (loading && cloudGames.isEmpty() && recentGames.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = CatalogWhite,
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(13.dp))

                    Text(
                        text = "Carregando catálogo...",
                        color = CatalogMuted,
                        fontSize = 13.sp
                    )
                }
            }

        } else if (
            cloudGames.isEmpty() &&
            recentGames.isEmpty()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = CatalogMuted,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Catálogo indisponível",
                    color = CatalogWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = error.ifBlank {
                        "Não foi possível consultar os jogos."
                    },
                    color = CatalogMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        XboxCatalogStore.load(force = true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CatalogWhite,
                        contentColor = Color.Black
                    )
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

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = CatalogMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (recentGames.isNotEmpty()) {

                CatalogSection(
                    title = "Adicionados recentemente",
                    games = recentGames,
                    onGameClick = { selectedGame = it }
                )

                Spacer(modifier = Modifier.height(25.dp))
            }

            if (cloudGames.isNotEmpty()) {

                CatalogSection(
                    title = "Jogos para jogar",
                    games = cloudGames,
                    onGameClick = { selectedGame = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "A disponibilidade pode variar conforme região e assinatura.",
                color = CatalogMuted,
                fontSize = 10.sp
            )
        }
    }

    selectedGame?.let { game ->

        GameDetailsDialog(
            game = game,
            onBack = {
                selectedGame = null
            }
        )
    }
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

            Text(
                text = title,
                color = CatalogWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = games.size.toString(),
                color = CatalogMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            items(
                items = games,
                key = { it.productId }
            ) { game ->

                CatalogGameCard(
                    game = game,
                    onClick = {
                        onGameClick(game)
                    }
                )
            }
        }
    }
}

@Composable
private fun CatalogGameCard(
    game: XboxGame,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .width(145.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CatalogPanel)
            .clickable {
                onClick()
            }
    ) {

        RemoteGameImage(
            url = game.imageUrl,
            title = game.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = game.title,
            color = CatalogWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = 9.dp,
                end = 9.dp,
                bottom = 11.dp
            )
        )
    }
}

/*
 * Carregador de imagens com cache,
 * timeout e tratamento de falhas.
 */
@Composable
private fun RemoteGameImage(
    url: String,
    title: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {

    var bitmap by remember(url) {
        mutableStateOf(GameImageCache.get(url))
    }

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

                val loaded = withContext(Dispatchers.IO) {
                    downloadGameImage(url)
                }

                bitmap = loaded

                if (loaded != null) {
                    GameImageCache.put(url, loaded)
                }

                loadingImage = false
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF292B30)),
        contentAlignment = Alignment.Center
    ) {

        when {

            bitmap != null -> {

                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }

            loadingImage -> {

                CircularProgressIndicator(
                    modifier = Modifier.size(23.dp),
                    color = CatalogWhite,
                    strokeWidth = 2.dp
                )
            }

            else -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "XBOX",
                        color = CatalogWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Imagem indisponível",
                        color = CatalogMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

private fun downloadGameImage(
    originalUrl: String
): Bitmap? {

    val imageUrl = originalUrl
        .trim()
        .replace("&amp;", "&")
        .let {
            when {
                it.startsWith("//") -> "https:$it"
                it.startsWith("http://") ->
                    it.replaceFirst("http://", "https://")
                else -> it
            }
        }

    return try {

        val connection =
            URL(imageUrl).openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "GET"
            connection.connectTimeout = 12000
            connection.readTimeout = 15000
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

            connection.setRequestProperty(
                "Referer",
                "https://www.xbox.com/"
            )

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {
                return null
            }

            val contentType = connection.contentType ?: ""

            if (contentType.contains("text/html", ignoreCase = true)) {
                return null
            }

            connection.inputStream.use {
                BitmapFactory.decodeStream(it)
            }

        } finally {
            connection.disconnect()
        }

    } catch (e: Exception) {
        null
    }
}

/*
 * Tela de detalhes do jogo.
 */
@Composable
private fun GameDetailsDialog(
    game: XboxGame,
    onBack: () -> Unit
) {

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CatalogBackground
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {

                    RemoteGameImage(
                        url = game.imageUrl,
                        title = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
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
                            .background(
                                Color.Black.copy(alpha = 0.65f),
                                RoundedCornerShape(50)
                            )
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = game.title,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.Top
                ) {

                    RemoteGameImage(
                        url = game.imageUrl,
                        title = game.title,
                        modifier = Modifier
                            .width(105.dp)
                            .height(145.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(15.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = game.title,
                        
                            color = CatalogWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Catálogo Xbox",
                            color = CatalogMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Informações do jogo",
                            color = CatalogAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sobre este jogo",
                    color = CatalogWhite,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = game.description.ifBlank {
                        "Não há descrição disponível para este jogo."
                    },
                    color = CatalogMuted,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(25.dp))

                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Streaming ainda não integrado")
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

         
