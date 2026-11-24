package com.example.bursaryaid.feature.student.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bursaryaid.model.AppConstants
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.ui.theme.StatusApprovedBg
import com.example.bursaryaid.ui.theme.StatusApprovedText
import com.example.bursaryaid.ui.theme.StatusDraftBg
import com.example.bursaryaid.ui.theme.StatusDraftText
import com.example.bursaryaid.ui.theme.StatusNeedsInfoBg
import com.example.bursaryaid.ui.theme.StatusNeedsInfoText
import com.example.bursaryaid.ui.theme.StatusRejectedBg
import com.example.bursaryaid.ui.theme.StatusRejectedText
import com.example.bursaryaid.ui.theme.StatusSubmittedBg
import com.example.bursaryaid.ui.theme.StatusSubmittedText
import com.example.bursaryaid.ui.theme.StatusUnderReviewBg
import com.example.bursaryaid.ui.theme.StatusUnderReviewText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Member 1 owns the app-wide reusable components; these are the few extras Member 2's screens need. */

@Composable
fun StudentLoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun StudentMessageBanner(message: String, isError: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Info else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun StudentFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    helperText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: helperText)?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StudentStatusChip(applicationStatus: ApplicationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = getStatusStyle(applicationStatus)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = when (applicationStatus) {
                    ApplicationStatus.SUBMITTED,
                    ApplicationStatus.UNDER_REVIEW,
                    ApplicationStatus.NEEDS_INFORMATION -> "Pending"
                    else -> applicationStatus.displayName()
                },
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

private fun getStatusStyle(status: ApplicationStatus): Triple<Color, Color, ImageVector> {
    return when (status) {
        ApplicationStatus.APPROVED -> Triple(StatusApprovedBg, StatusApprovedText, Icons.Default.CheckCircle)
        ApplicationStatus.REJECTED -> Triple(StatusRejectedBg, StatusRejectedText, Icons.Default.Info)
        ApplicationStatus.NEEDS_INFORMATION -> Triple(StatusNeedsInfoBg, StatusNeedsInfoText, Icons.Default.Info)
        ApplicationStatus.UNDER_REVIEW -> Triple(StatusUnderReviewBg, StatusUnderReviewText, Icons.Default.Search)
        ApplicationStatus.SUBMITTED -> Triple(StatusSubmittedBg, StatusSubmittedText, Icons.Default.Send)
        ApplicationStatus.DRAFT -> Triple(StatusDraftBg, StatusDraftText, Icons.Default.Edit)
    }
}

fun ApplicationStatus.displayName(): String = when (this) {
    ApplicationStatus.APPROVED -> "Accepted"
    else -> name.lowercase(Locale.ROOT).replace('_', ' ').replaceFirstChar { it.uppercase() }
}

fun formatKes(amountKes: Long): String = "${AppConstants.CURRENCY_CODE} ${"%,d".format(Locale.US, amountKes)}"

fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(epochMillis))