package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.batalhanaval.data.firebase.FirebaseService

@Composable
fun LoginScreen(navController: NavController) {
    var nick by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isNickValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Digite o seu Nick")
        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = nick,
            onValueChange = { newNick ->
                nick = newNick
            },
            label = { Text("Nick") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Digite sua Senha")
        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = password,
            onValueChange = { newPassword ->
                password = newPassword
            },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (nick.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    FirebaseService.loginPlayer(nick, password) { playerId ->
                        isLoading = false
                        if (playerId != null) {
                            navController.navigate("main_menu")  // Redireciona para o menu principal
                        } else {
                            // Exibe mensagem de erro
                            isNickValid = false
                            isPasswordValid = false
                        }
                    }
                }
            },
            enabled = !isLoading && nick.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLoading) "Entrando..." else "Entrar")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Link para registro
        TextButton(onClick = { navController.navigate("player_registration") }) {
            Text("Click here to register!")
        }

        // Mensagem de erro
        if (!isNickValid || !isPasswordValid) {
            Text(
                text = "Nick ou senha incorretos. Tente novamente!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
