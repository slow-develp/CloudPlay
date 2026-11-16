package com.cloudplay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080B10)
private val Panel = Color(0xFF151B24)
private val Green = Color(0xFF107C10)
private val Muted = Color(0xFF9AA4B2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CloudPlayApp() }
    }
}

@Composable
fun CloudPlayApp() {
    var page by remember { mutableStateOf("Início") }
    var notice by remember { mutableStateOf("CloudPlay iniciado. Integrações serão configuradas por etapas.") }
    val tabs = listOf("Início", "Biblioteca", "Explorar", "Perfil")
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF5DD65D), background = Bg, surface = Panel)) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Color(0xFF10151D), contentColor = Color.White) {
                tabs.forEach { tab ->
                    NavigationBarItem(selected = page == tab, onClick = { page = tab },
                        icon = { Text(when(tab) { "Início" -> "⌂"; "Biblioteca" -> "▣"; "Explorar" -> "⌕"; else -> "●" }, fontSize = 20.sp) },
                        label = { Text(tab, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = Green, selectedTextColor = Color.White, unselectedTextColor = Muted))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("CLOUDPLAY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.5.sp)
                        Text("JOGUE. CONECTE. DESCUBRA.", color = Muted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    }
                    Surface(color = Panel, shape = RoundedCornerShape(50)) {
                        Text("CP", modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                when(page) {
                    "Início" -> HomeScreen(onLogin = { notice = "Login Microsoft: próximo passo é registrar o aplicativo e configurar MSAL. Nenhuma senha será solicitada pelo CloudPlay." }, onExplore = { page = "Explorar" }, onLibrary = { page = "Biblioteca" })
                    "Biblioteca" -> EmptyScreen("Minha biblioteca", "Sua biblioteca real aparecerá aqui quando conectarmos uma fonte de dados autorizada.", "Nenhum jogo conectado")
                    "Explorar" -> EmptyScreen("Explorar jogos", "O catálogo será preenchido por uma integração autorizada. Não exibimos jogos inventados.", "Catálogo ainda não conectado")
                    else -> EmptyScreen("Perfil", "O perfil Xbox será carregado após autenticação e autorização dos serviços correspondentes.", "Conta Microsoft não conectada")
                }
                Text(notice, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp))
                Text("VERSÃO 0.1.0 • PROTÓTIPO NATIVO", color = Color(0xFF586272), fontSize = 9.sp, modifier = Modifier.padding(18.dp))
            }
        }
    }
}

@Composable
private fun HomeScreen(onLogin: () -> Unit, onExplore: () -> Unit, onLibrary: () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.linearGradient(listOf(Color(0xFF1C3B2A), Color(0xFF101820), Color(0xFF18212C))), RoundedCornerShape(20.dp)).padding(20.dp), contentAlignment = Alignment.BottomStart) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SEU PRÓXIMO JOGO COMEÇA AQUI", color = Color(0xFFB6F3B6), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Seu espaço de\njogos na nuvem.", color = Color.White, fontSize = 29.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text("Uma plataforma independente em desenvolvimento.", color = Color(0xFFD0D7DE), fontSize = 12.sp)
            }
        }
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Green), shape = RoundedCornerShape(12.dp)) { Text("CONECTAR CONTA MICROSOFT", fontWeight = FontWeight.Bold) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionCard("Minha biblioteca", "Jogos conectados", Modifier.weight(1f), onLibrary)
            ActionCard("Explorar", "Catálogo autorizado", Modifier.weight(1f), onExplore)
        }
        Text("ESTADO DO PROJETO", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        StatusRow("Aplicativo Android nativo", "Pronto para evoluir", true)
        StatusRow("Login Microsoft (MSAL)", "Precisa de cadastro e configuração", false)
        StatusRow("Dados Xbox / biblioteca", "Acesso depende de autorização", false)
        StatusRow("Streaming com GPU", "Servidor ainda não disponível", false)
        StatusRow("Controles na sessão", "Serão ligados ao streaming", false)
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), color = Panel, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatusRow(title: String, detail: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(10.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if(ready) "●" else "○", color = if(ready) Color(0xFF5DD65D) else Color(0xFFFFC857), fontSize = 17.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EmptyScreen(title: String, description: String, state: String) {
    Column(Modifier.fillMaxWidth().padding(18.dp).background(Panel, RoundedCornerShape(18.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Muted, fontSize = 13.sp)
        Divider(color = Color(0xFF303946))
        Text(state, color = Color(0xFFFFD27A), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
