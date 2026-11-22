package com.example.bursaryaid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bursaryaid.contract_stubs.InMemoryApplicationRepository
import com.example.bursaryaid.contract_stubs.InMemoryBursaryRepository
import com.example.bursaryaid.contract_stubs.InMemoryStudentProfileRepository
import com.example.bursaryaid.database.AppDatabase
import com.example.bursaryaid.feature.auth.ui.AdminDashboardScreen
import com.example.bursaryaid.feature.auth.ui.LoginScreen
import com.example.bursaryaid.feature.auth.ui.RegisterScreen
import com.example.bursaryaid.feature.auth.ui.StudentDashboardScreen
import com.example.bursaryaid.feature.auth.viewmodel.AuthViewModel
import com.example.bursaryaid.feature.auth.viewmodel.AuthViewModelFactory
import com.example.bursaryaid.feature.bursary.ui.BursaryDetailsScreen
import com.example.bursaryaid.feature.bursary.ui.BursaryListScreen
import com.example.bursaryaid.feature.bursary.viewmodel.BursaryBrowseViewModel
import com.example.bursaryaid.feature.student.ui.ApplicationDetailsScreen
import com.example.bursaryaid.feature.student.ui.ApplicationFormScreen
import com.example.bursaryaid.feature.student.ui.MyApplicationsScreen
import com.example.bursaryaid.feature.student.ui.StudentProfileScreen
import com.example.bursaryaid.feature.student.viewmodel.ApplicationDetailsViewModel
import com.example.bursaryaid.feature.student.viewmodel.ApplicationFormViewModel
import com.example.bursaryaid.feature.student.viewmodel.MyApplicationsViewModel
import com.example.bursaryaid.feature.student.viewmodel.StudentProfileViewModel
import com.example.bursaryaid.feature.student.viewmodel.applicationDetailsViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.applicationFormViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.myApplicationsViewModelFactory
import com.example.bursaryaid.feature.student.viewmodel.studentProfileViewModelFactory
import com.example.bursaryaid.navigation.Routes
import com.example.bursaryaid.navigation.navigateToApplicationDetails
import com.example.bursaryaid.navigation.navigateToBursaryDetails
import com.example.bursaryaid.navigation.navigateToDashboard
import com.example.bursaryaid.repository.UserRepository

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
    val context = LocalContext.current
    val navController = rememberNavController()

    val userRepository = remember { UserRepository(AppDatabase.getDatabase(context).userDao()) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userRepository))
    val bursaryBrowseViewModel: BursaryBrowseViewModel = viewModel()

    // Stub repositories (replaced by Room DB later)
    val profileRepo = remember { InMemoryStudentProfileRepository() }
    val applicationRepo = remember { InMemoryApplicationRepository() }
    val bursaryRepo = remember { InMemoryBursaryRepository() }

    val currentUserId = authViewModel.currentUserId

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { isAdmin -> navController.navigateToDashboard(isAdmin) },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            // Registration
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

            // Student dashboard
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

            // Admin dashboard
            composable(Routes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    onReviewApplications = { /* Admin review screen */ },
                    onLogout = {
                        authViewModel.logoutUser()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Bursary list
            composable(Routes.BURSARY_LIST) {
                BursaryListScreen(
                    viewModel = bursaryBrowseViewModel,
                    onSelectBursary = { id -> navController.navigateToBursaryDetails(id) },
                    onBack = { navController.popBackStack() }
                )
            }

            // Bursary details
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
                val myApplicationsViewModel: MyApplicationsViewModel = viewModel(
                    key = "my-applications-$currentUserId",
                    factory = myApplicationsViewModelFactory(
                        userId = currentUserId,
                        studentProfileRepository = profileRepo,
                        applicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo
                    )
                )
                MyApplicationsScreen(
                    viewModel = myApplicationsViewModel,
                    onNewApplication = { navController.navigate(Routes.BURSARY_LIST) },
                    onApplicationClick = { appId -> navController.navigateToApplicationDetails(appId) },
                    onCompleteProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Student Profile
            composable(Routes.PROFILE) {
                val profileViewModel: StudentProfileViewModel = viewModel(
                    key = "profile-$currentUserId",
                    factory = studentProfileViewModelFactory(
                        userId = currentUserId,
                        studentProfileRepository = profileRepo
                    )
                )
                StudentProfileScreen(
                    viewModel = profileViewModel,
                    onProfileSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
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

                val formViewModel: ApplicationFormViewModel = viewModel(
                    key = "form-$currentUserId-$bursaryId-$selectedAppId",
                    factory = applicationFormViewModelFactory(
                        userId = currentUserId,
                        selectedApplicationId = selectedAppId,
                        studentProfileRepository = profileRepo,
                        applicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo
                    )
                )
                ApplicationFormScreen(
                    viewModel = formViewModel,
                    onApplicationSaved = { navController.popBackStack() },
                    onCompleteProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Application Details
            composable(
                route = Routes.DETAILS_PATTERN,
                arguments = listOf(navArgument(Routes.ARG_APP_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getLong(Routes.ARG_APP_ID) ?: return@composable

                val detailsViewModel: ApplicationDetailsViewModel = viewModel(
                    key = "details-$currentUserId-$appId",
                    factory = applicationDetailsViewModelFactory(
                        userId = currentUserId,
                        selectedApplicationId = appId,
                        studentProfileRepository = profileRepo,
                        applicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo
                    )
                )
                ApplicationDetailsScreen(
                    viewModel = detailsViewModel,
                    onEditApplication = { editId ->
                        navController.navigate(Routes.form(bursaryId = 1L, appId = editId))
                    },
                    onApplicationDeleted = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
