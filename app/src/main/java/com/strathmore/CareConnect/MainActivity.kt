package com.strathmore.CareConnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.strathmore.CareConnect.ui.admin.AdminDashboardScreen
import com.strathmore.CareConnect.ui.admin.AdminProfileScreen
import com.strathmore.CareConnect.ui.admin.CaregiverDirectoryScreen
import com.strathmore.CareConnect.ui.admin.DeletedSymptomsScreen
import com.strathmore.CareConnect.ui.admin.ManageThresholdsScreen
import com.strathmore.CareConnect.ui.admin.SystemOverviewScreen
import com.strathmore.CareConnect.ui.alerts.AlertsScreen
import com.strathmore.CareConnect.ui.auth.ForgotPasswordScreen
import com.strathmore.CareConnect.ui.auth.LoginScreen
import com.strathmore.CareConnect.ui.auth.RegisterScreen
import com.strathmore.CareConnect.ui.home.HomeScreen
import com.strathmore.CareConnect.ui.medication.AddMedicationScreen
import com.strathmore.CareConnect.ui.medication.MedicationListScreen
import com.strathmore.CareConnect.ui.nurse.FindNurseScreen
import com.strathmore.CareConnect.ui.nurse.NurseHomeScreen
import com.strathmore.CareConnect.ui.nurse.RequestBookingScreen
import com.strathmore.CareConnect.ui.patient.AddPatientScreen
import com.strathmore.CareConnect.ui.patient.EditPatientScreen
import com.strathmore.CareConnect.ui.patient.PatientDetailsScreen
import com.strathmore.CareConnect.ui.profile.ProfileScreen
import com.strathmore.CareConnect.ui.symptom.EditSymptomScreen
import com.strathmore.CareConnect.ui.symptom.PainGuideScreen
import com.strathmore.CareConnect.ui.symptom.SymptomHistoryScreen
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

    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut()
        }
    ) {

        composable("login") {
            LoginScreen(
                onCaregiverLoggedIn = { caregiverId ->
                    navController.navigate("home/$caregiverId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onAdminLoggedIn = { userId ->
                    navController.navigate("admin_dashboard/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNurseLoggedIn = { nurseId ->
                    navController.navigate("nurse_home/$nurseId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onCaregiverRegistered = { caregiverId ->
                    navController.navigate("home/$caregiverId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNurseRegistered = { nurseId ->
                    navController.navigate("nurse_home/$nurseId") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onResetSuccess = {
                    navController.popBackStack()
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "home/{caregiverId}",
            arguments = listOf(navArgument("caregiverId") { type = NavType.LongType })
        ) { backStackEntry ->
            val caregiverId = backStackEntry.arguments?.getLong("caregiverId") ?: return@composable
            HomeScreen(
                caregiverId = caregiverId,
                onLogSymptoms = { patientId ->
                    navController.navigate("symptom_log/$caregiverId/$patientId")
                },
                onViewHistory = { patientId ->
                    navController.navigate("symptom_history/$patientId")
                },
                onAddPatient = {
                    navController.navigate("add_patient/$caregiverId")
                },
                onEditPatient = { patientId ->
                    navController.navigate("edit_patient/$patientId")
                },
                onViewPatientDetails = { patientId ->
                    navController.navigate("patient_details/$patientId")
                },
                onViewAlerts = { patientId ->
                    navController.navigate("alerts/$caregiverId/$patientId")
                },
                onPainGuide = {
                    navController.navigate("pain_guide")
                },
                onFindNurse = { patientId ->
                    navController.navigate("find_nurse/$patientId")
                },
                onProfile = {
                    navController.navigate("profile/$caregiverId")
                }
            )
        }

        composable(
            route = "profile/{caregiverId}",
            arguments = listOf(navArgument("caregiverId") { type = NavType.LongType })
        ) { backStackEntry ->
            val caregiverId = backStackEntry.arguments?.getLong("caregiverId") ?: return@composable
            ProfileScreen(
                caregiverId = caregiverId,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate("home/$caregiverId") {
                        popUpTo("home/$caregiverId") { inclusive = true }
                    }
                },
                onHistory = {
                    navController.navigate("home/$caregiverId") {
                        popUpTo("home/$caregiverId") { inclusive = true }
                    }
                },
                onComingSoon = { feature -> }
            )
        }

        composable(
            route = "add_patient/{caregiverId}",
            arguments = listOf(navArgument("caregiverId") { type = NavType.LongType })
        ) { backStackEntry ->
            val caregiverId = backStackEntry.arguments?.getLong("caregiverId") ?: return@composable
            AddPatientScreen(
                caregiverId = caregiverId,
                onPatientAdded = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "edit_patient/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            EditPatientScreen(
                patientId = patientId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "patient_details/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            PatientDetailsScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("edit_patient/$id") },
                onViewMedications = { id -> navController.navigate("medication_list/$id") }
            )
        }

        composable(
            route = "medication_list/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            MedicationListScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() },
                onAddMedication = { navController.navigate("add_medication/$patientId") }
            )
        }

        composable(
            route = "add_medication/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            AddMedicationScreen(
                patientId = patientId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "symptom_log/{caregiverId}/{patientId}",
            arguments = listOf(
                navArgument("caregiverId") { type = NavType.LongType },
                navArgument("patientId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val caregiverId = backStackEntry.arguments?.getLong("caregiverId") ?: return@composable
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            SymptomLogScreen(
                caregiverId = caregiverId,
                patientId = patientId,
                onBackToHome = { navController.popBackStack() },
                onPainGuide = { navController.navigate("pain_guide") }
            )
        }

        composable(
            route = "symptom_history/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            SymptomHistoryScreen(
                patientId = patientId,
                onBackToHome = { navController.popBackStack() },
                onEditSymptom = { symptomId ->
                    navController.navigate("edit_symptom/$symptomId")
                }
            )
        }

        composable(
            route = "edit_symptom/{symptomId}",
            arguments = listOf(navArgument("symptomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val symptomId = backStackEntry.arguments?.getLong("symptomId") ?: return@composable
            EditSymptomScreen(
                symptomId = symptomId,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onPainGuide = { navController.navigate("pain_guide") }
            )
        }

        composable("pain_guide") {
            PainGuideScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "alerts/{caregiverId}/{patientId}",
            arguments = listOf(
                navArgument("caregiverId") { type = NavType.LongType },
                navArgument("patientId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val caregiverId = backStackEntry.arguments?.getLong("caregiverId") ?: return@composable
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            AlertsScreen(
                patientId = patientId,
                onHome = { navController.popBackStack() },
                onHistory = {
                    navController.navigate("symptom_history/$patientId")
                },
                onProfile = {
                    navController.navigate("profile/$caregiverId")
                },
                onComingSoon = { }
            )
        }

        composable(
            route = "find_nurse/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            FindNurseScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() },
                onBookNurse = { nurseId ->
                    navController.navigate("request_booking/$patientId/$nurseId")
                }
            )
        }

        composable(
            route = "request_booking/{patientId}/{nurseId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.LongType },
                navArgument("nurseId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            val nurseId = backStackEntry.arguments?.getLong("nurseId") ?: return@composable
            RequestBookingScreen(
                patientId = patientId,
                nurseId = nurseId,
                onBack = { navController.popBackStack() },
                onRequested = {
                    navController.popBackStack("find_nurse/$patientId", inclusive = true)
                }
            )
        }

        composable(
            route = "nurse_home/{nurseId}",
            arguments = listOf(navArgument("nurseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val nurseId = backStackEntry.arguments?.getLong("nurseId") ?: return@composable
            NurseHomeScreen(
                nurseId = nurseId,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "admin_dashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            AdminDashboardScreen(
                onManageThresholds = { navController.navigate("manage_thresholds") },
                onViewDeletedSymptoms = { navController.navigate("deleted_symptoms") },
                onViewSystemOverview = { navController.navigate("system_overview") },
                onViewCaregivers = { navController.navigate("caregiver_directory") },
                onViewProfile = { navController.navigate("admin_profile/$userId") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "admin_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            AdminProfileScreen(
                adminUserId = userId,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("manage_thresholds") {
            ManageThresholdsScreen(onBack = { navController.popBackStack() })
        }

        composable("deleted_symptoms") {
            DeletedSymptomsScreen(onBack = { navController.popBackStack() })
        }

        composable("system_overview") {
            SystemOverviewScreen(onBack = { navController.popBackStack() })
        }

        composable("caregiver_directory") {
            CaregiverDirectoryScreen(onBack = { navController.popBackStack() })
        }
    }
}