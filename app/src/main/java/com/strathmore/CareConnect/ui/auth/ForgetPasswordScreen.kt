package com.strathmore.CareConnect.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel(),
    onResetSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmError by remember { mutableStateOf<String?>(null) }

    // false = stage 1 (enter email), true = stage 2 (enter code + new password)
    var codeSent by remember { mutableStateOf(false) }

    val resetState by viewModel.resetPasswordState.collectAsState()

    LaunchedEffect(resetState) {
        when (resetState) {
            is ResetPasswordState.CodeSent -> codeSent = true
            is ResetPasswordState.Success -> {
                onResetSuccess()
                viewModel.resetPasswordStateReset()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppLogo() },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Login")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Reset Password", style = MaterialTheme.typography.headlineSmall)

            if (!codeSent) {
                // ---------- Stage 1: ask for the email and send the code ----------
                Text(
                    "Enter your account email and we'll send you a 6-digit code.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.requestPasswordReset(email.trim()) },
                    enabled = resetState !is ResetPasswordState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (resetState is ResetPasswordState.Loading) "Sending..." else "Send code")
                }
            } else {
                // ---------- Stage 2: verify the code, then set the new password ----------
                Text(
                    "We sent a 6-digit code to ${email.trim()}. Enter it below and choose a new password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("6-digit code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; confirmError = null },
                    label = { Text("New password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmError = null },
                    label = { Text("Confirm new password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = confirmError != null,
                    supportingText = { confirmError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            confirmError = "Passwords don't match"
                            return@Button
                        }
                        viewModel.confirmPasswordReset(email.trim(), code, newPassword)
                    },
                    enabled = resetState !is ResetPasswordState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (resetState is ResetPasswordState.Loading) "Resetting..." else "Reset Password")
                }

                TextButton(
                    onClick = { viewModel.requestPasswordReset(email.trim()) },
                    enabled = resetState !is ResetPasswordState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Resend code")
                }

                TextButton(
                    onClick = {
                        codeSent = false
                        code = ""
                        newPassword = ""
                        confirmPassword = ""
                        viewModel.resetPasswordStateReset()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use a different email")
                }
            }

            if (resetState is ResetPasswordState.Error) {
                Text(
                    (resetState as ResetPasswordState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}