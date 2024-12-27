package com.example.batalhanaval

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            var playerNick by remember { mutableStateOf("Jogador1") }
            val playerNickFromNav = navController.currentBackStackEntry?.arguments?.getString("playerNick")

            LaunchedEffect(playerNickFromNav) {
                playerNick = playerNickFromNav ?: "Jogador1"
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "login_screen",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("login_screen") {
                        LoginScreen(navController = navController)
                    }

                    composable("register") {
                        PlayerRegistrationScreen(
                            onPlayerRegistered = { registeredNick ->
                                navController.navigate("main_menu/$registeredNick") {
                                    popUpTo("login_screen") { inclusive = true }
                                }
                            },
                            onError = { error ->
                                println("Erro ao registrar jogador: $error")
                            }
                        )
                    }

                    composable("main_menu/{playerNick}") { backStackEntry ->
                        val playerName = backStackEntry.arguments?.getString("playerNick") ?: "Jogador1"
                        MainMenuScreen(
                            playerName = playerName,
                            onStartOnlineGame = { navController.navigate("choose_opponent/$playerName") },
                            onShowLeaderboard = { navController.navigate("leaderboard") },
                            onShowChooseOpponent = { navController.navigate("choose_opponent/$playerName") },
                            onShowActiveGames = { navController.navigate("active_games/$playerName") }
                        )
                    }

                    composable("choose_opponent/{playerName}") { backStackEntry ->
                        val playerName = backStackEntry.arguments?.getString("playerName") ?: "Jogador1"
                        ChooseOpponentScreen(
                            playerName = playerName,
                            onOpponentSelected = { opponentName, gameId ->
                                navController.navigate("game_setup/$gameId/$playerName") {
                                    launchSingleTop = true
                                }
                            },
                            onBackToMenu = { navController.popBackStack() }
                        )
                    }

                    composable("game_setup/{gameId}/{currentPlayer}") { backStackEntry ->
                        val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                        val currentPlayer = backStackEntry.arguments?.getString("currentPlayer") ?: "Jogador1"

                        GameSetupScreen(
                            gameId = gameId,
                            currentPlayer = currentPlayer,
                            onWaitForOpponent = {
                                navController.navigate("wait_for_opponent/$currentPlayer") {
                                    launchSingleTop = true
                                }
                            },
                            onBackToMenu = {
                                navController.navigate("main_menu/$currentPlayer") {
                                    popUpTo("main_menu/$currentPlayer") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("wait_for_opponent/{playerName}") { backStackEntry ->
                        val playerName = backStackEntry.arguments?.getString("playerName") ?: "Jogador1"
                        WaitForOpponentScreen(
                            playerName = playerName,
                            onBackToMenu = { returnedPlayerName ->
                                navController.navigate("main_menu/$returnedPlayerName") {
                                    popUpTo("main_menu/$returnedPlayerName") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("game_screen/{gameId}/{currentPlayerId}") { backStackEntry ->
                        val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                        val currentPlayerId = backStackEntry.arguments?.getString("currentPlayerId") ?: ""

                        GameScreen(
                            gameId = gameId,
                            currentPlayer = currentPlayerId,
                            onGameEnd = {
                                navController.navigate("main_menu/$currentPlayerId") {
                                    popUpTo("main_menu/$currentPlayerId") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("active_games/{playerName}") { backStackEntry ->
                        val playerName = backStackEntry.arguments?.getString("playerName") ?: "Jogador1"
                        ActiveGamesScreen(
                            playerName = playerName,
                            onGameSelected = { gameId, status ->
                                when (status) {
                                    "placing" -> navController.navigate("game_setup/$gameId/$playerName")
                                    "playing" -> navController.navigate("game_screen/$gameId/$playerName")
                                }
                            },
                            onBackToMenu = { navController.popBackStack() }
                        )
                    }

                    composable("leaderboard") {
                        LeaderboardScreen(
                            scores = listOf(
                                ScoreItem("Player1", 100),
                                ScoreItem("Player2", 90)
                            )
                        )
                    }
                }
            }
        }
    }
}
