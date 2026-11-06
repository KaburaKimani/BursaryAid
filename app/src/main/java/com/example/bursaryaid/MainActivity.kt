package com.example.bursaryaid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.bursaryaid.feature.auth.ui.StudentDashboardScreen
import com.example.bursaryaid.feature.auth.viewmodel.AuthViewModel
import com.example.bursaryaid.feature.bursary.ui.BursaryDetailsScreen
import com.example.bursaryaid.feature.bursary.ui.BursaryListScreen
import com.example.bursaryaid.feature.bursary.viewmodel.BursaryBrowseViewModel
import com.example.bursaryaid.feature.student.ui.ApplicationDetailsScreen
import com.example.bursaryaid.feature.student.ui.ApplicationFormScreen
import com.example.bursaryaid.feature.student.ui.MyApplicationsScreen
import com.example.bursaryaid.feature.student.ui.StudentProfileScreen
import com.example.bursaryaid.feature.student.viewmodel.ApplicationDetailsViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.ApplicationFormViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.MyApplicationsViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.StudentProfileViewModelFactory
import com.example.bursaryaid.navigation.Routes
import com.example.bursaryaid.navigation.navigateToApplicationDetails
import com.example.bursaryaid.navigation.navigateToBursaryDetails
import com.example.bursaryaid.navigation.navigateToDashboard

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
    val authViewModel: AuthViewModel = viewModel()
    val bursaryBrowseViewModel: BursaryBrowseViewModel = viewModel()

    // Stub repositories (replaced by Member 3's Room DB later)
    val profileRepo = remember { InMemoryStudentProfileRepository() }
    val applicationRepo = remember { InMemoryApplicationRepository() }
    val bursaryRepo = remember { InMemoryBursaryRepository() }

    // Set by login (Member 1)
    val currentUserId = authViewModel.currentUserId

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login (Member 1)
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { isAdmin -> navController.navigateToDashboard(isAdmin) },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            // Registration (Member 1)
            composable(Routes.REGISTER) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // Student dashboard (Member 1)
            composable(Routes.STUDENT_DASHBOARD) {
                StudentDashboardScreen(
                    onMyApplications = { navController.navigate(Routes.MY_APPLICATIONS) },
                    onNewApplication = { navController.navigate(Routes.BURSARY_LIST) },
                    onProfile = { navController.navigate(Routes.PROFILE) },
                    onLogout = {
                        authViewModel.logoutUser()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Admin dashboard (Member 1 placeholder; Member 4 fills in review)
            composable(Routes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    onReviewApplications = { /* Member 4's review screen goes here */ },
                    onLogout = {
                        authViewModel.logoutUser()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Bursary list (Member 1)
            composable(Routes.BURSARY_LIST) {
                BursaryListScreen(
                    viewModel = bursaryBrowseViewModel,
                    onSelectBursary = { id -> navController.navigateToBursaryDetails(id) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Bursary details (Member 1)
            composable(
                route = Routes.BURSARY_DETAILS_PATTERN,
                arguments = listOf(navArgument(Routes.ARG_BURSARY_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val bursaryId = backStackEntry.arguments?.getLong(Routes.ARG_BURSARY_ID) ?: return@composable
                BursaryDetailsScreen(
                    viewModel = bursaryBrowseViewModel,
                    bursaryId = bursaryId,
                    onApply = { id -> navController.navigate(Routes.form(bursaryId = id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            // My Applications List
            composable(Routes.MY_APPLICATIONS) {
                val factory = remember {
                    MyApplicationsViewModelFactory(
                        userId = currentUserId,
                        studentProfileRepository = profileRepo,
                        applicationRepository = applicationRepo
                    )
                }
                MyApplicationsScreen(
                    factory = factory,
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToNewApplication = { navController.navigate(Routes.BURSARY_LIST) },
                    onSelectApplication = { appId -> navController.navigateToApplicationDetails(appId) }
                )
            }

            // Student Profile
            composable(Routes.PROFILE) {
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
                route = Routes.FORM_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_BURSARY_ID) { type = NavType.LongType; defaultValue = 1L },
                    navArgument(Routes.ARG_APP_ID) { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val bursaryId = backStackEntry.arguments?.getLong(Routes.ARG_BURSARY_ID) ?: 1L
                val appIdString = backStackEntry.arguments?.getString(Routes.ARG_APP_ID)
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
                route = Routes.DETAILS_PATTERN,
                arguments = listOf(navArgument(Routes.ARG_APP_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getLong(Routes.ARG_APP_ID) ?: return@composable

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
                        navController.navigate(Routes.form(bursaryId = 1L, appId = editId))
                    }
                )
            }
        }
    }
}