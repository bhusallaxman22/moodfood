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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.ui.components.AuthButton
import com.example.moodfood.ui.components.AuthLinkButton
import com.example.moodfood.ui.components.AuthTextField

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
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()
    
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
        // Title
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Join MoodFood and start your wellness journey",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm Password Field
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isPassword = true,
            isError = uiState.errorMessage != null,
            imeAction = ImeAction.Done,
            onImeAction = { 
                focusManager.clearFocus()
                viewModel.signUp(email, password, confirmPassword)
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign Up Button
        AuthButton(
            text = "Create Account",
            onClick = { viewModel.signUp(email, password, confirmPassword) },
            enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            loading = uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Message
        uiState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Navigation Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
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
