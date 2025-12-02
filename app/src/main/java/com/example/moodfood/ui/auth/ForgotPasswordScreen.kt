package com.example.moodfood.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var email by remember { mutableStateOf("") }
    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var securityQuestion1 by remember { mutableStateOf("") }
    var securityQuestion2 by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            )

            AnimatedContent(
                targetState = currentStep,
                label = "step_animation"
            ) { step ->
                when (step) {
                    1 -> EmailStep(
                        email = email,
                        onEmailChange = { 
                            email = it
                            errorMessage = ""
                        },
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onNext = {
                            isLoading = true
                            errorMessage = ""
                            
                            // Validate email
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email"
                                isLoading = false
                                return@EmailStep
                            }
                            
                            // Fetch security questions
                            viewModel.getSecurityQuestions(email) { questions ->
                                isLoading = false
                                if (questions != null) {
                                    securityQuestion1 = questions.first ?: ""
                                    securityQuestion2 = questions.second ?: ""
                                    
                                    if (securityQuestion1.isBlank() || securityQuestion2.isBlank()) {
                                        errorMessage = "No security questions found for this account"
                                    } else {
                                        currentStep = 2
                                    }
                                } else {
                                    errorMessage = "User not found or account doesn't support password reset"
                                }
                            }
                        }
                    )

                    2 -> SecurityQuestionsStep(
                        question1 = securityQuestion1,
                        question2 = securityQuestion2,
                        answer1 = answer1,
                        answer2 = answer2,
                        onAnswer1Change = { 
                            answer1 = it
                            errorMessage = ""
                        },
                        onAnswer2Change = { 
                            answer2 = it
                            errorMessage = ""
                        },
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onBack = { currentStep = 1 },
                        onNext = {
                            isLoading = true
                            errorMessage = ""
                            
                            if (answer1.isBlank() || answer2.isBlank()) {
                                errorMessage = "Please answer both questions"
                                isLoading = false
                                return@SecurityQuestionsStep
                            }
                            
                            // Verify answers
                            viewModel.verifySecurityAnswers(email, answer1, answer2) { isValid ->
                                isLoading = false
                                if (isValid) {
                                    currentStep = 3
                                } else {
                                    errorMessage = "Incorrect answers. Please try again."
                                }
                            }
                        }
                    )

                    3 -> NewPasswordStep(
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        onNewPasswordChange = { 
                            newPassword = it
                            errorMessage = ""
                        },
                        onConfirmPasswordChange = { 
                            confirmPassword = it
                            errorMessage = ""
                        },
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onBack = { currentStep = 2 },
                        onSubmit = {
                            isLoading = true
                            errorMessage = ""
                            
                            // Validate passwords
                            if (newPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                isLoading = false
                                return@NewPasswordStep
                            }
                            
                            if (newPassword != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                isLoading = false
                                return@NewPasswordStep
                            }
                            
                            // Reset password
                            viewModel.resetPassword(email, newPassword, answer1, answer2) { success, message ->
                                isLoading = false
                                if (success) {
                                    onResetSuccess()
                                } else {
                                    errorMessage = message ?: "Failed to reset password"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 1: Enter Your Email",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "We'll verify your identity using your security questions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onNext() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SecurityQuestionsStep(
    question1: String,
    question2: String,
    answer1: String,
    answer2: String,
    onAnswer1Change: (String) -> Unit,
    onAnswer2Change: (String) -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 2: Security Questions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Answer your security questions to verify your identity",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = question1,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = answer1,
                    onValueChange = onAnswer1Change,
                    label = { Text("Your Answer") },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = question2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = answer2,
                    onValueChange = onAnswer2Change,
                    label = { Text("Your Answer") },
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onNext() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Back")
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verify", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 3: New Password",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Choose a strong password to secure your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("New Password") },
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Back")
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Reset Password", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
