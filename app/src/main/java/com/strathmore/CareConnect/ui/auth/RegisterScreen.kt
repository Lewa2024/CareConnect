package com.strathmore.CareConnect.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strathmore.CareConnect.location.DeviceLocation
import com.strathmore.CareConnect.location.LocationHelper
import com.strathmore.CareConnect.ui.components.AppLogo
import kotlinx.coroutines.launch

private enum class RegisterRole { CAREGIVER, NURSE }

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onCaregiverRegistered: (Long) -> Unit,
    onNurseRegistered: (Long) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var role by remember { mutableStateOf(RegisterRole.CAREGIVER) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }

    var deviceLocation by remember { mutableStateOf<DeviceLocation?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authState by viewModel.authState.collectAsState()

    fun fetchLocation() {
        scope.launch {
            isFetchingLocation = true
            locationError = null
            val result = LocationHelper.getCurrentLocation(context)
            isFetchingLocation = false
            if (result == null) {
                locationError = "Couldn't determine your location. Make sure location is enabled and try again."
            } else {
                deviceLocation = result
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation() else locationError = "Location permission is needed so caregivers can find you nearby."
    }

    fun requestLocation() {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) fetchLocation() else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(authState) {
        val state = authState
        if (state is AuthState.CaregiverSuccess) {
            onCaregiverRegistered(state.caregiverId)
            viewModel.resetState()
        } else if (state is AuthState.NurseSuccess) {
            onNurseRegistered(state.nurseId)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Create an Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = role == RegisterRole.CAREGIVER,
                onClick = { role = RegisterRole.CAREGIVER },
                label = { Text("Caregiver") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = role == RegisterRole.NURSE,
                onClick = { role = RegisterRole.NURSE },
                label = { Text("Nurse") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = firstName, onValueChange = { firstName = it },
            label = { Text("First name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lastName, onValueChange = { lastName = it },
            label = { Text("Last name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber, onValueChange = { phoneNumber = it },
            label = { Text("Phone number") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (role == RegisterRole.CAREGIVER) {
            OutlinedTextField(
                value = relationship, onValueChange = { relationship = it },
                label = { Text("Relationship to patient (e.g. Daughter)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = qualification, onValueChange = { qualification = it },
                label = { Text("Qualification (e.g. Registered Nurse)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your location",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    val loc = deviceLocation
                    if (loc != null) {
                        Text(
                            "Captured: ${loc.city}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Caregivers find nurses by distance, so we need your current location.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { requestLocation() },
                        enabled = !isFetchingLocation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            when {
                                isFetchingLocation -> "Getting location..."
                                loc != null -> "Refresh location"
                                else -> "Share my location"
                            }
                        )
                    }

                    locationError?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (role == RegisterRole.CAREGIVER) {
                    viewModel.register(firstName, lastName, email, password, phoneNumber, relationship)
                } else {
                    val loc = deviceLocation
                    if (loc == null) {
                        locationError = "Please share your location before registering."
                        return@Button
                    }
                    viewModel.registerNurse(
                        firstName, lastName, email, password, phoneNumber,
                        qualification, loc.city, loc.latitude, loc.longitude
                    )
                }
            },
            enabled = authState !is AuthState.Loading,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (authState is AuthState.Loading) "Creating account..." else "Register")
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Already have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text("Log in", fontWeight = FontWeight.Bold)
            }
        }
    }
}