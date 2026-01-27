package com.example.closetly.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.utils.NotificationHelper
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.UserViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CustomCredential
import com.example.closetly.R
import com.example.closetly.ui.theme.Background_Dark
import com.example.closetly.ui.theme.Background_Light
import com.example.closetly.ui.theme.Brown
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHelper.createNotificationChannels(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                LoginBody(
                    requestNotificationPermission = {
                        requestNotificationPermission()
                    }
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationHelper.createNotificationChannels(this)
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationHelper.createNotificationChannels(this)
        }
    }
}

@Composable
fun LoginBody(requestNotificationPermission: () -> Unit = {}){

    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl(context)) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }

    val activity = context as Activity

    val scrollState = rememberScrollState()

    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val sharedPreferences = context.getSharedPreferences("ClosetlyPrefs", Context.MODE_PRIVATE)
    LaunchedEffect(Unit) {
        val savedEmail = sharedPreferences.getString("email", "")?: ""
        val savedPassword = sharedPreferences.getString("password", "")?:""
        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)

        if (rememberMe && savedEmail.isNotEmpty() && savedPassword.isNotEmpty()) {
            email = savedEmail
            password = savedPassword
            terms = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (ThemeManager.isDarkMode) Background_Dark else Background_Light
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .offset(x = (-50).dp, y = (-100).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Brown.copy(alpha = 0.3f),
                                    Light_brown.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = 30.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Light_brown.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(16.dp, RoundedCornerShape(50)),
                        shape = RoundedCornerShape(50),
                        color = White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Brown, Light_brown)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_checkroom_24),
                                contentDescription = "Closetly Logo",
                                modifier = Modifier.size(48.dp),
                                tint = White
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Closetly",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Your Personalized Digital Wardrobe & Style Assistant",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = if (ThemeManager.isDarkMode)
                                OnBackground_Dark.copy(alpha = 0.7f)
                            else
                                Grey
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column {
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
                                tint = if (isError) Error_Light else Light_brown,
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
                        modifier = Modifier.fillMaxWidth().testTag("email_input"),
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
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))

                Column {
                    Text(
                        "Password",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { data ->
                            password = data
                            if (isError) {
                                isError = false
                                errorMessage = ""
                            }
                        },
                        visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { visibility = !visibility }) {
                                Icon(
                                    painter = if (visibility)
                                        painterResource(R.drawable.outline_visibility_off_24) else
                                        painterResource(R.drawable.outline_visibility_24),
                                    contentDescription = null,
                                    tint = Light_brown,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_lock_24),
                                contentDescription = null,
                                tint = if (isError) Error_Light else Light_brown,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                "Enter your password",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp
                                )
                            )
                        },
                        isError = isError,
                        modifier = Modifier.fillMaxWidth().testTag("password_input"),
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
                        )
                    )
                }

                if (isError && errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_mail_outline_24),
                            contentDescription = null,
                            tint = Error_Light,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            errorMessage,
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 12.sp,
                                color = Error_Light
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = terms,
                            onCheckedChange = { terms = it },
                            modifier = Modifier.size(18.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = Light_brown,
                                checkmarkColor = White
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Remember me",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                color = if (ThemeManager.isDarkMode) OnSurface_Dark else DarkGrey
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            val intent = Intent(context, ForgotActivity::class.java)
                            context.startActivity(intent)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Forgot Password?",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = Brown,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        when {
                            email.isBlank() || password.isBlank() -> {
                                isError = true
                                errorMessage = "Please fill all fields"
                            }
                            else -> {
                                userViewModel.login(email, password) { success, message ->
                                    if (success) {
                                        if (terms) {
                                            sharedPreferences.edit().apply() {
                                                putString("email", email)
                                                putString("password", password)
                                                putBoolean("rememberMe", true)
                                                apply()
                                            }
                                        } else {
                                            sharedPreferences.edit().clear().apply()
                                        }

                                        requestNotificationPermission()

                                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val token = task.result
                                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                if (userId != null && token != null) {
                                                    val userRef = FirebaseDatabase.getInstance()
                                                        .getReference("Users").child(userId)
                                                    userRef.child("fcmToken").setValue(token)
                                                }
                                            }
                                        }

                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, DashboardActivity::class.java)
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        isError = true
                                        errorMessage = "Invalid credentials"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp))
                        .testTag("login_button"),
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
                            "Sign In",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        Modifier.weight(1f),
                        thickness = 1.dp,
                        color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1
                    )
                    Text(
                        "  or continue with  ",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 12.sp,
                            color = Grey
                        )
                    )
                    HorizontalDivider(
                        Modifier.weight(1f),
                        thickness = 1.dp,
                        color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1
                    )
                }

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        val credentialManager = CredentialManager.create(context)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("233120554772-kh29l4pko3gl9e6k46kf29v5t6v39974.apps.googleusercontent.com")
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val result = credentialManager.getCredential(context, request)
                                val credential = result.credential

                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken

                                    userViewModel.signInWithGoogle(idToken) { success, message ->
                                        if (success) {
                                            requestNotificationPermission()

                                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val token = task.result
                                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                    if (userId != null && token != null) {
                                                        val userRef = FirebaseDatabase.getInstance()
                                                            .getReference("Users").child(userId)
                                                        userRef.child("fcmToken").setValue(token)
                                                    }
                                                }
                                            }
                                            val intent = Intent(context, DashboardActivity::class.java)
                                            context.startActivity(intent)
                                            activity.finish()
                                        } else {
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Google",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (ThemeManager.isDarkMode) OnSurface_Dark else DarkGrey
                            )
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Don't have an account? ",
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
                            val intent = Intent(context, RegistrationActivity::class.java)
                            context.startActivity(intent)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Sign Up",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = Brown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
@Preview
fun PreviewLogin() {
    LoginBody()
}