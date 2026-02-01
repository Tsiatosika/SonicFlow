package com.example.sonicflow.presentation.screen.waveform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.WaveformData
import com.example.sonicflow.domain.usecase.waveform.GenerateWaveformUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaveformViewModel @Inject constructor(
    private val generateWaveformUseCase: GenerateWaveformUseCase
) : ViewModel() {

    private val _waveformData = MutableStateFlow<WaveformData?>(null)
    val waveformData: StateFlow<WaveformData?> = _waveformData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadWaveform(trackId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                android.util.Log.d("WaveformViewModel", "Loading waveform for track $trackId")
                val waveform = generateWaveformUseCase(trackId)
                _waveformData.value = waveform
                android.util.Log.d("WaveformViewModel", "Waveform loaded: ${waveform.amplitudes.size} points")
            } catch (e: Exception) {
                android.util.Log.e("WaveformViewModel", "Error loading waveform", e)
                _error.value = "Failed to load waveform: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}