package com.example.batalhanaval.ui.screens

import androidx.lifecycle.ViewModel
import com.example.batalhanaval.data.firebase.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginScreenViewModel : ViewModel() {
    private val _nick = MutableStateFlow("")
    val nick: StateFlow<String> = _nick

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun updateNick(newNick: String) {
        _nick.value = newNick
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun login(onSuccess: (String) -> Unit) {
        if (_nick.value.isNotEmpty() && _password.value.isNotEmpty()) {
            _isLoading.value = true
            FirebaseService.loginPlayer(_nick.value, _password.value) { playerId ->
                _isLoading.value = false
                if (playerId != null) {
                    FirebaseService.getPlayerName(playerId) { name ->
                        val playerNick = name ?: "Jogador1"
                        onSuccess(playerNick)
                    }
                } else {
                    _errorMessage.value = "Nick or password incorrect!"
                }
            }
        } else {
            _errorMessage.value = "Fill in all fields."
        }
    }
}
