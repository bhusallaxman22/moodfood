package com.example.moodfood.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.moodfood.navigation.NavRoute
import kotlinx.coroutines.delay

data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String,
    val backgroundColor: Color,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to MoodFood",
            description = "Discover personalized nutrition suggestions based on your mood and goals.",
            emoji = "🍎",
            backgroundColor = Color(0xFF6366F1),
            accentColor = Color(0xFF8B5CF6)
        ),
        OnboardingPage(
            title = "AI-Powered Suggestions",
            description = "Get intelligent meal recommendations tailored to boost your mood and energy.",
            emoji = "🤖",
            backgroundColor = Color(0xFFEC4899),
            accentColor = Color(0xFFF97316)
        ),
        OnboardingPage(
            title = "Track Your Journey",
            description = "Monitor your mood patterns and see how nutrition impacts your wellbeing.",
            emoji = "📊",
            backgroundColor = Color(0xFF10B981),
            accentColor = Color(0xFF06B6D4)
        ),
        OnboardingPage(
            title = "Start Your Journey",
            description = "Join thousands discovering the connection between food and mood.",
            emoji = "🚀",
            backgroundColor = Color(0xFFF59E0B),
            accentColor = Color(0xFFEF4444)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    
    // Auto-advance animation
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < pages.size - 1) {
            delay(4000) // Wait 4 seconds
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                isLastPage = page == pages.size - 1,
                onGetStarted = {
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(NavRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pages.forEachIndexed { index, _ ->
                PageIndicator(
                    isActive = index == pagerState.currentPage,
                    color = pages[pagerState.currentPage].accentColor
                )
            }
        }

        // Skip button
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = {
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(NavRoute.Onboarding.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Skip",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isLastPage: Boolean,
    onGetStarted: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backgroundOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        page.backgroundColor,
                        page.accentColor.copy(alpha = 0.8f),
                        page.backgroundColor.copy(alpha = 0.9f)
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        0.5f + backgroundOffset * 0.2f,
                        0.3f + backgroundOffset * 0.1f
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Animated emoji
            AnimatedEmoji(page.emoji)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Title with slide animation
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 800, delayMillis = 200)
                ) + fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 200))
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description with slide animation
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 800, delayMillis = 400)
                ) + fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 400))
            ) {
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Get Started button for last page
            if (isLastPage) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(
                        animationSpec = tween(durationMillis = 600, delayMillis = 600)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 600))
                ) {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = page.backgroundColor
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedEmoji(emoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "emoji")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiRotation"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .background(
                Color.White.copy(alpha = 0.2f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.5f
            ),
            modifier = Modifier.graphicsLayer {
                rotationZ = rotation
            }
        )
    }
}

@Composable
private fun PageIndicator(
    isActive: Boolean,
    color: Color
) {
    val width by animateDpAsState(
        targetValue = if (isActive) 24.dp else 8.dp,
        animationSpec = tween(durationMillis = 300),
        label = "indicatorWidth"
    )

    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
            )
    )
}
