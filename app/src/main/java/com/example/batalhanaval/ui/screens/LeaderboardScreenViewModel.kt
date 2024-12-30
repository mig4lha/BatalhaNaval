package com.example.batalhanaval.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batalhanaval.data.model.ScoreItem
import com.example.batalhanaval.data.firebase.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardScreenViewModel : ViewModel() {
    private val _scores = MutableStateFlow<List<ScoreItem>>(emptyList())
    val scores: StateFlow<List<ScoreItem>> = _scores

    init {
        fetchTopScores()
    }

    private fun fetchTopScores() {
        viewModelScope.launch {
            FirebaseService.getTopScores { result ->
                _scores.value = result
            }
        }
    }
}
