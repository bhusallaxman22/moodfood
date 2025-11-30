package com.example.moodfood.ui.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    var acceptedTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Terms & Conditions",
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Last Updated
            Text(
                text = "Last Updated: November 30, 2024",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // 1. Introduction
            Section(
                title = "1. Introduction",
                content = """
                    Welcome to MoodFood ("Application," "We," "Our," or "Us"). These Terms and Conditions ("Agreement") govern your use of the MoodFood mobile application and associated services. By downloading, installing, or using MoodFood, you agree to be bound by these Terms and Conditions. If you do not agree with any part of this Agreement, please do not use the Application.
                    
                    MoodFood is designed to provide personalized nutrition suggestions based on your mood and health goals, powered by artificial intelligence.
                """
            )

            // 2. Use License
            Section(
                title = "2. Use License",
                content = """
                    We grant you a limited, non-exclusive, non-transferable license to:
                    • Download and install the Application on your personal device
                    • Use the Application for personal, non-commercial purposes
                    • Access the content and services provided through the Application
                    
                    You may not:
                    • Modify, copy, or create derivative works
                    • Reverse engineer or decompile the Application
                    • Use the Application for commercial purposes without permission
                    • Share your login credentials with other users
                    • Attempt to gain unauthorized access to the Application
                """
            )

            // 3. User Accounts
            Section(
                title = "3. User Accounts",
                content = """
                    To use certain features of MoodFood, you must create an account. You are responsible for:
                    • Providing accurate and complete information during registration
                    • Maintaining the confidentiality of your password and login credentials
                    • All activities that occur under your account
                    • Notifying us immediately of any unauthorized use of your account
                    
                    We reserve the right to suspend or terminate accounts that violate these Terms or engage in prohibited conduct.
                """
            )

            // 4. Health Disclaimer
            Section(
                title = "4. Health Disclaimer",
                content = """
                    IMPORTANT: MoodFood is not a substitute for professional medical advice, diagnosis, or treatment. The suggestions provided by our Application are for informational purposes only and should not be relied upon as medical advice.
                    
                    • Always consult with a qualified healthcare provider before making significant dietary changes
                    • If you have allergies, medical conditions, or take medications, seek professional medical guidance
                    • Do not use MoodFood as a replacement for professional mental health services
                    • In case of medical emergencies, contact emergency services immediately
                    
                    The creators of MoodFood disclaim all liability for any harm arising from the use or misuse of this Application.
                """
            )

            // 5. User Content
            Section(
                title = "5. User Content",
                content = """
                    When you input information such as mood, food preferences, or health data into MoodFood, you grant us the right to:
                    • Store and process this information to provide personalized suggestions
                    • Use anonymized data for improving our algorithms and services
                    • Analyze patterns to enhance the Application's functionality
                    
                    You retain ownership of your data and can request deletion at any time. For data deletion requests, contact our support team.
                """
            )

            // 6. Privacy and Data Protection
            Section(
                title = "6. Privacy and Data Protection",
                content = """
                    Your privacy is important to us. Please refer to our Privacy Policy for detailed information about:
                    • What personal data we collect
                    • How we use and protect your information
                    • Your rights regarding your data
                    • Data retention and deletion policies
                    
                    By using MoodFood, you consent to our data practices as outlined in the Privacy Policy.
                """
            )

            // 7. Intellectual Property
            Section(
                title = "7. Intellectual Property Rights",
                content = """
                    All content in MoodFood, including but not limited to:
                    • Software code and algorithms
                    • Design elements and graphics
                    • Text, images, and multimedia content
                    • Trademarks and logos
                    
                    are the property of MoodFood or our licensors and are protected by copyright and intellectual property laws. You may not reproduce, distribute, or transmit any content without our explicit written permission.
                """
            )

            // 8. Limitation of Liability
            Section(
                title = "8. Limitation of Liability",
                content = """
                    TO THE MAXIMUM EXTENT PERMITTED BY LAW, MOODFOOD SHALL NOT BE LIABLE FOR:
                    • Any indirect, incidental, special, or consequential damages
                    • Loss of profit, data, or business opportunity
                    • Any damages arising from service interruptions or errors
                    • Health issues or adverse effects from following suggestions
                    
                    Our total liability is limited to the amount paid by you for the Application in the past 12 months, or $100, whichever is less.
                """
            )

            // 9. Disclaimers
            Section(
                title = "9. Disclaimers",
                content = """
                    THE APPLICATION IS PROVIDED "AS IS" AND "AS AVAILABLE" WITHOUT WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED.
                    
                    We disclaim all warranties including:
                    • Merchantability and fitness for a particular purpose
                    • Title and non-infringement
                    • Accuracy, reliability, and completeness of content
                    
                    We do not guarantee that the Application will be:
                    • Error-free or uninterrupted
                    • Free from viruses or harmful components
                    • Suitable for your specific needs
                """
            )

            // 10. Prohibited Conduct
            Section(
                title = "10. Prohibited Conduct",
                content = """
                    You agree not to:
                    • Harass, threaten, or abuse other users
                    • Share false or misleading health information
                    • Attempt to hack or bypass security measures
                    • Use the Application for illegal activities
                    • Spam or send unsolicited communications
                    • Violate any applicable laws or regulations
                    
                    We reserve the right to terminate access for violations of these provisions.
                """
            )

            // 11. Third-Party Services
            Section(
                title = "11. Third-Party Services",
                content = """
                    MoodFood may integrate with third-party services (APIs, authentication providers, analytics). We are not responsible for:
                    • The availability or functionality of third-party services
                    • Any damages arising from third-party services
                    • Privacy practices of third-party providers
                    
                    Your use of third-party services is governed by their respective terms and privacy policies.
                """
            )

            // 12. Changes to Terms
            Section(
                title = "12. Changes to Terms and Conditions",
                content = """
                    We may update these Terms and Conditions at any time. Changes will be effective immediately upon posting to the Application. Your continued use of MoodFood after changes constitutes your acceptance of the new Terms and Conditions.
                    
                    We encourage you to review these Terms periodically to stay informed of updates.
                """
            )

            // 13. Termination
            Section(
                title = "13. Termination",
                content = """
                    We may suspend or terminate your access to MoodFood at any time, with or without cause, and without prior notice. Termination may occur if you:
                    • Violate these Terms and Conditions
                    • Engage in prohibited conduct
                    • Fail to comply with applicable laws
                    
                    Upon termination, your right to use the Application ceases immediately.
                """
            )

            // 14. Governing Law
            Section(
                title = "14. Governing Law and Jurisdiction",
                content = """
                    These Terms and Conditions are governed by and construed in accordance with the laws of [Your Jurisdiction], without regard to its conflict of law provisions.
                    
                    You agree to submit to the exclusive jurisdiction of the courts located in [Your Jurisdiction] for any disputes arising from or relating to this Agreement.
                """
            )

            // 15. Contact Information
            Section(
                title = "15. Contact Information",
                content = """
                    If you have questions or concerns about these Terms and Conditions, please contact us at:
                    
                    Email: support@moodfood.com
                    Address: 3991 Main Isle Rd, Arlington, TX
                    Phone: +1 (831) 222-2333
                    
                    We will respond to inquiries within 7-10 business days.
                """
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Acceptance Section
        Divider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it }
                )
                Text(
                    text = "I have read and agree to the Terms and Conditions",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                enabled = acceptedTerms,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Accept & Continue",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = content.trimIndent(),
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 20.sp * 1.5f
                ),
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
