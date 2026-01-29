package com.example.closetly.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.model.UserModel
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.White
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.UserViewModel
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CustomCredential
import com.example.closetly.R
import com.example.closetly.ui.theme.Background_Dark
import com.example.closetly.ui.theme.Background_Light
import com.example.closetly.ui.theme.DarkGrey
import com.example.closetly.ui.theme.Light_grey1
import com.example.closetly.ui.theme.OnBackground_Dark
import com.example.closetly.ui.theme.OnSurface_Dark
import com.example.closetly.ui.theme.OnSurface_Light
import com.example.closetly.ui.theme.Surface_Dark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                RegistrationBody()
            }
        }
    }
}

data class Country(val name: String, val code: String, val dialCode: String)

fun getCountryFlagEmoji(countryCode: String): String {
    return countryCode
        .uppercase()
        .map { char ->
            Character.codePointAt("$char", 0) - 0x41 + 0x1F1E6
        }
        .map { codePoint ->
            Character.toChars(codePoint)
        }
        .joinToString("") { String(it) }
}

@Composable
fun RegistrationBody() {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl(context)) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(TextFieldValue(text = "", selection = TextRange(0))) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var visibility1 by remember { mutableStateOf(false) }
    var visibility2 by remember { mutableStateOf(false) }


    val phoneUtil = PhoneNumberUtil.getInstance()
    val countries = remember {
        phoneUtil.supportedRegions
            .sorted()
            .map { regionCode ->
                val countryCode = phoneUtil.getCountryCodeForRegion(regionCode)
                val countryName = Locale("", regionCode).displayCountry
                Country(countryName, regionCode, "+$countryCode")
            }
    }

    val nepalCountry = countries.find { it.code == "NP" } ?: countries.first()
    var selectedCountry by remember { mutableStateOf(nepalCountry) }
    var expanded by remember { mutableStateOf(false) }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Light_brown,
                                    Brown,
                                    Brown.copy(alpha = 0.8f)
                                ),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    White.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                center = Offset(800f, 100f),
                                radius = 600f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Create Account",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Join us and start your wardrobe journey",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = White.copy(alpha = 0.9f)
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp)
            ) {
                Column {
                    Text(
                        "Full Name",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_person_24),
                                contentDescription = null,
                                tint = Light_brown,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                "Enter your full name",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fullname_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedBorderColor = Brown,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "Phone Number",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.clickable { expanded = true }
                        ) {
                            OutlinedTextField(
                                value = "${getCountryFlagEmoji(selectedCountry.code)} ${selectedCountry.dialCode}",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                        contentDescription = null,
                                        tint = Light_brown,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable { expanded = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                    disabledBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                                    disabledTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                                ),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .height(300.dp)
                                    .background(
                                        if (ThemeManager.isDarkMode) Surface_Dark else White,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(getCountryFlagEmoji(country.code), fontSize = 20.sp)
                                                Text(
                                                    "${country.name} (${country.dialCode})",
                                                    style = TextStyle(
                                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                                        fontSize = 13.sp,
                                                        color = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCountry = country
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { newValue ->
                                val filteredText = newValue.text.filter { it.isDigit() }
                                phoneNumber = newValue.copy(
                                    text = filteredText,
                                    selection = TextRange(filteredText.length)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            placeholder = {
                                Text(
                                    "Phone number",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("phone_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                focusedBorderColor = Brown,
                                unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                                focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                                unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                            ),
                            textStyle = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "Email Address",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        onValueChange = { email = it },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_mail_outline_24),
                                contentDescription = null,
                                tint = Light_brown,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedBorderColor = Brown,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "Create Password",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = if (visibility1) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { visibility1 = !visibility1 }) {
                                Icon(
                                    painter = if (visibility1)
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
                                tint = Light_brown,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                "Enter password",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedBorderColor = Brown,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "Confirm Password",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        visualTransformation = if (visibility2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { visibility2 = !visibility2 }) {
                                Icon(
                                    painter = if (visibility2)
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
                                tint = Light_brown,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                "Re-enter password",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedBorderColor = Brown,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        val phoneNumberText = phoneNumber.text
                        when {
                            fullName.isBlank() || phoneNumberText.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()
                            }
                            password != confirmPassword -> {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                            }
                            phoneNumberText.length < 7 -> {
                                Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumberText"
                                userViewModel.register(email, password, fullName, phoneNumberText) { success, message, userId ->
                                    if (success) {
                                        val model = UserModel(
                                            userId = userId,
                                            email = email,
                                            fullName = fullName,
                                            phoneNumber = fullPhoneNumber,
                                            selectedCountry = selectedCountry.name
                                        )
                                        userViewModel.addUserToDatabase(userId, model) { dbSuccess, dbMessage ->
                                            Toast.makeText(context, dbMessage, Toast.LENGTH_LONG).show()
                                        }
                                        val intent = Intent(context, LoginActivity::class.java)
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(14.dp))
                        .testTag("register_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Light_brown, Brown)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Create Account",
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
                        "Already have an account? ",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark.copy(alpha = 0.7f) else Grey,
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

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
@Preview
fun PreviewRegistration() {
    RegistrationBody()
}