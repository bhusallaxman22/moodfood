package com.example.moodfood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    var mood by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    val symptoms = remember { mutableStateListOf<String>() }
    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("How are you feeling today?", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("Current mood") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide advanced" else "Advanced")
            }
        }
        if (showAdvanced) {
            OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Goal mood (optional)") }, modifier = Modifier.fillMaxWidth())
            SymptomChips(symptoms)
        }
        Button(onClick = { /* TODO call AI */ }, enabled = mood.isNotBlank(), modifier = Modifier.align(Alignment.End)) {
            Text("Get suggestion")
        }
        // Placeholder for suggestions output
        Text("Suggestions will appear here.")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomChips(selected: MutableList<String>) {
    val all = listOf("anxious", "stressed", "fatigued", "low", "irritable")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        all.forEach { tag ->
            val chosen = selected.contains(tag)
            FilterChip(
                selected = chosen,
                onClick = {
                    if (chosen) selected.remove(tag) else selected.add(tag)
                },
                label = { Text(tag) }
            )
        }
    }
}

@Composable
fun ProgressScreen() {
    CenterText("Progress")
}

@Composable
fun TrendsScreen() {
    CenterText("Trends")
}

@Composable
fun MindfulnessScreen() {
    CenterText("Mindfulness")
}

@Composable
fun ProfileScreen() {
    CenterText("Profile")
}

@Composable
private fun CenterText(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}
