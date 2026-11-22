package com.example.bursaryaid.feature.auth.ui

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.auth.viewmodel.AuthViewModel
import com.example.bursaryaid.ui.components.ErrorMessage
import com.example.bursaryaid.ui.components.SuccessMessage
import com.example.bursaryaid.ui.theme.DarkPurpleBg
import com.example.bursaryaid.ui.theme.DarkPurpleSurface
import com.example.bursaryaid.ui.theme.DisabledPurple
import com.example.bursaryaid.ui.theme.IndigoPurple
import com.example.bursaryaid.ui.theme.LightLavender
import com.example.bursaryaid.ui.theme.PrimaryPurple

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val PHONE_REGEX = Regex("^(?:\\+254|0)[17]\\d{8}$")

// Graduation Cap Icon
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

// Person Icon
private val PersonIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Person",
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

// Email Icon
private val EmailIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Email",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(20f, 4f)
        lineTo(4f, 4f)
        curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f)
        lineTo(2f, 18f)
        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
        lineToRelative(16f, 0f)
        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
        lineTo(22f, 6f)
        curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
        close()
        moveTo(20f, 8f)
        lineToRelative(-8f, 5f)
        lineToRelative(-8f, -5f)
        lineTo(4f, 6f)
        lineToRelative(8f, 5f)
        lineToRelative(8f, -5f)
        close()
    }.build()
}

// Phone Icon
private val PhoneIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Phone",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(6.62f, 10.79f)
        curveToRelative(1.44f, 2.83f, 3.76f, 5.14f, 6.59f, 6.59f)
        lineToRelative(2.2f, -2.2f)
        curveToRelative(0.27f, -0.27f, 0.67f, -0.36f, 1.02f, -0.24f)
        curveToRelative(1.12f, 0.37f, 2.33f, 0.57f, 3.57f, 0.57f)
        curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
        lineToRelative(0f, 3.5f)
        curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
        curveToRelative(-9.39f, 0f, -17f, -7.61f, -17f, -17f)
        curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
        lineToRelative(3.5f, 0f)
        curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
        curveToRelative(0f, 1.25f, 0.2f, 2.45f, 0.57f, 3.57f)
        curveToRelative(0.11f, 0.35f, 0.03f, 0.74f, -0.25f, 1.02f)
        lineToRelative(-2.2f, 2.2f)
        close()
    }.build()
}

// Lock Icon
private val LockIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Lock",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(PrimaryPurple)) {
        moveTo(18f, 8f)
        lineTo(17f, 8f)
        lineTo(17f, 6f)
        curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
        curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
        lineToRelative(0f, 2f)
        lineTo(6f, 8f)
        curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
        lineToRelative(0f, 10f)
        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
        lineToRelative(12f, 0f)
        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
        lineTo(20f, 10f)
        curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
        close()
        moveTo(12f, 17f)
        curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
        curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
        curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
        curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
        close()
        moveTo(15.1f, 8f)
        lineTo(8.9f, 8f)
        lineTo(8.9f, 6f)
        curveToRelative(0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
        curveToRelative(1.71f, 0f, 3.1f, 1.39f, 3.1f, 3.1f)
        close()
    }.build()
}

// Eye Icon
private val EyeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Eye",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color(0xFF79747E))) {
        moveTo(12f, 4.5f)
        curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
        curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f)
        curveToRelative(5f, 0f, 9.27f, -3.11f, 11f, -7.5f)
        curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f)
        close()
        moveTo(12f, 17f)
        curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
        curveToRelative(0f, -2.76f, 2.24f, -5f, 5f, -5f)
        curveToRelative(2.76f, 0f, 5f, 2.24f, 5f, 5f)
        curveToRelative(0f, 2.76f, -2.24f, 5f, -5f, 5f)
        close()
        moveTo(12f, 9f)
        curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
        curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
        curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
        curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
        close()
    }.build()
}

// EyeOff Icon
private val EyeOffIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "EyeOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color(0xFF79747E))) {
        moveTo(12f, 7f)
        curveToRelative(2.76f, 0f, 5f, 2.24f, 5f, 5f)
        curveToRelative(0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f)
        lineToRelative(2.92f, 2.92f)
        curveTo(21.03f, 15.22f, 22.22f, 13.72f, 23f, 12f)
        curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f)
        curveToRelative(-1.4f, 0f, -2.74f, 0.25f, -3.98f, 0.7f)
        lineToRelative(2.16f, 2.16f)
        curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
        close()
        moveTo(2f, 4.27f)
        lineToRelative(2.28f, 2.28f)
        lineToRelative(0.46f, 0.46f)
        curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
        curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f)
        curveToRelative(1.55f, 0f, 3.03f, -0.3f, 4.38f, -0.84f)
        lineToRelative(0.42f, 0.42f)
        lineTo(19.73f, 22f)
        lineTo(21f, 20.73f)
        lineTo(3.27f, 3f)
        lineTo(2f, 4.27f)
        close()
        moveTo(7.53f, 9.8f)
        lineToRelative(1.55f, 1.55f)
        curveTo(9.03f, 11.56f, 9f, 11.77f, 9f, 12f)
        curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
        curveToRelative(0.23f, 0f, 0.44f, -0.03f, 0.65f, -0.08f)
        lineToRelative(1.55f, 1.55f)
        curveTo(13.42f, 16.85f, 12.73f, 17f, 12f, 17f)
        curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
        curveToRelative(0f, -0.73f, 0.15f, -1.42f, 0.53f, -2.2f)
        close()
        moveTo(11.84f, 9.02f)
        lineToRelative(3.15f, 3.15f)
        lineToRelative(0.02f, -0.17f)
        curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
        lineToRelative(-0.17f, 0.02f)
        close()
    }.build()
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var fullNameTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) { viewModel.clearMessages() }

    // Inline validation rules
    val isFullNameValid = fullName.isNotBlank()
    val fullNameError = if (fullNameTouched && !isFullNameValid) "Full name is required" else null

    val isEmailValid = email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    val emailError = if (emailTouched && !isEmailValid) "Enter a valid email address" else null

    val isPhoneValid = phoneNumber.isNotBlank() && PHONE_REGEX.matches(phoneNumber.trim().replace(" ", ""))
    val phoneError = if (phoneTouched && !isPhoneValid) "Enter a valid phone number, e.g. 0712345678" else null

    val isPasswordValid = password.length >= 6
    val passwordError = if (passwordTouched && !isPasswordValid) "Password must be at least 6 characters" else null

    val isConfirmPasswordValid = confirmPassword.isNotEmpty() && confirmPassword == password
    val confirmPasswordError = if (confirmPasswordTouched && !isConfirmPasswordValid) {
        if (confirmPassword.isEmpty()) "Confirm password is required" else "Passwords do not match"
    } else null

    val isFormValid = isFullNameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid

    // Button press scale animation
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "register_button_scale"
    )

    // Soft vertical gradient background matching LoginScreen
    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(DarkPurpleBg, DarkPurpleSurface))
    } else {
        Brush.verticalGradient(listOf(LightLavender, Color.White))
    }

    // Gradient or faded purple button background
    val buttonBrush = if (isFormValid && !viewModel.isLoading) {
        Brush.horizontalGradient(listOf(PrimaryPurple, IndigoPurple))
    } else {
        SolidColor(DisabledPurple.copy(alpha = 0.5f))
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
            // Header Graduation Cap Logo
            Icon(
                imageVector = GraduationCapIcon,
                contentDescription = "Bursary Aid Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(8.dp))

            // Bold Primary Purple title
            Text(
                text = "Create account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sign up for Bursary Aid",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            SuccessMessage(viewModel.successMessage)

            // Rounded Card container (16dp corners, 6dp elevation)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) DarkPurpleSurface else Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameTouched = true
                            viewModel.clearMessages()
                        },
                        label = { Text("Full name") },
                        singleLine = true,
                        isError = fullNameError != null,
                        supportingText = fullNameError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(
                                imageVector = PersonIcon,
                                contentDescription = "Person icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailTouched = true
                            viewModel.clearMessages()
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(
                                imageVector = EmailIcon,
                                contentDescription = "Email icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Phone Number Field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneTouched = true
                            viewModel.clearMessages()
                        },
                        label = { Text("Phone number") },
                        singleLine = true,
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(
                                imageVector = PhoneIcon,
                                contentDescription = "Phone icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordTouched = true
                            viewModel.clearMessages()
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = LockIcon,
                                contentDescription = "Lock icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) EyeIcon else EyeOffIcon,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordTouched = true
                            viewModel.clearMessages()
                        },
                        label = { Text("Confirm password") },
                        singleLine = true,
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it) } },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = LockIcon,
                                contentDescription = "Lock icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) EyeIcon else EyeOffIcon,
                                    contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isFormValid && !viewModel.isLoading) {
                                    focusManager.clearFocus()
                                    viewModel.registerUser(
                                        fullName, email, phoneNumber, password, confirmPassword, onRegisterSuccess
                                    )
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    ErrorMessage(viewModel.errorMessage)

                    Spacer(Modifier.height(16.dp))

                    // Full-width Gradient Register Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .background(buttonBrush, shape = RoundedCornerShape(12.dp))
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.registerUser(
                                    fullName, email, phoneNumber, password, confirmPassword, onRegisterSuccess
                                )
                            },
                            enabled = isFormValid && !viewModel.isLoading,
                            interactionSource = buttonInteractionSource,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Register",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFormValid && !viewModel.isLoading) Color.White else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // "Already have an account? Log in" with "Log in" bold in primary purple
            TextButton(onClick = {
                viewModel.clearMessages()
                onNavigateToLogin()
            }) {
                val annotatedString = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Log in")
                    }
                }
                Text(text = annotatedString, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
