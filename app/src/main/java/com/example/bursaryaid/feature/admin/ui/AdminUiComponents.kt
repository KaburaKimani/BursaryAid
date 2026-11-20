package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.admin.logic.AdminApplicationItem
import com.example.bursaryaid.model.ApplicationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 25000 -> "KSh 25,000". Money is a whole-shilling Long everywhere in the admin module. */
fun formatKes(amountKes: Long): String = "KSh " + "%,d".format(Locale.US, amountKes)

/** UNDER_REVIEW -> "Under review". Built from the enum name so it never goes out of date. */
fun formatStatusLabel(status: ApplicationStatus): String =
    status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

/** A small coloured label showing an application's status. */
@Composable
fun StatusBadge(status: ApplicationStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (background, content) = when (status) {
        ApplicationStatus.APPROVED -> colors.primaryContainer to colors.onPrimaryContainer
        ApplicationStatus.REJECTED -> colors.errorContainer to colors.onErrorContainer
        else -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    Surface(color = background, contentColor = content, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            text = formatStatusLabel(status),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** One row of the admin list. Tapping it opens the review screen. */
@Composable
fun AdminApplicationCard(
    item: AdminApplicationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.studentName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                StatusBadge(item.applicationStatus)
            }
            Text(
                text = "${item.admissionNumber} | ${item.bursaryName}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Requested ${formatKes(item.requestedAmountKes)} | Submitted ${formatDate(item.submittedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}