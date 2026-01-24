package com.example.closetly.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.ui.theme.Purple80

class ChangePasswordScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme {
                ChangePasswordUI()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordUI() {

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive,
                        color = Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            PasswordField(
                label = "Current password",
                value = currentPassword,
                isVisible = currentVisible,
                onValueChange = { currentPassword = it },
                onVisibilityChange = { currentVisible = !currentVisible }
            )

            PasswordField(
                label = "New password",
                value = newPassword,
                isVisible = newVisible,
                onValueChange = { newPassword = it },
                onVisibilityChange = { newVisible = !newVisible }
            )

            PasswordField(
                label = "Confirm new password",
                value = confirmPassword,
                isVisible = confirmVisible,
                onValueChange = { confirmPassword = it },
                onVisibilityChange = { confirmVisible = !confirmVisible }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple80,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.SemiBold
                )
            }

        }
    }
}
@Composable
fun PasswordField(
    label: String,
    value: String,
    isVisible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityChange: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation =
            if (isVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Icon(
                    imageVector =
                        if (isVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple80,
            unfocusedBorderColor = Purple80.copy(alpha = 0.5f),
            focusedLabelColor = Purple80,
            cursorColor = Purple80
        )
    )
}


@Preview(showBackground = true)
@Composable
fun ChangePasswordPreview() {
    ClosetlyTheme {
        ChangePasswordUI()
    }
}
