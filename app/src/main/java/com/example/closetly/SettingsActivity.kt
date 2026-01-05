package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Light_grey1

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingsBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBody() {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider(color = Light_grey1, thickness = 0.5.dp)

            Text(
                "ACCOUNT",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.baseline_lock_24,
                title = "Change Password",
                onClick = { }
            )

            SettingsItem(
                icon = R.drawable.baseline_block_24,
                title = "Blocked Users",
                onClick = { }
            )

            HorizontalDivider(color = Light_grey1, thickness = 8.dp)

            Text(
                "CONTENT",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.save,
                title = "Saved Posts",
                onClick = { }
            )

            SettingsItem(
                icon = R.drawable.heart,
                title = "Liked Posts",
                onClick = { }
            )

            HorizontalDivider(color = Light_grey1, thickness = 8.dp)

            Text(
                "PREFERENCES",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.notification,
                title = "Notifications",
                onClick = { }
            )

            HorizontalDivider(color = Light_grey1, thickness = 8.dp)

            Text(
                "ABOUT",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.outline_info_24,
                title = "About Us",
                onClick = { }
            )

            HorizontalDivider(color = Light_grey1, thickness = 8.dp)

            Text(
                "ACTIONS", style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.baseline_logout_24,
                title = "Logout",
                onClick = { },
                textColor = Color.Black
            )

            SettingsItem(
                icon = R.drawable.baseline_delete_24,
                title = "Delete Account",
                onClick = {  },
                textColor = Color.Red
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (textColor == Color.Red) Color.Red else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.baseline_arrow_forward_ios_24),
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview
@Composable
fun SettingsPreview() {
    SettingsBody()
}