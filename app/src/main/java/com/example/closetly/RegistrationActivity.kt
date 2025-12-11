package com.example.closetly

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.hardware.lights.Light
import android.icu.util.Calendar
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
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
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.White
import org.intellij.lang.annotations.JdkConstants

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistrationBody()
        }
    }
}

@Composable
fun RegistrationBody(){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }
    var isForgotClicked by remember { mutableStateOf(false)}
    var terms by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
//    val context = LocalContext.current
//    val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)
//    val editor = sharedPreferences.edit()
    var selectedDate by remember { mutableStateOf("") }
//    val activity = context as Activity
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

//    var datepicker = DatePickerDialog(
//        context,{
//                _,y,m,d->
//            selectedDate = "$y/${m+1}/$d"
//
//        },year,month,day
//    )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            Image(
                painter = painterResource(R.drawable.registrationbg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ){
                Text(
                    "Create an account ",
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brown,
                    )
                )
                Text(
                    "Sign up to continue ",
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = Brown,
                    )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                OutlinedTextField(
                    value = email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    onValueChange = { data ->
                        email = data
                    },
                    leadingIcon = {
                        Icon(painter = painterResource(R.drawable.baseline_mail_outline_24),
                            contentDescription = null,
                            tint = Light_brown
                        )
                    },
                    label = {
                        Text("Email", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey)
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
                    leadingIcon = {
                        Icon(painter = painterResource(R.drawable.baseline_lock_24),
                            contentDescription = null,
                            tint = Light_brown
                        )
                    },
                    label = {
                        Text(" Create Password", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey
                        ))
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
                    leadingIcon = {
                        Icon(painter = painterResource(R.drawable.baseline_lock_24),
                            contentDescription = null,
                            tint = Light_brown
                        )
                    },
                    label = {
                        Text(" Confirm Password", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 15.sp,
                            color = Grey
                        ))
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

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {
                        selectedDate = it
                    },
                    leadingIcon = {
                        Icon(painter = painterResource(R.drawable.baseline_calendar_month_24),
                            contentDescription = null,
                            tint = Light_brown
                        )
                    },
                    placeholder = {
                        Text("dd/mm/yyyy")
                    },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth().clickable{
//                        datepicker.show()
                        }
                        .padding(horizontal = 15.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        disabledIndicatorColor = Brown,
                        disabledContainerColor = White,
                        focusedContainerColor = White,
                        unfocusedContainerColor = Light_brown,
                        focusedIndicatorColor = Brown,
                        unfocusedIndicatorColor = Light_brown
                    )
                )
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Checkbox(
                        checked = terms,
                        onCheckedChange = {
                            terms = it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Blue,
                            checkmarkColor = White
                        )
                    )
                    Text("I agree to terms & conditions")
                }

                Button(
                    onClick = {
//                    if(!terms){
//                        Toast.makeText(context,"Please agree to terms & conditions", Toast.LENGTH_SHORT).show()
//                    }else{
//                        editor.putString("email",email)
//                        editor.putString("password",password)
//                        editor.putString("date",selectedDate)
//
//                        editor.apply()
//                        activity.finish()
//
//                        Toast.makeText(context,"Registration success", Toast.LENGTH_SHORT).show()
//
//
//                    }

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(55.dp)
                        .width(170.dp)
                ) {
                    Text(
                        "Sign Up", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 28.sp,
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
                    Text("or sign in with",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular))
                        ),
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        color = Brown
                    )
                    HorizontalDivider(Modifier.weight(1f), color = Black)
                }

                Spacer(Modifier.height(1.dp))

                OutlinedButton(
                    {},
                    modifier = Modifier
                        .width(140.dp)
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
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Google",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(120.dp))

                Row(
                    modifier = Modifier
                        .padding(15.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Already have an account?", style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = Black,
                        fontSize = 14.sp
                    ))

                    Spacer(Modifier.width(4.dp))

                    TextButton(
                        onClick = {isClicked = true},
                        contentPadding = PaddingValues(0.dp)
                    ){
                        Text("Login", style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = if (isClicked) Color.Blue else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ))
                    }
                }

            }

        }
    }




@Composable
@Preview
fun PreviewRegistration(){
    RegistrationBody()
}