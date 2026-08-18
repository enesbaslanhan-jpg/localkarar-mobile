package com.localkarar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localkarar.app.auth.AuthViewModel
import com.localkarar.app.auth.UserDto

@Composable
fun AuthenticatedVerificationScreen(user: UserDto, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LocalKarar", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Giriş yapıldı: ${user.email}", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Kullanıcı Adı: ${user.name}", style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { viewModel.logout() }) {
            Text("Çıkış Yap")
        }
    }
}
