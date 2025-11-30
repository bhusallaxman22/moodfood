package com.example.moodfood.ui.legal

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodfood.data.SettingsRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = SettingsRepository(context)
    val scope = rememberCoroutineScope()

    val dataSharing by repo.dataSharingEnabled.collectAsState(initial = false)
    val analytics by repo.analyticsEnabled.collectAsState(initial = false)
    val profilePublic by repo.profilePublic.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Privacy Settings", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Manage how your data is used", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

            // Data sharing toggle
            PreferenceItem(
                title = "Data Sharing",
                subtitle = "Allow anonymized sharing of your data to improve the service",
                checked = dataSharing,
                onCheckedChange = { new -> scope.launch { repo.setDataSharingEnabled(new) } }
            )

            // Analytics toggle
            PreferenceItem(
                title = "Usage Analytics",
                subtitle = "Share anonymous usage statistics",
                checked = analytics,
                onCheckedChange = { new -> scope.launch { repo.setAnalyticsEnabled(new) } }
            )

            // Profile visibility toggle
            PreferenceItem(
                title = "Profile Visibility",
                subtitle = "Make your profile public to other users",
                checked = profilePublic,
                onCheckedChange = { new -> scope.launch { repo.setProfilePublic(new) } }
            )

            Divider()

            Text(text = "Account Requests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            // Request data download via email
            ElevatedCard(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Request your data or request account deletion by emailing our support team.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@moodfood.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Data Download Request")
                                putExtra(Intent.EXTRA_TEXT, "Please provide a copy of my account data.\nAccount email: ")
                            }
                            if (emailIntent.resolveActivity(context.packageManager) != null) context.startActivity(emailIntent)
                        }) { Text("Request Data") }

                        TextButton(onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@moodfood.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Account Deletion Request")
                                putExtra(Intent.EXTRA_TEXT, "Please delete my account and all associated data.\nAccount email: ")
                            }
                            if (emailIntent.resolveActivity(context.packageManager) != null) context.startActivity(emailIntent)
                        }) { Text("Request Deletion") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PreferenceItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
