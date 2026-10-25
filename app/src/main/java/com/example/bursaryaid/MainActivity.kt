package com.example.bursaryaid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bursaryaid.contract_stubs.InMemoryApplicationRepository
import com.example.bursaryaid.contract_stubs.InMemoryBursaryRepository
import com.example.bursaryaid.contract_stubs.InMemoryStudentProfileRepository
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

    val currentUserId = 101L

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "my_applications",
            modifier = Modifier.padding(innerPadding)
        ) {
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