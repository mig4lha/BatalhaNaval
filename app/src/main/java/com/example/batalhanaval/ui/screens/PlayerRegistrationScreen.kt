package com.example.batalhanaval.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.batalhanaval.data.firebase.FirebaseService

@Composable
fun PlayerRegistrationScreen(
    onPlayerRegistered: (String) -> Unit,
    onError: (String) -> Unit
) {
    var nick by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNickAvailable by remember { mutableStateOf(true) } // Controla se o nick está disponível
    var isPasswordValid by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Verifica se o nick está disponível enquanto o usuário digita
    LaunchedEffect(nick) {
        if (nick.isNotEmpty()) {
            FirebaseService.checkIfNickExists(nick) { available ->
                isNickAvailable = available
            }
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Enter your Nick!")

            TextField(
                value = nick,
                onValueChange = { newNick ->
                    nick = newNick
                },
                label = { Text("Nick") },
                modifier = Modifier.fillMaxWidth()
            )

            // Exibe mensagem de erro se o nick já existir
            if (!isNickAvailable && nick.isNotEmpty()) {
                Text(
                    text = "This nickname is already in use. Please choose another one!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(text = "Enter your Password")

            TextField(
                value = password,
                onValueChange = { newPassword ->
                    password = newPassword
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Text(text = "Confirm your Password")

            TextField(
                value = confirmPassword,
                onValueChange = { newConfirmPassword ->
                    confirmPassword = newConfirmPassword
                },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            // Validando se as senhas coincidem
            if (password != confirmPassword) {
                Text(
                    text = "Passwords do not match!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Botão para registrar o jogador
            Button(
                onClick = {
                    if (nick.isNotEmpty() && password.isNotEmpty() && password == confirmPassword && isNickAvailable) {
                        isLoading = true
                        FirebaseService.registerPlayer(nick, password) { playerId ->
                            isLoading = false
                            if (playerId != null) {
                                // Jogador registrado com sucesso, navega para o menu principal
                                onPlayerRegistered(playerId)
                            } else {
                                // Caso ocorra erro no registro
                                onError("Failed to register player!")
                            }
                        }
                    } else {
                        onError("Verify the information provided.")
                    }
                },
                enabled = isNickAvailable && password.isNotEmpty() && password == confirmPassword && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoading) "Registering..." else "Register")
            }
        }
    }
}
