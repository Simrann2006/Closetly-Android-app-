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

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistrationBody()
        }
    }
}

// Data class for country codes
data class Country(val name: String, val code: String, val dialCode: String)

@Composable
fun RegistrationBody() {

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var visibility1 by remember { mutableStateOf(false) }
    var visibility2 by remember { mutableStateOf(false) }

    var isloginClicked by remember { mutableStateOf(false) }

    // Country code picker state
    var selectedCountry by remember { mutableStateOf(Country("United States", "US", "+1")) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity

    val scrollState = rememberScrollState()

    // List of popular countries with their dial codes
    val countries = listOf(
        Country("United States", "US", "+1"),
        Country("United Kingdom", "GB", "+44"),
        Country("Canada", "CA", "+1"),
        Country("Australia", "AU", "+61"),
        Country("India", "IN", "+91"),
        Country("Germany", "DE", "+49"),
        Country("France", "FR", "+33"),
        Country("Italy", "IT", "+39"),
        Country("Spain", "ES", "+34"),
        Country("Japan", "JP", "+81"),
        Country("China", "CN", "+86"),
        Country("Brazil", "BR", "+55"),
        Country("Mexico", "MX", "+52"),
        Country("South Korea", "KR", "+82"),
        Country("Netherlands", "NL", "+31"),
        Country("Sweden", "SE", "+46"),
        Country("Norway", "NO", "+47"),
        Country("Denmark", "DK", "+45"),
        Country("Finland", "FI", "+358"),
        Country("Switzerland", "CH", "+41")
    )

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

            // Title
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

            // Full Name Field
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

            // Phone Number Field with Country Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country Code Picker
                Box {
                    OutlinedTextField(
                        value = selectedCountry.dialCode,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(android.R.drawable.arrow_down_float),
                                contentDescription = "Select Country",
                                tint = Light_brown,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .width(110.dp)
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(22.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedIndicatorColor = Brown,
                            unfocusedIndicatorColor = Light_brown,
                            disabledContainerColor = White,
                            disabledIndicatorColor = Light_brown
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Black
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.height(300.dp)
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${country.name} (${country.dialCode})",
                                        style = TextStyle(
                                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                            fontSize = 14.sp
                                        )
                                    )
                                },
                                onClick = {
                                    selectedCountry = country
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Phone Number Input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { data ->
                        // Only allow digits
                        if (data.all { it.isDigit() }) {
                            phoneNumber = data
                        }
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
                    singleLine = true
                )
            }

            Spacer(Modifier.height(12.dp))

            // Email Field
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

            // Password Field
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

            // Confirm Password Field
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

            // Create Account Button
            Button(
                onClick = {
                    when {
                        fullName.isBlank() || phoneNumber.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG)
                                .show()
                        }

                        password != confirmPassword -> {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG)
                                .show()
                        }

                        phoneNumber.length < 7 -> {
                            Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_LONG)
                                .show()
                        }

                        else -> {
                            val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumber"
                            // You'll need to update your backend to accept phone number
                            // For now, we'll pass empty string for DOB
                            userViewModel.register(email, password, fullName, phoneNumber) {
                                    success, message, userId ->
                                if (success) {
                                    val model = UserModel(
                                        userId = userId,
                                        email = email,
                                        fullName = fullName,
                                        phoneNumber = fullPhoneNumber
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
                onClick = {},
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