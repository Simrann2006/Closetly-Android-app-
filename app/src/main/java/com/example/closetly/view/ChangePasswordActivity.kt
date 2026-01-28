package com.example.closetly.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.R
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                ChangePasswordBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordBody() {
    val context = LocalContext.current
    val activity = context as Activity

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var currentError by remember { mutableStateOf(false) }
    var newError by remember { mutableStateOf(false) }
    var confirmError by remember { mutableStateOf(false) }

    var currentErrorMessage by remember { mutableStateOf("") }
    var newErrorMessage by remember { mutableStateOf("") }
    var confirmErrorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = if (ThemeManager.isDarkMode) White else Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
                )
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
    ) { paddingValues ->
        val currentUser = FirebaseAuth.getInstance().currentUser
        val isGoogleUser = currentUser?.providerData?.any { it.providerId == "google.com" } == true

        if (isGoogleUser) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = "Google",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Google Account",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "You signed in with Google.\nTo change your password, please visit your Google Account settings.",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 14.sp,
                        color = if (ThemeManager.isDarkMode)
                            OnBackground_Dark.copy(alpha = 0.7f)
                        else
                            Grey,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { activity.finish() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Brown, Light_brown)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Go Back",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                Image(
                    painter = painterResource(R.drawable.changepass),
                    contentDescription = "Change Password",
                    modifier = Modifier.size(80.dp),
                    alignment = Alignment.Center
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Change Password",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Create a strong password to keep\nyour account secure",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 14.sp,
                        color = if (ThemeManager.isDarkMode)
                            OnBackground_Dark.copy(alpha = 0.7f)
                        else
                            Grey,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(Modifier.height(40.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PasswordFieldWithLabel(
                        label = "Current Password",
                        placeholder = "Enter your current password",
                        value = currentPassword,
                        isVisible = currentVisible,
                        isError = currentError,
                        errorMessage = currentErrorMessage,
                        testTag = "current_password_input",
                        onValueChange = {
                            currentPassword = it
                            if (currentError) {
                                currentError = false
                                currentErrorMessage = ""
                            }
                        },
                        onVisibilityChange = { currentVisible = !currentVisible }
                    )

                    PasswordFieldWithLabel(
                        label = "New Password",
                        placeholder = "Enter new password",
                        value = newPassword,
                        isVisible = newVisible,
                        isError = newError,
                        errorMessage = newErrorMessage,
                        testTag = "new_password_input",
                        onValueChange = {
                            newPassword = it
                            if (newError) {
                                newError = false
                                newErrorMessage = ""
                            }
                        },
                        onVisibilityChange = { newVisible = !newVisible }
                    )

                    PasswordFieldWithLabel(
                        label = "Confirm New Password",
                        placeholder = "Re-enter new password",
                        value = confirmPassword,
                        isVisible = confirmVisible,
                        isError = confirmError,
                        errorMessage = confirmErrorMessage,
                        testTag = "confirm_password_input",
                        onValueChange = {
                            confirmPassword = it
                            if (confirmError) {
                                confirmError = false
                                confirmErrorMessage = ""
                            }
                        },
                        onVisibilityChange = { confirmVisible = !confirmVisible }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            "Forgot Password?",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brown
                            ),
                            modifier = Modifier.clickable {
                                val intent = Intent(context, ForgotActivity::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    var isLoading by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            currentError = false
                            newError = false
                            confirmError = false

                            when {
                                currentPassword.isBlank() -> {
                                    currentError = true
                                    currentErrorMessage = "Please enter current password"
                                }
                                newPassword.isBlank() -> {
                                    newError = true
                                    newErrorMessage = "Please enter new password"
                                }
                                newPassword.length < 6 -> {
                                    newError = true
                                    newErrorMessage = "Password must be at least 6 characters"
                                }
                                confirmPassword.isBlank() -> {
                                    confirmError = true
                                    confirmErrorMessage = "Please confirm your password"
                                }
                                newPassword != confirmPassword -> {
                                    confirmError = true
                                    confirmErrorMessage = "Passwords do not match"
                                }
                                currentPassword == newPassword -> {
                                    newError = true
                                    newErrorMessage = "New password must be different"
                                }
                                else -> {
                                    isLoading = true
                                    val user = FirebaseAuth.getInstance().currentUser
                                    val email = user?.email

                                    if (user != null && email != null) {
                                        val credential = EmailAuthProvider.getCredential(email, currentPassword)

                                        user.reauthenticate(credential)
                                            .addOnSuccessListener {
                                                user.updatePassword(newPassword)
                                                    .addOnSuccessListener {
                                                        isLoading = false
                                                        Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                                        activity.finish()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                currentError = true
                                                currentErrorMessage = "Current password is incorrect"
                                            }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "User not found. Please login again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                            .testTag("change_password_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Brown, Light_brown)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Save Password",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (ThemeManager.isDarkMode)
                                    Light_brown.copy(alpha = 0.1f)
                                else
                                    Light_brown.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_info_24),
                                contentDescription = null,
                                tint = Brown,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    "Password Requirements",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (ThemeManager.isDarkMode)
                                            OnBackground_Dark
                                        else
                                            DarkGrey
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "• At least 6 characters\n• Different from current password\n• Include a mix of letters and numbers",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 11.sp,
                                        color = if (ThemeManager.isDarkMode)
                                            OnBackground_Dark.copy(alpha = 0.7f)
                                        else
                                            Grey,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun PasswordFieldWithLabel(
    label: String,
    placeholder: String,
    value: String,
    isVisible: Boolean,
    isError: Boolean,
    errorMessage: String,
    testTag: String = "",
    onValueChange: (String) -> Unit,
    onVisibilityChange: () -> Unit
) {
    Column {
        Text(
            label,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            visualTransformation = if (isVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_lock_24),
                    contentDescription = null,
                    tint = if (isError)
                        (if (ThemeManager.isDarkMode) Error_Dark else Error_Light)
                    else
                        Light_brown,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (isVisible)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff,
                        contentDescription = if (isVisible) "Hide password" else "Show password",
                        tint = if (isError)
                            (if (ThemeManager.isDarkMode) Error_Dark else Error_Light)
                        else
                            Light_brown
                    )
                }
            },
            placeholder = {
                Text(
                    placeholder,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 14.sp,
                        color = if (ThemeManager.isDarkMode)
                            OnBackground_Dark.copy(alpha = 0.4f)
                        else
                            Grey.copy(alpha = 0.6f)
                    )
                )
            },
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (ThemeManager.isDarkMode)
                    Surface_Dark
                else
                    White,
                unfocusedContainerColor = if (ThemeManager.isDarkMode)
                    Surface_Dark
                else
                    White,
                focusedBorderColor = Brown,
                unfocusedBorderColor = if (ThemeManager.isDarkMode)
                    Grey.copy(alpha = 0.3f)
                else
                    Light_grey1,
                errorBorderColor = if (ThemeManager.isDarkMode) Error_Dark else Error_Light,
                focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
            ),
            textStyle = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 14.sp
            )
        )

        if (isError && errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_error_24),
                    contentDescription = null,
                    tint = if (ThemeManager.isDarkMode) Error_Dark else Error_Light,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    errorMessage,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 12.sp,
                        color = if (ThemeManager.isDarkMode) Error_Dark else Error_Light
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun ChangePasswordPreview() {
    ChangePasswordBody()
}