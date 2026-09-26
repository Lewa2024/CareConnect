package com.strathmore.CareConnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.strathmore.CareConnect.ui.auth.LoginScreen
import com.strathmore.CareConnect.ui.auth.RegisterScreen
import com.strathmore.CareConnect.ui.patient.AddPatientScreen
import com.strathmore.CareConnect.ui.symptom.SymptomLogScreen
import com.strathmore.CareConnect.ui.theme.CareConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareConnectTheme {
                CareConnectNavHost()
            }
        }
    }
}

@Composable
private fun CareConnectNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoggedIn = { caregiverId ->
                    navController.navigate("add_patient/$caregiverId")
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegistered = { caregiverId ->
                    navController.navigate("add_patient/$caregiverId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "add_patient/{caregiverId}",
            arguments = listOf(navArgument("caregiverId") { type = NavType.LongType })
        ) { backStackEntry ->
            val caregiverId = backStackEntry.arguments?.getLong("caregiverId") ?: return@composable
            AddPatientScreen(
                caregiverId = caregiverId,
                onPatientAdded = { patientId ->
                    navController.navigate("symptom_log/$patientId") {
                        popUpTo("add_patient/$caregiverId") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "symptom_log/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            SymptomLogScreen(
                caregiverId = 0L, // not used by SymptomLogScreen's patient list right now, see note below
                patientId = patientId
            )
        }
    }
}