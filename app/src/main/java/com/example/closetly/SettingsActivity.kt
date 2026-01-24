package com.example.closetly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.White
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                SettingsBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBody() {
    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl(context) }
    val userViewModel = remember { UserViewModel(userRepo) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(ThemeManager.isDarkMode) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { 
                        (context as Activity).finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

            Text(
                "ACCOUNT",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                onClick = {
                    val intent = Intent(context, BlockedUsersActivity::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 8.dp)

            Text(
                "CONTENT",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.baseline_bookmark_border_24,
                title = "Saved Posts",
                onClick = {
                    val intent = Intent(context, SavedPostsActivity::class.java)
                    context.startActivity(intent)
                }
            )

            SettingsItem(
                icon = R.drawable.baseline_favorite_border_24,
                title = "Liked Posts",
                onClick = {
                    val intent = Intent(context, LikedPostsActivity::class.java)
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 8.dp)

            Text(
                "PREFERENCES",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsToggleItem(
                icon = R.drawable.baseline_dark_mode_24,
                title = "Dark Mode",
                subtitle = "Switch between light and dark theme",
                checked = darkModeEnabled,
                onCheckedChange = { 
                    darkModeEnabled = it
                    ThemeManager.setDarkMode(context, it)
                    (context as Activity).recreate()
                }
            )

            SettingsToggleItem(
                icon = R.drawable.notification,
                title = "Notifications",
                subtitle = "Receive notifications",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 8.dp)

            Text(
                "ABOUT",style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.outline_info_24,
                title = "About Us",
                onClick = { }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 8.dp)

            Text(
                "ACTIONS", style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            SettingsItem(
                icon = R.drawable.baseline_logout_24,
                title = "Logout",
                onClick = { showLogoutDialog = true },
                textColor = MaterialTheme.colorScheme.onBackground
            )

            SettingsItem(
                icon = R.drawable.baseline_delete_24,
                title = "Delete Account",
                onClick = { showDeleteDialog = true },
                textColor = Color.Red
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", color = MaterialTheme.colorScheme.onSurface) },


            text = { Text("Are you sure you want to logout?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        userViewModel.logout { success, message ->
                            if (success) {
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as Activity).finish()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.let {
                            val userId = it.uid
                            FirebaseDatabase.getInstance().getReference("Users").child(userId).removeValue()
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        it.delete().addOnCompleteListener { authTask ->
                                            if (authTask.isSuccessful) {
                                                val intent = Intent(context, LoginActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(intent)
                                                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                                (context as Activity).finish()
                                            } else {
                                                Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Failed to delete data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onBackground
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
            tint = if (textColor == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = White,
                uncheckedTrackColor = Grey.copy(alpha = 0.4f)
            )
        )
    }
}

@Preview
@Composable
fun SettingsPreview() {
    SettingsBody()
}