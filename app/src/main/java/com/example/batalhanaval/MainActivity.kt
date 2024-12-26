package com.example.batalhanaval

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.batalhanaval.ui.screens.ChooseOpponentScreen
import com.example.batalhanaval.ui.screens.GameSetupScreen
import com.example.batalhanaval.ui.theme.BatalhaNavalTheme
import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.ui.components.CellState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatalhaNavalTheme {
                val navController = rememberNavController()

                var playerNick by remember { mutableStateOf("Jogador1") }
                val playerNickFromNav = navController.currentBackStackEntry?.arguments?.getString("playerNick")

                LaunchedEffect(playerNickFromNav) {
                    playerNick = playerNickFromNav ?: "Jogador1"
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = "login_screen") {
                        composable("login_screen") {
                            LoginScreen(navController = navController)
                        }
                        composable("main_menu/{playerNick}") { backStackEntry ->
                            val playerName = backStackEntry.arguments?.getString("playerNick") ?: "Jogador1"
                            MainMenuScreen(
                                playerName = playerName,
                                onStartOnlineGame = { navController.navigate("choose_opponent/$playerName") },
                                onShowLeaderboard = { navController.navigate("leaderboard") },
                                onShowChooseOpponent = { navController.navigate("choose_opponent/$playerName") }
                            )
                        }
                        composable("choose_opponent/{playerName}") { backStackEntry ->
                            val playerName = backStackEntry.arguments?.getString("playerName") ?: "Jogador1"
                            ChooseOpponentScreen(
                                playerName = playerName,
                                onOpponentSelected = { opponentName, gameId ->
                                    navController.navigate("game_setup/$gameId/$opponentName") {
                                        launchSingleTop = true
                                    }
                                },
                                onBackToMenu = { navController.popBackStack() }
                            )
                        }
                        composable("game_setup/{gameId}/{opponentName}") { backStackEntry ->
                            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                            val opponentName = backStackEntry.arguments?.getString("opponentName") ?: ""

                            GameSetupScreen(
                                gameId = gameId,
                                opponentName = opponentName,
                                onSetupComplete = {
                                    navController.navigate("game_screen/$gameId/$playerNick") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("game_screen/{gameId}/{currentPlayerId}") { backStackEntry ->
                            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                            val currentPlayerId = backStackEntry.arguments?.getString("currentPlayerId") ?: ""

                            GameScreen(
                                gameId = gameId,
                                currentPlayerId = currentPlayerId,
                                board = List(8) { List(8) { CellState.EMPTY } },
                                onCellClick = { row, col -> /* Lógica de clique e atualização do jogo */ },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable("leaderboard") {
                            LeaderboardScreen(
                                scores = listOf(ScoreItem("Player1", 100), ScoreItem("Player2", 90)),
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
