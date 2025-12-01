package com.example.moodfood.ui.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(viewModel: TrendsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Show error message if present
    uiState.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
    
    PullToRefreshBox(
        isRefreshing = uiState.isLoading && uiState.totalEntries > 0,
        onRefresh = { viewModel.refreshData() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error Display
            uiState.error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
            
            if (uiState.isLoading && uiState.totalEntries == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.totalEntries == 0) {
                EmptyTrendsState()
            } else {
                // Header Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 56.sp
                        )
                        
                        Text(
                            text = "Your Mood Trends",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Text(
                            text = "Analyzing ${uiState.totalEntries} mood entries",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                // Mood Change Indicator
                MoodChangeCard(
                    averageLast7Days = uiState.averageMoodLast7Days,
                    averageLast30Days = uiState.averageMoodLast30Days,
                    change = uiState.moodChange
                )
                
                // Weekly Trends
                if (uiState.weeklyTrends.isNotEmpty()) {
                    WeeklyTrendsCard(weeklyTrends = uiState.weeklyTrends)
                }
                
                // Mood Distribution
                if (uiState.moodDistribution.isNotEmpty()) {
                    MoodDistributionCard(
                        distribution = uiState.moodDistribution,
                        topMood = uiState.topMood
                    )
                }
                
                // Recent Mood Timeline
                if (uiState.moodTrendData.isNotEmpty()) {
                    MoodTimelineCard(moodData = uiState.moodTrendData.takeLast(7))
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun EmptyTrendsState() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📈",
                fontSize = 64.sp
            )
            Text(
                text = "No Trends Yet",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Start tracking your moods to see trends and patterns!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MoodChangeCard(
    averageLast7Days: Float,
    averageLast30Days: Float,
    change: Float
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📈 Mood Trend",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f%%", averageLast7Days * 100),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Last 7 Days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f%%", averageLast30Days * 100),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Last 30 Days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emoji = when {
                    change > 0.05f -> "📈"
                    change < -0.05f -> "📉"
                    else -> "➡️"
                }
                val text = when {
                    change > 0.05f -> "Improving"
                    change < -0.05f -> "Declining"
                    else -> "Stable"
                }
                val color = when {
                    change > 0.05f -> MaterialTheme.colorScheme.tertiary
                    change < -0.05f -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Text(text = emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun WeeklyTrendsCard(weeklyTrends: List<WeeklyTrendData>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📅 Weekly Trends",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            weeklyTrends.forEach { week ->
                WeeklyTrendItem(week)
            }
        }
    }
}

@Composable
private fun WeeklyTrendItem(week: WeeklyTrendData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = week.weekLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(80.dp)
        )
        
        LinearProgressIndicator(
            progress = { week.averageMood },
            modifier = Modifier
                .weight(1f)
                .height(12.dp),
        )
        
        Text(
            text = "${(week.averageMood * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.width(45.dp)
        )
        
        Text(
            text = "(${week.entryCount})",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.width(35.dp)
        )
    }
}

@Composable
private fun MoodDistributionCard(
    distribution: List<MoodDistribution>,
    topMood: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "😊 Mood Distribution",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Text(
                text = "Most frequent: $topMood",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            HorizontalDivider()
            
            distribution.forEach { mood ->
                MoodDistributionItem(mood)
            }
        }
    }
}

@Composable
private fun MoodDistributionItem(mood: MoodDistribution) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = mood.emoji, fontSize = 24.sp)
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mood.mood,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            LinearProgressIndicator(
                progress = { mood.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
        
        Text(
            text = "${mood.count}x",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        
        Text(
            text = "${mood.percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun MoodTimelineCard(moodData: List<MoodTrendData>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🕐 Recent Timeline (Last 7 Days)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            moodData.forEach { data ->
                MoodTimelineItem(data)
            }
        }
    }
}

@Composable
private fun MoodTimelineItem(data: MoodTrendData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        data.moodScore >= 0.7f -> MaterialTheme.colorScheme.tertiaryContainer
                        data.moodScore >= 0.4f -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = try {
                    java.time.LocalDate.parse(
                        data.date,
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
                    ).dayOfMonth.toString()
                } catch (e: Exception) {
                    "?"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.mood.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = data.date,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        
        Text(
            text = "${(data.moodScore * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = when {
                    data.moodScore >= 0.7f -> MaterialTheme.colorScheme.tertiary
                    data.moodScore >= 0.4f -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        )
    }
}
