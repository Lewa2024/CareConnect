package com.strathmore.CareConnect.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.ui.components.AppLogo
import androidx.compose.ui.graphics.Color
import com.strathmore.CareConnect.ui.components.blackTextFieldColors

@Composable
fun VerifyEmailScreen(
    userId: Long,
    email: String,
    viewModel: AuthViewModel = viewModel(),
    onCaregiverVerified: (Long) -> Unit,
    onAdminVerified: (Long) -> Unit,
    onNurseVerified: (Long) -> Unit,
    onBackToLogin: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.CaregiverSuccess -> {
                onCaregiverVerified(state.caregiverId)
                viewModel.resetState()
                viewModel.resetVerificationState()
            }
            is AuthState.AdminSuccess -> {
                onAdminVerified(state.userId)
                viewModel.resetState()
                viewModel.resetVerificationState()
            }
            is AuthState.NurseSuccess -> {
                onNurseVerified(state.nurseId)
                viewModel.resetState()
                viewModel.resetVerificationState()
            }
            else -> { /* no-op for Idle, Loading, PendingVerification, Error */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(large = true)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Verify your email",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "We've sent a 6-digit code to $email. Enter it below to finish setting up your account.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            colors = blackTextFieldColors(),
            value = code,
            onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit) },
            label = { Text("Verification code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.verifyEmail(userId, code) },
            enabled = verificationState !is VerificationState.Sending,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (verificationState is VerificationState.Sending) "Checking..." else "Verify")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.resendVerificationCode(userId) },
            enabled = verificationState !is VerificationState.Sending
        ) {
            Text("Resend code", color = Color.Black)
        }

        when (val state = verificationState) {
            is VerificationState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            VerificationState.CodeSent -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "A new code has been sent to $email.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> { /* no message for Idle/Sending */ }
        }

        Spacer(modifier = Modifier.height(28.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Back to login", color = Color.Black)
        }
    }
}