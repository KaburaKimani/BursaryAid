package com.example.bursaryaid.feature.auth.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.bursaryaid.ui.theme.DarkPurpleBg
import com.example.bursaryaid.ui.theme.DarkPurpleSurface
import com.example.bursaryaid.ui.theme.LightLavender
import com.example.bursaryaid.ui.theme.PrimaryPurple

// Graduation cap header icon
private val GraduationCapIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "GraduationCap",
        defaultWidth = 56.dp,
        defaultHeight = 56.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(12f, 3f)
        lineTo(1f, 9f)
        lineToRelative(11f, 6f)
        lineToRelative(9f, -4.91f)
        lineTo(21f, 17f)
        lineToRelative(2f, 0f)
        lineTo(23f, 9f)
        close()
        moveTo(5f, 13.18f)
        lineTo(5f, 17f)
        curveToRelative(0f, 2.21f, 3.13f, 4f, 7f, 4f)
        curveToRelative(3.87f, 0f, 7f, -1.79f, 7f, -4f)
        lineToRelative(0f, -3.82f)
        lineToRelative(-7f, 3.82f)
        close()
    }.build()
}

// Applications list icon
private val ApplicationsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Applications",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(19f, 3f)
        lineTo(5f, 3f)
        curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
        lineToRelative(0f, 14f)
        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
        lineToRelative(14f, 0f)
        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
        lineTo(21f, 5f)
        curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
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

// New Application icon
private val NewApplicationIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "NewApplication",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(19f, 13f)
        lineToRelative(-6f, 0f)
        lineToRelative(0f, 6f)
        lineToRelative(-2f, 0f)
        lineToRelative(0f, -6f)
        lineToRelative(-6f, 0f)
        lineToRelative(0f, -2f)
        lineToRelative(6f, 0f)
        lineToRelative(0f, -6f)
        lineToRelative(2f, 0f)
        lineToRelative(0f, 6f)
        lineToRelative(6f, 0f)
        close()
    }.build()
}

// User Profile icon
private val ProfileIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Profile",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(12f, 12f)
        curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f)
        curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f)
        curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f)
        curveToRelative(0f, 2.21f, 1.79f, 4f, 4f, 4f)
        close()
        moveTo(12f, 14f)
        curveToRelative(-2.67f, 0f, -8f, 1.34f, -8f, 4f)
        lineToRelative(0f, 2f)
        lineToRelative(16f, 0f)
        lineToRelative(0f, -2f)
        curveToRelative(0f, -2.66f, -5.33f, -4f, -8f, -4f)
        close()
    }.build()
}

@Composable
fun StudentDashboardScreen(
    onMyApplications: () -> Unit,
    onNewApplication: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Soft vertical gradient background matching LoginScreen
    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(DarkPurpleBg, DarkPurpleSurface))
    } else {
        Brush.verticalGradient(listOf(LightLavender, Color.White))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header icon & titles
            Icon(
                imageVector = GraduationCapIcon,
                contentDescription = "Dashboard Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Student Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Apply for bursaries and track your applications",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Action Card 1: My Applications
            DashboardActionCard(
                title = "My applications",
                description = "View and manage your submitted or draft applications",
                icon = ApplicationsIcon,
                onClick = onMyApplications
            )

            Spacer(Modifier.height(14.dp))

            // Action Card 2: New Application
            DashboardActionCard(
                title = "New application",
                description = "Explore open bursary opportunities and apply",
                icon = NewApplicationIcon,
                onClick = onNewApplication
            )

            Spacer(Modifier.height(14.dp))

            // Action Card 3: My Profile
            DashboardActionCard(
                title = "My profile",
                description = "Update your education, household, and guardian details",
                icon = ProfileIcon,
                onClick = onProfile
            )

            Spacer(Modifier.height(24.dp))

            // Subtle Outlined Log Out button
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Log out",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
