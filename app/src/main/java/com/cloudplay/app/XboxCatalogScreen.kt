
package com.cloudplay.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

@Composable
fun XboxCatalogScreen() {

    var cloudGames by remember {
        mutableStateOf<List<XboxGame>>(emptyList())
    }

    var recentGames by remember {
        mutableStateOf<List<XboxGame>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf("")
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(reloadKey) {

        loading = true
        error = ""

        try {

            val results = withTimeout(45000L) {

                coroutineScope {

                    val cloudRequest = async(Dispatchers.IO) {
                        runCatching {
                            XboxCatalog.loadCloudGames()
                        }.getOrDefault(emptyList())
                    }

                    val recentRequest = async(Dispatchers.IO) {
                        runCatching {
                            XboxCatalog.loadRecentlyAdded()
                        }.getOrDefault(emptyList())
                    }

                    Pair(
                        cloudRequest.await(),
                        recentRequest.await()
                    )
                }
            }

            cloudGames = results.first
            recentGames = results.second

            if (
                cloudGames.isEmpty() &&
                recentGames.isEmpty()
            ) {
                error = "Não foi possível carregar o catálogo. Verifique sua conexão e tente novamente."
            }

        } catch (e: TimeoutCancellationException) {

            error = "O servidor demorou para responder. Tente novamente."

        } catch (e: Exception) {

            error = "Erro ao consultar o catálogo: ${
                e.localizedMessage ?: "Erro desconhecido"
            }"

        } finally {

            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CatalogBackground)
            .padding(
                horizontal = 18.dp,
                vertical = 16.dp
            )
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

        Spacer(modifier = Modifier.height(22.dp))

        when {

            loading -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            color = CatalogWhite,
                            strokeWidth = 3.dp
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Carregando catálogo...",
                            color = CatalogMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            cloudGames.isEmpty() && recentGames.isEmpty() -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = CatalogMuted,
                            modifier = Modifier.size(42.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        Text(
                            text = "Catálogo indisponível",
                            color = CatalogWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = error.ifBlank {
                                "Não foi possível consultar os jogos."
                            },
                            color = CatalogMuted,
                            fontSize = 13.sp
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        Button(
                            onClick = {
                                reloadKey++
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CatalogWhite,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            else -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    if (error.isNotBlank()) {

                        Text(
                            text = error,
                            color = CatalogMuted,
                            fontSize = 12.sp
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }

                    if (recentGames.isNotEmpty()) {

                        CatalogSection(
                            title = "Adicionados recentemente",
                            games = recentGames
                        )

                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )
                    }

                    if (cloudGames.isNotEmpty()) {

                        CatalogSection(
                            title = "Jogos para jogar",
                            games = cloudGames
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogSection(
    title: String,
    games: List<XboxGame>
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

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = games.size.toString(),
                color = CatalogMuted,
                fontSize = 12.sp
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            items(
                items = games,
                key = { it.productId }
            ) { game ->

                CatalogGameCard(game)
            }
        }
    }
}

@Composable
private fun CatalogGameCard(
    game: XboxGame
) {

    Column(
        modifier = Modifier
            .width(145.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CatalogPanel)
            .clickable { }
    ) {

        RemoteGameImage(
            url = game.imageUrl,
            title = game.title
        )

        Spacer(
            modifier = Modifier.height(9.dp)
        )

        Text(
            text = game.title,
            color = CatalogWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = 9.dp,
                end = 9.dp,
                bottom = 11.dp
            )
        )
    }
}

@Composable
private fun RemoteGameImage(
    url: String,
    title: String
) {

    var bitmap by remember(url) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    var loadingImage by remember(url) {
        mutableStateOf(true)
    }

    LaunchedEffect(url) {

        if (url.isBlank()) {
            loadingImage = false
        } else {

            val loaded = withContext(Dispatchers.IO) {

                try {

                    val connection =
                        URL(url).openConnection() as HttpURLConnection

                    try {

                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        connection.requestMethod = "GET"
                        connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0"
                        )

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

            bitmap = loaded
            loadingImage = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(Color(0xFF292B30)),
        contentAlignment = Alignment.Center
    ) {

        when {

            bitmap != null -> {

                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            loadingImage -> {

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CatalogWhite,
                    strokeWidth = 2.dp
                )
            }

            else -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "XBOX",
                        color = CatalogWhite,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Imagem indisponível",
                        color = CatalogMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
