
package com.cloudplay.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private val CatalogGreen = Color(0xFF8BFF4D)
private val CatalogWhite = Color(0xFFF5F8F5)
private val CatalogMuted = Color(0xFF9AA69C)
private val CatalogPanel = Color(0xFF121A14)

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

    LaunchedEffect(Unit) {

        loading = true
        error = ""

        try {
            cloudGames = withContext(Dispatchers.IO) {
                XboxCatalog.loadCloudGames()
            }

            recentGames = withContext(Dispatchers.IO) {
                XboxCatalog.loadRecentlyAdded()
            }

            if (cloudGames.isEmpty() && recentGames.isEmpty()) {
                error = "Nenhum jogo foi encontrado nas listas consultadas."
            }

        } catch (e: Exception) {
            error = e.message ?: "Erro desconhecido ao consultar o catálogo."
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "XBOX CLOUD GAMING",
                color = CatalogGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Jogos para jogar",
                color = CatalogWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Catálogo consultado online",
                color = CatalogMuted,
                fontSize = 12.sp
            )
        }

        if (loading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = CatalogGreen
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Carregando catálogo...",
                    color = CatalogWhite
                )
            }
        }

        if (error.isNotBlank() && cloudGames.isEmpty() && recentGames.isEmpty()) {
            Surface(
                color = CatalogPanel,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Não foi possível consultar o catálogo.\n$error",
                    color = CatalogMuted,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp
                )
            }
        }

        if (recentGames.isNotEmpty()) {
            CatalogSection(
                title = "ADICIONADOS RECENTEMENTE",
                games = recentGames
            )
        }

        if (cloudGames.isNotEmpty()) {
            CatalogSection(
                title = "CATÁLOGO NA NUVEM",
                games = cloudGames
            )
        }

        if (!loading && cloudGames.isEmpty() && recentGames.isEmpty() && error.isBlank()) {
            Text(
                text = "Nenhum jogo foi retornado pela consulta.",
                color = CatalogMuted,
                fontSize = 12.sp
            )
        }

        Text(
            text = "A disponibilidade para jogar depende da região, da conta e do plano Xbox.",
            color = CatalogMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CatalogSection(
    title: String,
    games: List<XboxGame>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = title,
            color = CatalogWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
private fun CatalogGameCard(game: XboxGame) {

    Column(
        modifier = Modifier.width(145.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(CatalogPanel),
            contentAlignment = Alignment.Center
        ) {

            if (game.imageUrl.isNotBlank()) {
                RemoteGameImage(url = game.imageUrl)
            } else {
                Text(
                    text = game.title,
                    color = CatalogWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Text(
            text = game.title,
            color = CatalogWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
    }
}

@Composable
private fun RemoteGameImage(url: String) {

    var bitmap by remember(url) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    LaunchedEffect(url) {

        bitmap = withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                connection.getInputStream().use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Capa de ${url}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        CircularProgressIndicator(
            color = CatalogGreen,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}
