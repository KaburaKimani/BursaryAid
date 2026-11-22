package com.example.bursaryaid.feature.student.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.data.model.ApplicationStatus
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import com.example.bursaryaid.feature.student.viewmodel.MyApplicationsViewModel
import com.example.bursaryaid.ui.theme.DarkPurpleBg
import com.example.bursaryaid.ui.theme.DarkPurpleSurface
import com.example.bursaryaid.ui.theme.IndigoPurple
import com.example.bursaryaid.ui.theme.LightLavender
import com.example.bursaryaid.ui.theme.PrimaryPurple

// Assignment / Empty State Illustration Icon
private val AssignmentIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Assignment",
        defaultWidth = 64.dp,
        defaultHeight = 64.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(19f, 3f)
        lineTo(14.82f, 3f)
        curveTo(14.4f, 1.84f, 13.3f, 1f, 12f, 1f)
        curveTo(10.7f, 1f, 9.6f, 1.84f, 9.18f, 3f)
        lineTo(5f, 3f)
        curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
        lineToRelative(0f, 14f)
        curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
        lineToRelative(14f, 0f)
        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
        lineTo(21f, 5f)
        curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
        close()
        moveTo(12f, 3f)
        curveTo(12.55f, 3f, 13f, 3.45f, 13f, 4f)
        curveTo(13f, 4.55f, 12.55f, 5f, 12f, 5f)
        curveTo(11.45f, 5f, 11f, 4.55f, 11f, 4f)
        curveTo(11f, 3.45f, 11.45f, 3f, 12f, 3f)
        close()
        moveTo(14f, 17f)
        lineTo(7f, 17f)
        lineToRelative(0f, -2f)
        lineToRelative(7f, 0f)
        close()
        moveTo(17f, 13f)
        lineTo(7f, 13f)
        lineToRelative(0f, -2f)
        lineToRelative(10f, 0f)
        close()
        moveTo(17f, 9f)
        lineTo(7f, 9f)
        lineToRelative(0f, -2f)
        lineToRelative(10f, 0f)
        close()
    }.build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    viewModel: MyApplicationsViewModel,
    onNewApplication: () -> Unit,
    onApplicationClick: (applicationId: Long) -> Unit,
    onCompleteProfile: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var applicationPendingDeletion by remember { mutableStateOf<BursaryApplication?>(null) }
    val isDark = isSystemInDarkTheme()

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(DarkPurpleBg, DarkPurpleSurface))
    } else {
        Brush.verticalGradient(listOf(LightLavender, Color.White))
    }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        val message = state.successMessage ?: state.errorMessage.takeIf { state.applications.isNotEmpty() }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    applicationPendingDeletion?.let { application ->
        AlertDialog(
            onDismissRequest = { applicationPendingDeletion = null },
            title = { Text("Delete this draft?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteApplication(application.applicationId)
                    applicationPendingDeletion = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { applicationPendingDeletion = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My applications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) DarkPurpleSurface else LightLavender
                )
            )
        },
        floatingActionButton = {
            if (!state.isProfileMissing) {
                FloatingActionButton(
                    onClick = onNewApplication,
                    containerColor = PrimaryPurple,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New application")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(Modifier.fillMaxSize()) {
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

                when {
                    state.isProfileMissing -> CenteredMessage(
                        title = "Complete your student profile first",
                        body = "You need a profile before you can apply for a bursary.",
                        actionLabel = "Create profile",
                        onAction = onCompleteProfile
                    )

                    state.errorMessage != null && state.totalApplicationCount == 0 && !state.isLoading -> CenteredMessage(
                        title = "Something went wrong",
                        body = state.errorMessage.orEmpty(),
                        actionLabel = "Try again",
                        onAction = viewModel::loadStudentAndApplications
                    )

                    !state.isLoading && state.totalApplicationCount == 0 -> CenteredMessage(
                        title = "No applications yet",
                        body = "Start a bursary application and it will appear here.",
                        actionLabel = "New application",
                        onAction = onNewApplication
                    )

                    else -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = state.selectedFilter == null,
                                    onClick = { viewModel.onFilterSelected(null) },
                                    label = { Text("All") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryPurple,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(ApplicationStatus.entries) { status ->
                                FilterChip(
                                    selected = state.selectedFilter == status,
                                    onClick = { viewModel.onFilterSelected(status) },
                                    label = { Text(status.displayName()) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryPurple,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (state.applications.isEmpty() && !state.isLoading) {
                            CenteredMessage(
                                title = "Nothing with this status",
                                body = "Try a different filter.",
                                actionLabel = "Show all",
                                onAction = { viewModel.onFilterSelected(null) }
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.applications, key = { it.applicationId }) { application ->
                                    ApplicationCard(
                                        application = application,
                                        bursaryName = state.bursaryNames[application.bursaryId]
                                            ?: "Bursary #${application.bursaryId}",
                                        onClick = {
                                            viewModel.onApplicationSelected(application.applicationId)
                                            onApplicationClick(application.applicationId)
                                        },
                                        onDeleteClick = { applicationPendingDeletion = application }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    application: BursaryApplication,
    bursaryName: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkPurpleSurface else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = bursaryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${application.academicYear.ifBlank { "Academic year not set" }} • ${formatKes(application.requestedAmountKes)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = application.submittedAtMillis?.let { "Submitted ${formatDateTime(it)}" }
                        ?: "Not submitted • last edited ${formatDateTime(application.updatedAtMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                StudentStatusChip(application.applicationStatus)
            }
            if (ApplicationRules.canDelete(application.applicationStatus)) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete draft",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
internal fun CenteredMessage(title: String, body: String, actionLabel: String, onAction: () -> Unit) {
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon illustration above empty-state text
        Icon(
            imageVector = AssignmentIcon,
            contentDescription = null,
            tint = PrimaryPurple,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        // Styled Login-like gradient button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .background(
                    brush = Brush.horizontalGradient(listOf(PrimaryPurple, IndigoPurple)),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Button(
                onClick = onAction,
                interactionSource = buttonInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
