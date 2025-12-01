package com.example.moodfood.ui.mindfulness

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MindfulnessScreen(
    viewModel: MindfulnessViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.selectedExercise == null) {
            MindfulnessMainScreen(
                uiState = uiState,
                onExerciseClick = { viewModel.selectExercise(it) }
            )
        } else {
            ExerciseDetailScreen(
                exercise = uiState.selectedExercise!!,
                timerState = uiState.timerState,
                onBack = { viewModel.clearSelection() },
                onStart = { pattern -> viewModel.startBreathingExercise(pattern) },
                onPause = { viewModel.pauseTimer() },
                onResume = { viewModel.resumeTimer() },
                onStop = { viewModel.stopTimer() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MindfulnessMainScreen(
    uiState: MindfulnessUiState,
    onExerciseClick: (MindfulnessExercise) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "🧘 Mindfulness",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = "✅",
                            value = uiState.completedToday.toString(),
                            label = "Completed"
                        )
                        StatItem(
                            icon = "⏱️",
                            value = "${uiState.totalMinutes}m",
                            label = "Minutes"
                        )
                        StatItem(
                            icon = "🔥",
                            value = "${uiState.currentStreak}d",
                            label = "Streak"
                        )
                    }
                }
            }

            // Quick Start Section
            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Breathing Exercises Grid
            val breathingExercises = uiState.exercises.filter { it.type == ExerciseType.BREATHING }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(breathingExercises) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise) }
                    )
                }
            }

            // All Practices Section
            Text(
                text = "All Practices",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            val otherExercises = uiState.exercises.filter { it.type != ExerciseType.BREATHING }
            otherExercises.forEach { exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatItem(icon: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 32.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: MindfulnessExercise,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = exercise.icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exercise.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = "${exercise.duration} min",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: MindfulnessExercise,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = exercise.icon, fontSize = 40.sp)
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2
                )
            }
            
            Text(
                text = "${exercise.duration}m",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailScreen(
    exercise: MindfulnessExercise,
    timerState: TimerState,
    onBack: () -> Unit,
    onStart: (BreathingPattern) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(exercise.title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        if (timerState.isRunning) {
            BreathingExerciseActive(
                timerState = timerState,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop
            )
        } else {
            ExerciseInstructions(
                exercise = exercise,
                onStart = onStart
            )
        }
    }
}

@Composable
private fun ExerciseInstructions(
    exercise: MindfulnessExercise,
    onStart: (BreathingPattern) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = exercise.icon, fontSize = 80.sp)
        
        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (exercise.type == ExerciseType.BREATHING) {
            // Show breathing pattern options
            val patterns = getBreathingPatterns(exercise.id)
            
            Text(
                text = "Choose a Pattern",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 16.dp)
            )

            patterns.forEach { pattern ->
                BreathingPatternCard(
                    pattern = pattern,
                    onSelect = { onStart(pattern) }
                )
            }
        } else {
            // Meditation/Body Scan/Visualization instructions
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    when (exercise.type) {
                        ExerciseType.BODY_SCAN -> {
                            InstructionStep("1", "Find a comfortable position, lying down or seated")
                            InstructionStep("2", "Close your eyes and take a few deep breaths")
                            InstructionStep("3", "Bring attention to your toes, noticing any sensations")
                            InstructionStep("4", "Slowly move up through each body part")
                            InstructionStep("5", "Release tension as you scan each area")
                        }
                        ExerciseType.MEDITATION -> {
                            InstructionStep("1", "Sit comfortably with your spine straight")
                            InstructionStep("2", "Think of someone you care about")
                            InstructionStep("3", "Silently repeat: 'May you be happy, may you be healthy'")
                            InstructionStep("4", "Extend this wish to yourself, then to all beings")
                        }
                        ExerciseType.VISUALIZATION -> {
                            InstructionStep("1", "Close your eyes and breathe deeply")
                            InstructionStep("2", "Imagine a peaceful, safe place")
                            InstructionStep("3", "Notice the colors, sounds, and sensations")
                            InstructionStep("4", "Stay in this place for several minutes")
                        }
                        else -> {}
                    }
                }
            }

            Button(
                onClick = { /* Start meditation timer */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start ${exercise.duration} min session",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BreathingPatternCard(
    pattern: BreathingPattern,
    onSelect: () -> Unit
) {
    ElevatedCard(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = pattern.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = pattern.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Inhale ${pattern.inhale}s • Hold ${pattern.hold}s • Exhale ${pattern.exhale}s",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "${pattern.cycles} cycles",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun BreathingExerciseActive(
    timerState: TimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (timerState.currentPhase) {
                    BreathPhase.INHALE -> timerState.remainingSeconds * 1000
                    BreathPhase.EXHALE -> timerState.remainingSeconds * 1000
                    else -> 1000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress indicator
        Text(
            text = "Cycle ${timerState.completedCycles + 1} of ${timerState.totalCycles}",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Breathing circle
        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(if (timerState.isPaused) 1f else scale)
                .background(
                    color = when (timerState.currentPhase) {
                        BreathPhase.INHALE -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                        BreathPhase.HOLD -> Color(0xFFFFC107).copy(alpha = 0.3f)
                        BreathPhase.EXHALE -> Color(0xFF2196F3).copy(alpha = 0.3f)
                        BreathPhase.REST -> Color(0xFF9E9E9E).copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (timerState.currentPhase) {
                        BreathPhase.INHALE -> "Breathe In"
                        BreathPhase.HOLD -> "Hold"
                        BreathPhase.EXHALE -> "Breathe Out"
                        BreathPhase.REST -> "Rest"
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "${timerState.remainingSeconds}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stop button
            FilledIconButton(
                onClick = onStop,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Pause/Resume button
            FilledIconButton(
                onClick = { if (timerState.isPaused) onResume() else onPause() },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (timerState.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (timerState.isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun getBreathingPatterns(exerciseId: String): List<BreathingPattern> {
    return when (exerciseId) {
        "box_breathing" -> listOf(
            BreathingPattern(
                name = "Classic Box",
                inhale = 4,
                hold = 4,
                exhale = 4,
                cycles = 5,
                description = "Equal 4-second intervals for balance"
            ),
            BreathingPattern(
                name = "Extended Box",
                inhale = 5,
                hold = 5,
                exhale = 5,
                cycles = 4,
                description = "Longer intervals for deeper calm"
            )
        )
        "478_breathing" -> listOf(
            BreathingPattern(
                name = "4-7-8 Relaxation",
                inhale = 4,
                hold = 7,
                exhale = 8,
                cycles = 4,
                description = "Dr. Weil's relaxation technique"
            )
        )
        "mindful_breathing" -> listOf(
            BreathingPattern(
                name = "Natural Rhythm",
                inhale = 4,
                hold = 0,
                exhale = 6,
                cycles = 8,
                description = "Follow your natural breath"
            ),
            BreathingPattern(
                name = "Deep Breathing",
                inhale = 5,
                hold = 2,
                exhale = 7,
                cycles = 6,
                description = "Deeper breaths for relaxation"
            )
        )
        else -> listOf(
            BreathingPattern(
                name = "Standard",
                inhale = 4,
                hold = 4,
                exhale = 4,
                cycles = 5,
                description = "Balanced breathing pattern"
            )
        )
    }
}
