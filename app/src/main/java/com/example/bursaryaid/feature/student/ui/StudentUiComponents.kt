package com.example.bursaryaid.feature.student.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.data.model.AppConstants
import com.example.bursaryaid.data.model.ApplicationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Member 1 owns the app-wide reusable components; these are the few extras Member 2's screens need. */

@Composable
fun StudentLoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun StudentMessageBanner(message: String, isError: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
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
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StudentStatusChip(applicationStatus: ApplicationStatus, modifier: Modifier = Modifier) {
    AssistChip(onClick = {}, label = { Text(applicationStatus.displayName()) }, modifier = modifier)
}

fun ApplicationStatus.displayName(): String =
    name.lowercase(Locale.ROOT).replace('_', ' ').replaceFirstChar { it.uppercase() }

fun formatKes(amountKes: Long): String = "${AppConstants.CURRENCY_CODE} ${"%,d".format(Locale.US, amountKes)}"

fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(epochMillis))
