package com.example.closetly

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.model.UserModel
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.White
import com.example.closetly.viewmodel.UserViewModel
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CustomCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistrationBody()
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

    var isloginClicked by remember { mutableStateOf(false) }

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
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.registrationbg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text("Create Account",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brown,
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Join us and start your wardrobe journey",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Grey,
                )
            )

            Spacer(modifier = Modifier.height(25.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { data ->
                    fullName = data
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_person_24),
                        contentDescription = null,
                        tint = Light_brown
                    )
                },
                placeholder = {
                    Text(
                        "Full Name", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape((22.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Brown,
                    unfocusedIndicatorColor = Light_brown
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                    contentDescription = null,
                                    tint = Light_brown,
                                    modifier = Modifier.size(35.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .width(135.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedIndicatorColor = Brown,
                            unfocusedIndicatorColor = Light_brown,
                            disabledContainerColor = White,
                            disabledIndicatorColor = Light_brown,
                            disabledTextColor = Black
                        ),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Black,
                            fontWeight = FontWeight.SemiBold,
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .height(300.dp)
                            .background(White, RoundedCornerShape(12.dp))
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            getCountryFlagEmoji(country.code),
                                            style = TextStyle(
                                                fontSize = 22.sp,
                                                shadow = androidx.compose.ui.graphics.Shadow(
                                                    color = Color.Black.copy(alpha = 0.2f),
                                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                                    blurRadius = 2f
                                                )
                                            )
                                        )
                                        Text(
                                            "${country.name} (${country.dialCode})",
                                            style = TextStyle(
                                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                                fontSize = 14.sp,
                                                color = Black
                                            )
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCountry = country
                                    expanded = false
                                },
                                modifier = Modifier.background(White)
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    placeholder = {
                        Text(
                            "Phone Number", style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 15.sp,
                                color = Grey
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Brown,
                        unfocusedIndicatorColor = Light_brown
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                onValueChange = { data ->
                    email = data
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_mail_outline_24),
                        contentDescription = null,
                        tint = Light_brown
                    )
                },
                placeholder = {
                    Text(
                        "Email", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape((22.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Brown,
                    unfocusedIndicatorColor = Light_brown
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { data ->
                    password = data
                },
                visualTransformation = if (visibility1) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        visibility1 = !visibility1
                    }) {
                        Icon(
                            painter = if (visibility1)
                                painterResource(R.drawable.outline_visibility_off_24) else
                                painterResource(R.drawable.outline_visibility_24),
                            contentDescription = null
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_lock_24),
                        contentDescription = null,
                        tint = Light_brown
                    )
                },
                placeholder = {
                    Text(
                        "Create Password", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape((22.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Brown,
                    unfocusedIndicatorColor = Light_brown
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { data ->
                    confirmPassword = data
                },
                visualTransformation = if (visibility2) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        visibility2 = !visibility2
                    }) {
                        Icon(
                            painter = if (visibility2)
                                painterResource(R.drawable.outline_visibility_off_24) else
                                painterResource(R.drawable.outline_visibility_24),
                            contentDescription = null
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_lock_24),
                        contentDescription = null,
                        tint = Light_brown
                    )
                },
                placeholder = {
                    Text(
                        "Confirm Password", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape((22.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Brown,
                    unfocusedIndicatorColor = Light_brown
                )
            )

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = {
                    val phoneNumberText = phoneNumber.text
                    when {
                        fullName.isBlank() || phoneNumberText.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG)
                                .show()
                        }

                        password != confirmPassword -> {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG)
                                .show()
                        }

                        phoneNumberText.length < 7 -> {
                            Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_LONG)
                                .show()
                        }

                        else -> {
                            val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumberText"
                            userViewModel.register(email, password, fullName, phoneNumberText) {
                                    success, message, userId ->
                                if (success) {
                                    val model = UserModel(
                                        userId = userId,
                                        email = email,
                                        fullName = fullName,
                                        phoneNumber = fullPhoneNumber,
                                        selectedCountry = selectedCountry.name
                                    )
                                    userViewModel.addUserToDatabase(userId, model) {
                                            success, message ->
                                        if (success) {
                                            Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brown
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(55.dp)
                    .width(230.dp),
            ) {
                Text(
                    "Create Account", style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(Modifier.weight(1f), color = Black, thickness = 1.dp)
                Text(
                    "or continue with",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Brown
                )
                HorizontalDivider(Modifier.weight(1f), color = Black, thickness = 1.dp)
            }

            Spacer(Modifier.height(12.dp))

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
                    .width(160.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Light_grey,
                    contentColor = Black
                ),
                border = BorderStroke(1.dp, Black)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Google",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.padding(15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account?", style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = Black,
                        fontSize = 14.sp
                    )
                )

                Spacer(Modifier.width(5.dp))

                TextButton(
                    onClick = {
                        isloginClicked = true
                        val intent = Intent(
                            context,
                            LoginActivity::class.java
                        )
                        context.startActivity(intent)
                        activity.finish()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Login", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = if (isloginClicked) Color.Blue else Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
@Preview
fun PreviewRegistration() {
    RegistrationBody()
}