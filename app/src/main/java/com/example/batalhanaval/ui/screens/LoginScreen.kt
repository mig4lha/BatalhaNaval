package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.batalhanaval.data.firebase.FirebaseService

@Composable
fun LoginScreen(navController: NavController) {
    var nick by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Função para fazer login
    fun login() {
        if (nick.isNotEmpty() && password.isNotEmpty()) {
            isLoading = true
            FirebaseService.loginPlayer(nick, password) { playerId ->
                isLoading = false
                if (playerId != null) {
                    // Busca o nome do jogador após o login
                    FirebaseService.getPlayerName(playerId) { name ->
                        val playerNick = name ?: "Jogador1" // Nome obtido ou nome padrão
                        // Passa o playerNick para o MainMenuScreen
                        navController.navigate("main_menu/$playerNick") {
                            launchSingleTop = true
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                } else {
                    errorMessage = "Nick or password incorrect!"
                }
            }
        } else {
            errorMessage = "Fill in all fields."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = nick,
                onValueChange = { nick = it },
                label = { Text("Nick") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { login() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoading) "Loading.." else "Sign")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.navigate("register") {
                        launchSingleTop = true
                        popUpTo("login_screen") { inclusive = false }
                    }
                }
            ) {
                Text("Click here for register")
            }
        }
    }
}
