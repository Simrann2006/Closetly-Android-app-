package com.example.closetly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.Skin
import com.example.closetly.ui.theme.White

@Composable
fun ClosetScreen() {

    var selectedCategory by remember { mutableStateOf("All") }
    var searchText by remember { mutableStateOf("") }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddClothesDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val categories = remember {
        mutableStateListOf("All", "Tops", "Bottoms", "Shoes")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = {
                        Text(
                            "Search your closet...",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = Grey
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = null,
                            tint = Grey,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Light_grey,
                        focusedContainerColor = Light_grey,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Brown
                    ),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true
                )

                val rotation by animateFloatAsState(
                    targetValue = if (isFabExpanded) 45f else 0f,
                    label = "rotation"
                )

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = Skin,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Skin
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Try on Avatar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp)
                    .padding(bottom = 12.dp),

                shape = RoundedCornerShape(24.dp),
                color = Light_grey
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(7.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        CategoryButton(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isFabExpanded,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 24.dp),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Brown,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            "Add to Closet",
                            modifier = Modifier.padding(12.dp, 6.dp),
                            color = White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            showAddClothesDialog = true
                            isFabExpanded = false
                        },
                        containerColor = Light_brown,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.closet),
                            contentDescription = "Add to Closet",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Brown,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            "Add Category",
                            modifier = Modifier.padding(12.dp, 6.dp),
                            color = White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            showAddCategoryDialog = true
                            isFabExpanded = false
                        },
                        containerColor = Light_brown,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = "Add Category",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddCategoryDialog = false
                newCategoryName = ""
            },
            title = {
                Text(
                    text = "Add New Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter category name:",
                        fontSize = 14.sp,
                        color = Grey
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedBorderColor = Grey
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank() && !categories.contains(newCategoryName)) {
                            categories.add(newCategoryName)
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Brown
                    )
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddCategoryDialog = false
                        newCategoryName = ""
                    }
                ) {
                    Text("Cancel", color = Grey)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddClothesDialog) {
        TODO("Add to closet work")
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(38.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) White else Light_grey,
            contentColor = if (isSelected) Brown else Grey
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp,
            pressedElevation = if (isSelected) 6.dp else 1.dp
        ),
        border = if (isSelected) BorderStroke(
            width = 1.5.dp,
            brush = SolidColor(Brown)
        ) else null,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
@Preview
fun PreviewCloset() {
    ClosetScreen()
}