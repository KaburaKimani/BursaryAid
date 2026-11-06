package com.example.bursaryaid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bursaryaid.contract_stubs.InMemoryApplicationRepository
import com.example.bursaryaid.contract_stubs.InMemoryBursaryRepository
import com.example.bursaryaid.contract_stubs.InMemoryStudentProfileRepository
import com.example.bursaryaid.feature.auth.ui.AdminDashboardScreen
import com.example.bursaryaid.feature.auth.ui.LoginScreen
import com.example.bursaryaid.feature.auth.ui.RegisterScreen
import com.example.bursaryaid.feature.student.ui.ApplicationDetailsScreen
import com.example.bursaryaid.feature.student.ui.ApplicationFormScreen
import com.example.bursaryaid.feature.student.ui.MyApplicationsScreen
import com.example.bursaryaid.feature.student.ui.StudentProfileScreen
import com.example.bursaryaid.feature.student.viewmodel.ApplicationDetailsViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.ApplicationFormViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.MyApplicationsViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.StudentProfileViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BursaryAppNavigation()
        }
    }
}

@Composable
fun BursaryAppNavigation() {
    val navController = rememberNavController()

    // Stub repositories (replaced by Member 3's Room DB later)
    val profileRepo = remember { InMemoryStudentProfileRepository() }
    val applicationRepo = remember { InMemoryApplicationRepository() }
    val bursaryRepo = remember { InMemoryBursaryRepository() }

    // Set by the login screen (Member 1)
    var currentUserId by remember { mutableLongStateOf(0L) }

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login (Member 1)
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { userId, isAdmin ->
                        currentUserId = userId
                        val destination = if (isAdmin) "admin_dashboard" else "my_applications"
                        navController.navigate(destination) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            // Registration (Member 1)
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // Admin dashboard (Member 1 placeholder; Member 4 fills in review)
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    onReviewApplications = { /* Member 4's review screen goes here */ },
                    onLogout = {
                        currentUserId = 0L
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // My Applications List
            composable("my_applications") {
                val factory = remember {
                    MyApplicationsViewModelFactory(
                        userId = currentUserId,
                        studentProfileRepository = profileRepo,
                        applicationRepository = applicationRepo
                    )
                }
                MyApplicationsScreen(
                    factory = factory,
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToNewApplication = { navController.navigate("form?bursaryId=1") },
                    onSelectApplication = { appId -> navController.navigate("details/$appId") }
                )
            }

            // Student Profile
            composable("profile") {
                val factory = remember {
                    StudentProfileViewModelFactory(
                        userId = currentUserId,
                        repository = profileRepo
                    )
                }
                StudentProfileScreen(
                    factory = factory,
                    onSaved = { navController.popBackStack() }
                )
            }

            // Application Form (Create/Edit Draft)
            composable(
                route = "form?bursaryId={bursaryId}&appId={appId}",
                arguments = listOf(
                    navArgument("bursaryId") { type = NavType.LongType; defaultValue = 1L },
                    navArgument("appId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val bursaryId = backStackEntry.arguments?.getLong("bursaryId") ?: 1L
                val appIdString = backStackEntry.arguments?.getString("appId")
                val selectedAppId = appIdString?.toLongOrNull()

                val factory = remember {
                    ApplicationFormViewModelFactory(
                        userId = currentUserId,
                        bursaryId = bursaryId,
                        selectedApplicationId = selectedAppId,
                        studentProfileRepository = profileRepo,
                        bursaryRepository = bursaryRepo,
                        applicationRepository = applicationRepo,
                        onApplicationSubmitted = { /* Notification hook for Member 5 */ }
                    )
                }
                ApplicationFormScreen(
                    factory = factory,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Application Details
            composable(
                route = "details/{appId}",
                arguments = listOf(navArgument("appId") { type = NavType.LongType })
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getLong("appId") ?: return@composable

                val factory = remember {
                    ApplicationDetailsViewModelFactory(
                        userId = currentUserId,
                        applicationId = appId,
                        applicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo
                    )
                }
                ApplicationDetailsScreen(
                    factory = factory,
                    onNavigateBack = { navController.popBackStack() },
                    onEditApplication = { editId ->
                        navController.navigate("form?bursaryId=1&appId=$editId")
                    }
                )
            }
        }
    }
}