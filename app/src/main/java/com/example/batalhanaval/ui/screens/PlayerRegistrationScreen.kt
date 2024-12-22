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

        // Exibe mensagem de erro se o nick já existir
        if (!isNickAvailable && nick.isNotEmpty()) {
            Text(
                text = "Este nick já está em uso. Escolha outro!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

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

        Text(text = "Confirme sua Senha")
        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { newConfirmPassword ->
                confirmPassword = newConfirmPassword
            },
            label = { Text("Confirmar Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Validando se as senhas coincidem
        if (password != confirmPassword) {
            Text(
                text = "As senhas não coincidem!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                            onError("Falha ao registrar o jogador!")
                        }
                    }
                } else {
                    onError("Verifique as informações fornecidas.")
                }
            },
            enabled = isNickAvailable && password.isNotEmpty() && password == confirmPassword && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLoading) "Registrando..." else "Registrar")
        }
    }
}
