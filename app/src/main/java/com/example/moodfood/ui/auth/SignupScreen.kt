package com.example.moodfood.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.ui.components.AuthButton
import com.example.moodfood.ui.components.AuthLinkButton
import com.example.moodfood.ui.components.AuthTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var securityQuestion1 by remember { mutableStateOf("What city were you born in?") }
    var securityAnswer1 by remember { mutableStateOf("") }
    var securityQuestion2 by remember { mutableStateOf("What was your first pet's name?") }
    var securityAnswer2 by remember { mutableStateOf("") }
    var showSecurityQuestions by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    val availableQuestions = listOf(
        "What city were you born in?",
        "What was your first pet's name?",
        "What is your mother's maiden name?",
        "What was the name of your first school?",
        "What street did you grow up on?",
        "What was your childhood nickname?",
        "What is your favorite book?",
        "What was the make of your first car?"
    )

    // Handle success state
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignUpSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Icon Section
        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.size(100.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 56.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join MoodFood and start your wellness journey",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        // Signup Form Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email Field
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    isError = uiState.errorMessage != null,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                // Password Field
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    isError = uiState.errorMessage != null,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                // Confirm Password Field
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    isPassword = true,
                    isError = uiState.errorMessage != null,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Security Questions Toggle
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showSecurityQuestions = !showSecurityQuestions }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Security Questions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Required for password recovery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (showSecurityQuestions) "▲" else "▼",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // Security Questions Section (Expandable)
                if (showSecurityQuestions) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Question 1
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        var expanded1 by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded1,
                            onExpandedChange = { expanded1 = it }
                        ) {
                            OutlinedTextField(
                                value = securityQuestion1,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Security Question 1") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded1,
                                onDismissRequest = { expanded1 = false }
                            ) {
                                availableQuestions.forEach { question ->
                                    DropdownMenuItem(
                                        text = { Text(question) },
                                        onClick = {
                                            securityQuestion1 = question
                                            expanded1 = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    AuthTextField(
                        value = securityAnswer1,
                        onValueChange = { securityAnswer1 = it },
                        label = "Your Answer",
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Question 2
                    var expanded2 by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded2,
                        onExpandedChange = { expanded2 = it }
                    ) {
                        OutlinedTextField(
                            value = securityQuestion2,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Security Question 2") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded2,
                            onDismissRequest = { expanded2 = false }
                        ) {
                            availableQuestions
                                .filter { it != securityQuestion1 } // Don't show same question
                                .forEach { question ->
                                    DropdownMenuItem(
                                        text = { Text(question) },
                                        onClick = {
                                            securityQuestion2 = question
                                            expanded2 = false
                                        }
                                    )
                                }
                        }
                    }
                    
                    AuthTextField(
                        value = securityAnswer2,
                        onValueChange = { securityAnswer2 = it },
                        label = "Your Answer",
                        imeAction = ImeAction.Done,
                        onImeAction = { 
                            focusManager.clearFocus()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sign Up Button
                AuthButton(
                    text = "Create Account",
                    onClick = {
                        if (securityAnswer1.isNotBlank() && securityAnswer2.isNotBlank()) {
                            viewModel.signUpWithSecurityQuestions(
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                securityQuestion1 = securityQuestion1,
                                securityAnswer1 = securityAnswer1,
                                securityQuestion2 = securityQuestion2,
                                securityAnswer2 = securityAnswer2
                            )
                        } else {
                            viewModel.signUp(email, password, confirmPassword)
                        }
                    },
                    enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                    loading = uiState.isLoading
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        uiState.errorMessage?.let { errorMessage ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Navigation Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AuthLinkButton(
                text = "Sign In",
                onClick = onNavigateToLogin
            )
        }
    }
}
