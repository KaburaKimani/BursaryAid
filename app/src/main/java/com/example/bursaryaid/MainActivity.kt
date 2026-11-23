
package com.example.bursaryaid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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

import com.example.bursaryaid.feature.admin.data.RoomAdminReviewRepository
import com.example.bursaryaid.feature.admin.ui.AdminApplicationsScreen
import com.example.bursaryaid.feature.admin.ui.AdminBursaryOverviewScreen
import com.example.bursaryaid.feature.admin.ui.AdminCreateBursaryScreen
import com.example.bursaryaid.feature.admin.ui.AdminProfileScreen
import com.example.bursaryaid.feature.admin.ui.AdminReviewScreen
import com.example.bursaryaid.feature.admin.viewmodel.adminApplicationsViewModelFactory
import com.example.bursaryaid.feature.admin.viewmodel.adminBursaryOverviewViewModelFactory
import com.example.bursaryaid.feature.admin.viewmodel.adminCreateBursaryViewModelFactory
import com.example.bursaryaid.feature.admin.viewmodel.adminProfileViewModelFactory
import com.example.bursaryaid.feature.admin.viewmodel.adminReviewViewModelFactory

import com.example.bursaryaid.navigation.Routes
import com.example.bursaryaid.navigation.navigateToApplicationDetails
import com.example.bursaryaid.navigation.navigateToBursaryDetails
import com.example.bursaryaid.navigation.navigateToDashboard

import com.example.bursaryaid.repository.AllocationRecordRepository
import com.example.bursaryaid.repository.AppNotificationRepository
import com.example.bursaryaid.repository.BursaryApplicationRepository
import com.example.bursaryaid.repository.BursaryRepository
import com.example.bursaryaid.repository.StudentProfileRepository
import com.example.bursaryaid.repository.UserRepository

import com.example.bursaryaid.ui.theme.BursaryAidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            BursaryAidTheme(darkTheme = isDarkTheme) {
                BursaryAppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun BursaryAppNavigation(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {

    val context = LocalContext.current

    // Database
    val database = remember {
        AppDatabase.getDatabase(context)
    }

    // Repositories
    val userRepository = remember {
        UserRepository(database.userDao())
    }

    val profileRepo = remember {
        StudentProfileRepository(database.studentProfileDao())
    }

    val applicationRepo = remember {
        BursaryApplicationRepository(database.bursaryApplicationDao())
    }

    val bursaryRepo = remember {
        BursaryRepository(database.bursaryDao())
    }

    val allocationRepo = remember {
        AllocationRecordRepository(database.allocationRecordDao())
    }

    val notificationRepo = remember {
        AppNotificationRepository(database.appNotificationDao())
    }

    // Administrator review repository
    val adminReviewRepository = remember {
        RoomAdminReviewRepository(
            database = database,
            applicationRepository = applicationRepo,
            profileRepository = profileRepo,
            userRepository = userRepository,
            bursaryRepository = bursaryRepo,
            allocationRepository = allocationRepo,
            notificationRepository = notificationRepo
        )
    }

    val navController = rememberNavController()

    // Authentication ViewModel
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userRepository)
    )

    val currentUserId = authViewModel.currentUserId

    // Bursary browsing ViewModel
    val bursaryBrowseViewModel: BursaryBrowseViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                BursaryBrowseViewModel(bursaryRepo)
            }
        }
    )

    Scaffold { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {

            // LOGIN
            composable(Routes.LOGIN) {
                BursaryAidTheme(darkTheme = false) {
                    LoginScreen(
                        viewModel = authViewModel,

                        onLoginSuccess = { isAdmin ->
                            navController.navigateToDashboard(isAdmin)
                        },

                        onNavigateToRegister = {
                            navController.navigate(Routes.REGISTER)
                        }
                    )
                }
            }

            // REGISTRATION
            composable(Routes.REGISTER) {
                BursaryAidTheme(darkTheme = false) {
                    RegisterScreen(
                        viewModel = authViewModel,

                        onRegisterSuccess = {
                            navController.navigateToDashboard(authViewModel.isAdmin)
                        },

                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            // STUDENT DASHBOARD
            composable(Routes.STUDENT_DASHBOARD) {

                StudentDashboardScreen(
                    userName = authViewModel.currentUserName,
                    userEmail = authViewModel.currentUserEmail,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,

                    onMyApplications = {
                        navController.navigate(Routes.MY_APPLICATIONS)
                    },

                    onNewApplication = {
                        navController.navigate(Routes.BURSARY_LIST)
                    },

                    onProfile = {
                        navController.navigate(Routes.PROFILE)
                    },

                    onLogout = {
                        authViewModel.logoutUser()

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // ADMIN DASHBOARD
            composable(Routes.ADMIN_DASHBOARD) {

                AdminDashboardScreen(
                    userName = authViewModel.currentUserName,
                    userEmail = authViewModel.currentUserEmail,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,

                    onReviewApplications = {
                        navController.navigate(Routes.ADMIN_BURSARY_OVERVIEW)
                    },

                    onCreateBursary = {
                        navController.navigate(Routes.ADMIN_CREATE_BURSARY)
                    },

                    onProfile = {
                        navController.navigate(Routes.ADMIN_PROFILE)
                    },

                    onLogout = {
                        authViewModel.logoutUser()

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // ADMIN CREATE BURSARY
            composable(Routes.ADMIN_CREATE_BURSARY) {

                val adminCreateViewModel =
                    viewModel<com.example.bursaryaid.feature.admin.viewmodel.AdminCreateBursaryViewModel>(
                        factory = adminCreateBursaryViewModelFactory(
                            bursaryRepo
                        )
                    )

                AdminCreateBursaryScreen(
                    viewModel = adminCreateViewModel,
                    onBursaryCreated = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ADMIN BURSARY OVERVIEW: all counts are observed from Room.
            composable(Routes.ADMIN_BURSARY_OVERVIEW) {
                val overviewViewModel =
                    viewModel<com.example.bursaryaid.feature.admin.viewmodel.AdminBursaryOverviewViewModel>(
                        factory = adminBursaryOverviewViewModelFactory(
                            bursaryRepo,
                            applicationRepo
                        )
                    )

                AdminBursaryOverviewScreen(
                    viewModel = overviewViewModel,
                    onSelectBursary = { bursaryId ->
                        navController.navigate(Routes.adminBursaryApplicants(bursaryId))
                    },
                    onCreateBursary = {
                        navController.navigate(Routes.ADMIN_CREATE_BURSARY)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // APPLICATIONS FOR A SELECTED BURSARY.
            composable(
                route = Routes.ADMIN_BURSARY_APPLICANTS_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_BURSARY_ID) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val bursaryId =
                    backStackEntry.arguments?.getLong(Routes.ARG_BURSARY_ID)
                        ?: return@composable

                val applicantsViewModel =
                    viewModel<com.example.bursaryaid.feature.admin.viewmodel.AdminApplicationsViewModel>(
                        key = "admin-bursary-applicants-$bursaryId",
                        factory = adminApplicationsViewModelFactory(
                            adminReviewRepository,
                            bursaryId
                        )
                    )

                AdminApplicationsScreen(
                    viewModel = applicantsViewModel,
                    onOpenApplication = { applicationId ->
                        navController.navigate(Routes.adminReview(applicationId))
                    },
                    title = "Bursary Applicants",
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ADMIN PROFILE
            composable(Routes.ADMIN_PROFILE) {

                val adminProfileViewModel =
                    viewModel<com.example.bursaryaid.feature.admin.viewmodel.AdminProfileViewModel>(
                        factory = adminProfileViewModelFactory(
                            currentUserId,
                            userRepository
                        )
                    )

                AdminProfileScreen(
                    viewModel = adminProfileViewModel,
                    onProfileSaved = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ADMIN APPLICATION LIST
            composable(Routes.ADMIN_APPLICATIONS) {

                val adminApplicationsViewModel =
                    viewModel<com.example.bursaryaid.feature.admin.viewmodel.AdminApplicationsViewModel>(
                        factory = adminApplicationsViewModelFactory(
                            adminReviewRepository
                        )
                    )

                AdminApplicationsScreen(
                    viewModel = adminApplicationsViewModel,

                    onOpenApplication = { applicationId ->
                        navController.navigate(
                            Routes.adminReview(applicationId)
                        )
                    }
                )
            }

            // ADMIN APPLICATION REVIEW
            composable(
                route = Routes.ADMIN_REVIEW_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_APP_ID) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val applicationId =
                    backStackEntry.arguments?.getLong(Routes.ARG_APP_ID)
                        ?: return@composable

                val adminReviewViewModel =
                    viewModel<com.example.bursaryaid.feature.admin.viewmodel.AdminReviewViewModel>(
                        key = "admin-review-$applicationId",

                        factory = adminReviewViewModelFactory(
                            selectedApplicationId = applicationId,
                            reviewedByUserId = currentUserId,
                            adminReviewRepository = adminReviewRepository
                        )
                    )

                AdminReviewScreen(
                    viewModel = adminReviewViewModel,

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // BURSARY LIST
            composable(Routes.BURSARY_LIST) {

                BursaryListScreen(
                    viewModel = bursaryBrowseViewModel,

                    onSelectBursary = { id ->
                        navController.navigateToBursaryDetails(id)
                    },

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // BURSARY DETAILS
            composable(
                route = Routes.BURSARY_DETAILS_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_BURSARY_ID) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val bursaryId =
                    backStackEntry.arguments?.getLong(Routes.ARG_BURSARY_ID)
                        ?: return@composable

                BursaryDetailsScreen(
                    viewModel = bursaryBrowseViewModel,
                    bursaryId = bursaryId,

                    onApply = { id ->
                        navController.navigate(
                            Routes.form(bursaryId = id)
                        )
                    },

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // MY APPLICATIONS
            composable(Routes.MY_APPLICATIONS) {

                val myApplicationsViewModel: MyApplicationsViewModel = viewModel(
                    key = "my-applications-$currentUserId",

                    factory = myApplicationsViewModelFactory(
                        userId = currentUserId,
                        studentProfileRepository = profileRepo,
                        bursaryApplicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo
                    )
                )

                MyApplicationsScreen(
                    viewModel = myApplicationsViewModel,

                    onNewApplication = {
                        navController.navigate(Routes.BURSARY_LIST)
                    },

                    onApplicationClick = { appId ->
                        navController.navigateToApplicationDetails(appId)
                    },

                    onCompleteProfile = {
                        navController.navigate(Routes.PROFILE)
                    },

                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // STUDENT PROFILE
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

                    onProfileSaved = {
                        navController.navigate(Routes.STUDENT_DASHBOARD) {
                            popUpTo(Routes.STUDENT_DASHBOARD) {
                                inclusive = true
                            }
                        }
                    },

                    onNavigateBack = {
                        navController.navigate(Routes.STUDENT_DASHBOARD) {
                            popUpTo(Routes.STUDENT_DASHBOARD) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // APPLICATION FORM: CREATE OR EDIT
            composable(
                route = Routes.FORM_PATTERN,

                arguments = listOf(
                    navArgument(Routes.ARG_BURSARY_ID) {
                        type = NavType.LongType
                        defaultValue = 1L
                    },

                    navArgument(Routes.ARG_APP_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->

                val bursaryId =
                    backStackEntry.arguments?.getLong(Routes.ARG_BURSARY_ID) ?: 1L

                val appIdString =
                    backStackEntry.arguments?.getString(Routes.ARG_APP_ID)

                val selectedAppId =
                    appIdString?.toLongOrNull()

                val formViewModel: ApplicationFormViewModel = viewModel(
                    key = "form-$currentUserId-$bursaryId-$selectedAppId",

                    factory = applicationFormViewModelFactory(
                        userId = currentUserId,
                        selectedApplicationId = selectedAppId,
                        studentProfileRepository = profileRepo,
                        bursaryApplicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo,
                        onApplicationSubmitted = {}
                    )
                )

                ApplicationFormScreen(
                    viewModel = formViewModel,

                    onApplicationSaved = {
                        navController.popBackStack()
                    },

                    onCompleteProfile = {
                        navController.navigate(Routes.PROFILE)
                    },

                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // APPLICATION DETAILS
            composable(
                route = Routes.DETAILS_PATTERN,

                arguments = listOf(
                    navArgument(Routes.ARG_APP_ID) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->

                val appId =
                    backStackEntry.arguments?.getLong(Routes.ARG_APP_ID)
                        ?: return@composable

                val detailsViewModel: ApplicationDetailsViewModel = viewModel(
                    key = "details-$currentUserId-$appId",

                    factory = applicationDetailsViewModelFactory(
                        userId = currentUserId,
                        selectedApplicationId = appId,
                        studentProfileRepository = profileRepo,
                        bursaryApplicationRepository = applicationRepo,
                        bursaryRepository = bursaryRepo
                    )
                )

                ApplicationDetailsScreen(
                    viewModel = detailsViewModel,

                    onEditApplication = { editId, bursaryId ->
                        navController.navigate(
                            Routes.form(
                                bursaryId = bursaryId,
                                appId = editId
                            )
                        )
                    },

                    onApplicationDeleted = {
                        navController.popBackStack()
                    },

                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
