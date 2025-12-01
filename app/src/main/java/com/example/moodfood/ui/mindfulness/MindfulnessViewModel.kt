package com.example.moodfood.ui.mindfulness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MindfulnessExercise(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int, // in minutes
    val icon: String,
    val type: ExerciseType
)

enum class ExerciseType {
    BREATHING,
    MEDITATION,
    BODY_SCAN,
    VISUALIZATION
}

data class BreathingPattern(
    val name: String,
    val inhale: Int,
    val hold: Int,
    val exhale: Int,
    val cycles: Int,
    val description: String
)

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentPhase: BreathPhase = BreathPhase.INHALE,
    val remainingSeconds: Int = 0,
    val totalDuration: Int = 0,
    val completedCycles: Int = 0,
    val totalCycles: Int = 0
)

enum class BreathPhase {
    INHALE,
    HOLD,
    EXHALE,
    REST
}

data class MindfulnessUiState(
    val exercises: List<MindfulnessExercise> = emptyList(),
    val selectedExercise: MindfulnessExercise? = null,
    val timerState: TimerState = TimerState(),
    val completedToday: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MindfulnessViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MindfulnessUiState())
    val uiState: StateFlow<MindfulnessUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentPattern: BreathingPattern? = null

    init {
        loadExercises()
        loadStats()
    }

    private fun loadExercises() {
        val exercises = listOf(
            MindfulnessExercise(
                id = "box_breathing",
                title = "Box Breathing",
                description = "Equal parts inhale, hold, exhale, and hold. Great for stress relief.",
                duration = 5,
                icon = "🟦",
                type = ExerciseType.BREATHING
            ),
            MindfulnessExercise(
                id = "478_breathing",
                title = "4-7-8 Breathing",
                description = "Relaxing breath for sleep and anxiety. Inhale 4, hold 7, exhale 8.",
                duration = 5,
                icon = "😴",
                type = ExerciseType.BREATHING
            ),
            MindfulnessExercise(
                id = "body_scan",
                title = "Body Scan Meditation",
                description = "Progressive relaxation through body awareness.",
                duration = 10,
                icon = "🧘",
                type = ExerciseType.BODY_SCAN
            ),
            MindfulnessExercise(
                id = "loving_kindness",
                title = "Loving-Kindness",
                description = "Cultivate compassion for yourself and others.",
                duration = 10,
                icon = "💝",
                type = ExerciseType.MEDITATION
            ),
            MindfulnessExercise(
                id = "visualization",
                title = "Peaceful Place",
                description = "Visualize a calm, safe space to reduce stress.",
                duration = 8,
                icon = "🌅",
                type = ExerciseType.VISUALIZATION
            ),
            MindfulnessExercise(
                id = "mindful_breathing",
                title = "Mindful Breathing",
                description = "Simple awareness of natural breath rhythm.",
                duration = 5,
                icon = "🌬️",
                type = ExerciseType.BREATHING
            )
        )
        
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    private fun loadStats() {
        // TODO: Load from database
        _uiState.value = _uiState.value.copy(
            completedToday = 2,
            totalMinutes = 45,
            currentStreak = 3
        )
    }

    fun selectExercise(exercise: MindfulnessExercise) {
        _uiState.value = _uiState.value.copy(selectedExercise = exercise)
    }

    fun clearSelection() {
        stopTimer()
        _uiState.value = _uiState.value.copy(selectedExercise = null)
    }

    fun startBreathingExercise(pattern: BreathingPattern) {
        currentPattern = pattern
        val totalSeconds = (pattern.inhale + pattern.hold + pattern.exhale) * pattern.cycles
        
        _uiState.value = _uiState.value.copy(
            timerState = TimerState(
                isRunning = true,
                isPaused = false,
                currentPhase = BreathPhase.INHALE,
                remainingSeconds = pattern.inhale,
                totalDuration = totalSeconds,
                completedCycles = 0,
                totalCycles = pattern.cycles
            )
        )
        
        startTimer(pattern)
    }

    private fun startTimer(pattern: BreathingPattern) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var currentCycle = 0
            
            while (currentCycle < pattern.cycles) {
                // Inhale phase
                _uiState.value = _uiState.value.copy(
                    timerState = _uiState.value.timerState.copy(
                        currentPhase = BreathPhase.INHALE,
                        remainingSeconds = pattern.inhale
                    )
                )
                countdown(pattern.inhale)
                
                // Hold phase
                if (pattern.hold > 0) {
                    _uiState.value = _uiState.value.copy(
                        timerState = _uiState.value.timerState.copy(
                            currentPhase = BreathPhase.HOLD,
                            remainingSeconds = pattern.hold
                        )
                    )
                    countdown(pattern.hold)
                }
                
                // Exhale phase
                _uiState.value = _uiState.value.copy(
                    timerState = _uiState.value.timerState.copy(
                        currentPhase = BreathPhase.EXHALE,
                        remainingSeconds = pattern.exhale
                    )
                )
                countdown(pattern.exhale)
                
                currentCycle++
                _uiState.value = _uiState.value.copy(
                    timerState = _uiState.value.timerState.copy(
                        completedCycles = currentCycle
                    )
                )
                
                // Small pause between cycles
                if (currentCycle < pattern.cycles) {
                    delay(1000)
                }
            }
            
            // Exercise complete
            completeExercise()
        }
    }

    private suspend fun countdown(seconds: Int) {
        repeat(seconds) {
            if (!_uiState.value.timerState.isRunning || _uiState.value.timerState.isPaused) {
                return
            }
            delay(1000)
            _uiState.value = _uiState.value.copy(
                timerState = _uiState.value.timerState.copy(
                    remainingSeconds = _uiState.value.timerState.remainingSeconds - 1
                )
            )
        }
    }

    fun pauseTimer() {
        _uiState.value = _uiState.value.copy(
            timerState = _uiState.value.timerState.copy(isPaused = true)
        )
    }

    fun resumeTimer() {
        _uiState.value = _uiState.value.copy(
            timerState = _uiState.value.timerState.copy(isPaused = false)
        )
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerState = TimerState()
        )
    }

    private fun completeExercise() {
        _uiState.value = _uiState.value.copy(
            timerState = TimerState(),
            completedToday = _uiState.value.completedToday + 1,
            totalMinutes = _uiState.value.totalMinutes + (_uiState.value.selectedExercise?.duration ?: 0)
        )
        // TODO: Save to database
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
