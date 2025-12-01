package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.White

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@Composable
fun LoginBody(){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }


    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ){
            Image(painter = painterResource(R.drawable.loginbg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ){
                Spacer(Modifier.height(350.dp))

                Text("Log in",
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brown,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    onValueChange = { data ->
                        email = data
                    },
                    label = {
                        Text("Email", style = TextStyle(
                            fontSize = 15.sp,
                            color = Grey)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape((22.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Brown,
                        unfocusedIndicatorColor = Light_brown
                    )
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,

                    onValueChange = { data ->
                        password = data
                    },
                    visualTransformation = if(visibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            visibility = !visibility
                        }) {
                            Icon(
                                painter = if (visibility)
                                    painterResource(R.drawable.outline_visibility_off_24) else
                                    painterResource(R.drawable.outline_visibility_24),
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text("Password", style = TextStyle(
                            fontSize = 15.sp,
                            color = Grey
                        ))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape((22.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Brown,
                        unfocusedIndicatorColor = Light_brown
                    )
                )

                Spacer(Modifier.height(2.dp))

                Text("Forget Password?", style = TextStyle(
                    color = Brown,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign =TextAlign.End),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                )

                Spacer(Modifier.height(1.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = terms,
                        onCheckedChange = {
                            terms = it
                        },
                        modifier = Modifier.size(5.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Brown,
                            checkmarkColor = White,
                            uncheckedColor = Light_brown
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Remember me", style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ))
                }

                Spacer(Modifier.height(15.dp))

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .width(170.dp)
                ) {
                    Text(
                        "Log in", style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 15.dp),

                    verticalAlignment = Alignment.CenterVertically
                ){
                    HorizontalDivider(Modifier.weight(1f), color = Black)
                    Text("or sign in with",modifier = Modifier.padding(horizontal = 15.dp), color = Brown)
                    HorizontalDivider(Modifier.weight(1f), color = Black)
                }

                Spacer(Modifier.height(1.dp))

                OutlinedButton(
                    {},
                    modifier = Modifier
                        .width(135.dp)
                        .height(43.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Light_grey,
                        contentColor = Black
                    ),
                    border = BorderStroke(1.dp, Black)
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Google",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Black)) {
                            append("Don't have an account?")
                        }
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                color = if (isClicked) Color.Blue else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign Up")
                        }
                    },
                    modifier = Modifier
                        .padding(15.dp)
                        .clickable {
                            isClicked = true
                        }
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewLogin() {
    LoginBody()
}