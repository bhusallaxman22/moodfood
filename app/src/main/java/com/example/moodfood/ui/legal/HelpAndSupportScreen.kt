package com.example.moodfood.ui.legal

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndSupportScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
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

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Section
            Text(
                text = "How can we help you?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Find answers to common questions or contact our support team for assistance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // FAQs Section
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            FAQItem(
                question = "How does MoodFood work?",
                answer = "MoodFood analyzes your current mood and dietary preferences to provide personalized nutrition suggestions. Simply input your mood, and our AI-powered system will recommend foods that can help improve your wellbeing."
            )

            FAQItem(
                question = "Is MoodFood a substitute for medical advice?",
                answer = "No. MoodFood is for informational purposes only and should not replace professional medical advice. Always consult with a healthcare provider before making significant dietary changes, especially if you have allergies or medical conditions."
            )

            FAQItem(
                question = "How is my personal data stored?",
                answer = "Your data is securely stored in encrypted form on our servers. We follow strict data protection standards and will never share your information with third parties without your consent. See our Privacy Policy for more details."
            )

            FAQItem(
                question = "Can I delete my account?",
                answer = "Yes, you can delete your account anytime. Please contact our support team at support@moodfood.com with your request, and we'll process it within 7-10 business days. All your data will be permanently removed."
            )

            FAQItem(
                question = "Why aren't notifications working?",
                answer = "Make sure notifications are enabled in your device settings and in the MoodFood app settings. Go to Profile > Settings > Notifications and toggle it on. Also check your phone's notification permissions for MoodFood."
            )

            FAQItem(
                question = "How do I change my password?",
                answer = "You can change your password in the Profile section. Go to Profile > Settings > Change Password and follow the prompts to set a new secure password."
            )

            Divider()

            // Contact Support Section
            Text(
                text = "Contact Support",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            // Email Support Button
            SupportContactCard(
                icon = "📧",
                title = "Email Support",
                description = "Get help via email",
                details = "support@moodfood.com",
                onClick = {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@moodfood.com")
                        putExtra(Intent.EXTRA_SUBJECT, "MoodFood Support Request")
                    }
                    if (emailIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(emailIntent)
                    }
                }
            )

            // Phone Support Button
            SupportContactCard(
                icon = "📞",
                title = "Call Us",
                description = "Speak with our team",
                details = "+1 (831) 222-2333",
                onClick = {
                    try {
                        val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                            data =  Uri.parse("tel:+18312222333")
                            putExtra("phone"+18312222333, "MoodFood Support")
                        }
                        context.startActivity(phoneIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }
            )


            // Live Chat Support
            SupportContactCard(
                icon = "💬",
                title = "Live Chat",
                description = "Chat with us in real-time",
                details = "Available 9 AM - 6 PM EST",
                onClick = {

                }
            )

            Divider()

            // App Info Section
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            InfoCard(
                label = "App Version",
                value = "1.0.0"
            )

            InfoCard(
                label = "Build Number",
                value = "101"
            )

            InfoCard(
                label = "Last Updated",
                value = "November 2024"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FAQItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (expanded) "−" else "+",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5f
                )
            }
        }
    }
}

@Composable
private fun SupportContactCard(
    icon: String,
    title: String,
    description: String,
    details: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 40.sp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
