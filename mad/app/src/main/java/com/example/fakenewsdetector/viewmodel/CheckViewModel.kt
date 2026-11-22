package com.example.fakenewsdetector.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakenewsdetector.data.AppDatabase
import com.example.fakenewsdetector.data.CheckRepository
import com.example.fakenewsdetector.ml.Classification
import com.example.fakenewsdetector.ml.Classifier
import com.example.fakenewsdetector.ml.MockClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CheckUiState {
    data object Idle : CheckUiState
    data object Loading : CheckUiState
    data class Done(val text: String, val result: Classification) : CheckUiState
    data class Error(val message: String) : CheckUiState
}

class CheckViewModel(app: Application) : AndroidViewModel(app) {

    // Swap this line for TfliteClassifier(app) once the real model is ready.
    private val classifier: Classifier = MockClassifier()

    private val repository = CheckRepository(AppDatabase.get(app).checkDao())

    private val _state = MutableStateFlow<CheckUiState>(CheckUiState.Idle)
    val state: StateFlow<CheckUiState> = _state.asStateFlow()

    fun analyze(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        _state.value = CheckUiState.Loading
        viewModelScope.launch {
            try {
                val result = classifier.classify(clean)
                repository.save(clean, result)
                _state.value = CheckUiState.Done(clean, result)
            } catch (e: Exception) {
                _state.value = CheckUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun reset() {
        _state.value = CheckUiState.Idle
    }
}
