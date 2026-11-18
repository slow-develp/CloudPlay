package com.cloudplay.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Branded sign-in panel for CloudPlay.
 * Place it in the login/profile area and pass onMicrosoftLogin = { xboxAuth.signIn() }.
 */
@Composable
fun XboxLoginBranding(
    onMicrosoftLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF151922), RoundedCornerShape(20.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CLOUDPLAY",
            color = Color.White,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Entre para conectar sua conta",
            color = Color(0xFFB8C0CC),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onMicrosoftLogin,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF202124)
            )
        ) {
            Text("Entrar com Microsoft", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "Xbox",
            color = Color(0xFF35C878),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Conecte sua identidade Xbox Network",
            color = Color(0xFFB8C0CC),
            fontSize = 12.sp
        )
    }
}
