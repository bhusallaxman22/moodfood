package com.example.moodfood.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.moodfood.data.auth.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepo = AuthRepository.get(context)
    val currentUserState by authRepo.currentUser.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNotAuthDialog by remember { mutableStateOf(false) }

    // If user is not authenticated, prompt to login
    LaunchedEffect(currentUserState) {
        if (currentUserState == null) {
            showNotAuthDialog = true
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Change Password") }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Back")
            }
        })
    }) { inner ->
        Column(modifier = Modifier
            .padding(inner)
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Top) {

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                errorMessage = null
                if (currentUserState == null) {
                    showNotAuthDialog = true
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    errorMessage = "New passwords do not match"
                    return@Button
                }
                if (newPassword.length < 6) {
                    errorMessage = "New password must be at least 6 characters"
                    return@Button
                }

                isLoading = true
                coroutineScope.launch {
                    val result = authRepo.changePassword(currentPassword, newPassword)
                    isLoading = false
                    when (result) {
                        is com.example.moodfood.data.auth.AuthResult.Success -> {
                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                        is com.example.moodfood.data.auth.AuthResult.Error -> {
                            errorMessage = result.message
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Password")
            }
        }
    }

    if (showNotAuthDialog) {
        AlertDialog(
            onDismissRequest = { showNotAuthDialog = false },
            title = { Text("Not Signed In") },
            text = { Text("You must be signed in to change your password. Would you like to go to the login screen?") },
            confirmButton = {
                TextButton(onClick = {
                    showNotAuthDialog = false
                    navController.navigate(com.example.moodfood.navigation.NavRoute.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }) { Text("Go to Login") }
            },
            dismissButton = {
                TextButton(onClick = { showNotAuthDialog = false }) { Text("Cancel") }
            }
        )
    }
}
