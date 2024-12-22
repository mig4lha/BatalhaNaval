package com.example.batalhanaval

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.batalhanaval.ui.screens.GameScreen
import com.example.batalhanaval.ui.screens.LeaderboardScreen
import com.example.batalhanaval.ui.screens.MainMenuScreen
import com.example.batalhanaval.ui.screens.PlayerRegistrationScreen
import com.example.batalhanaval.ui.screens.LoginScreen
import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.ui.components.CellState
import com.example.batalhanaval.ui.theme.BatalhaNavalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatalhaNavalTheme {
                val navController = rememberNavController()

                // Suponha que o nome do jogador (nick) é obtido após o login ou registro
                var playerNick = "Jogador1" // Aqui, o playerNick pode vir de um ViewModel ou do Firebase

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = "login_screen") {
                        composable("login_screen") {
                            LoginScreen(navController = navController)
                        }

                        composable("player_registration") {
                            PlayerRegistrationScreen(
                                onPlayerRegistered = { playerId ->
                                    // Após o registro bem-sucedido, navega para o menu principal
                                    navController.navigate("main_menu") {
                                        launchSingleTop = true
                                        popUpTo("login_screen") { inclusive = true }
                                    }
                                },
                                onError = { errorMessage ->
                                    // Exibe o erro, como um Toast ou Snackbar
                                    println(errorMessage)
                                }
                            )
                        }

                        composable("main_menu") {
                            // Passando o nick do jogador para a MainMenuScreen
                            MainMenuScreen(
                                playerName = playerNick, // Passa o nome do jogador (nick)
                                onStartLocalGame = { navController.navigate("game_screen") },
                                onStartOnlineGame = { navController.navigate("game_screen") },
                                onShowLeaderboard = { navController.navigate("leaderboard") }
                            )
                        }

                        composable("game_screen") {
                            GameScreen(
                                board = List(10) { List(10) { CellState.EMPTY } },
                                onCellClick = { row, col -> /* Lógica de clique e atualização do jogo */ },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        composable("leaderboard") {
                            LeaderboardScreen(
                                scores = listOf(
                                    ScoreItem("Player1", 100),
                                    ScoreItem("Player2", 90)
                                ),
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
