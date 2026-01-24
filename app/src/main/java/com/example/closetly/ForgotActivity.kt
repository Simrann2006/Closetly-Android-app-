package com.example.closetly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.Background_Dark
import com.example.closetly.ui.theme.Background_Light
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.ui.theme.DarkGrey
import com.example.closetly.ui.theme.Error_Dark
import com.example.closetly.ui.theme.Error_Light
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey1
import com.example.closetly.ui.theme.OnBackground_Dark
import com.example.closetly.ui.theme.OnSurface_Dark
import com.example.closetly.ui.theme.OnSurface_Light
import com.example.closetly.ui.theme.Surface_Dark
import com.example.closetly.ui.theme.White
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.UserViewModel

class ForgotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                ForgotBody()
            }
        }
    }
}

@Composable
fun ForgotBody(){
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl(context)) }

    var email by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val activity = context as Activity
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (ThemeManager.isDarkMode) Background_Dark else Background_Light)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Image(
                painter = painterResource(R.drawable.forgot),
                contentDescription = "Password Reset",
                modifier = Modifier
                    .size(100.dp),
                alignment = Alignment.Center
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Forgot Password?",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                )
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "No worries! Enter your email and we'll send\nyou a link to reset your password",
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

            Spacer(Modifier.height(48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    "Email Address",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                OutlinedTextField(
                    value = email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    onValueChange = { data ->
                        email = data
                        if (isError) {
                            isError = false
                            errorMessage = ""
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_mail_outline_24),
                            contentDescription = null,
                            tint = if (isError)
                                (if (ThemeManager.isDarkMode) Error_Dark else Error_Light)
                            else
                                Light_brown,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    placeholder = {
                        Text(
                            "Enter your email",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 14.sp
                            )
                        )
                    },
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                            painter = painterResource(R.drawable.baseline_mail_outline_24),
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

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        when {
                            email.isBlank() -> {
                                isError = true
                                errorMessage = "Please enter your email"
                            }
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                isError = true
                                errorMessage = "Invalid email format"
                            }
                            else -> {
                                userViewModel.forgotPassword(email) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        isError = true
                                        errorMessage = "Email not found"
                                    }
                                }
                            }
                        }
                    },
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
                            "Send Reset Link",
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

                Spacer(Modifier.height(24.dp))

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
                            painter = painterResource(R.drawable.baseline_mail_outline_24),
                            contentDescription = null,
                            tint = Brown,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            "Check your inbox for a password reset link. It may take a few minutes to arrive.",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 12.sp,
                                color = if (ThemeManager.isDarkMode)
                                    OnBackground_Dark.copy(alpha = 0.8f)
                                else
                                    DarkGrey,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Back to login? ",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = if (ThemeManager.isDarkMode)
                            OnBackground_Dark.copy(alpha = 0.7f)
                        else
                            Grey,
                        fontSize = 14.sp
                    )
                )
                TextButton(
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        activity.finish()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Sign In",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Brown,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Preview
@Composable
fun PreviewForgot(){
    ForgotBody()
}