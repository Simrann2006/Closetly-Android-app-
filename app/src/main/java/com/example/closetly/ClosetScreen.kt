package com.example.closetly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(16.dp)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
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
                    Icon(painter = painterResource(R.drawable.baseline_search_24),
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

            FloatingActionButton(
                onClick = {},
                containerColor = Skin,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = null
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

        Surface (
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
                CategoryButton(
                    text = "All",
                    isSelected = selectedCategory == "All",
                    onClick = { selectedCategory = "All" },
                )
                CategoryButton(
                    text = "Tops",
                    isSelected = selectedCategory == "Tops",
                    onClick = { selectedCategory = "Tops" },
                )
                CategoryButton(
                    text = "Bottoms",
                    isSelected = selectedCategory == "Bottoms",
                    onClick = { selectedCategory = "Bottoms" },
                )
                CategoryButton(
                    text = "Shoes",
                    isSelected = selectedCategory == "Shoes",
                    onClick = { selectedCategory = "Shoes" },
                )
                CategoryButton(
                    text = "Dresses",
                    isSelected = selectedCategory == "Dresses",
                    onClick = { selectedCategory = "Dresses" },
                )
                CategoryButton(
                    text = "Outerwear",
                    isSelected = selectedCategory == "Outerwear",
                    onClick = { selectedCategory = "Outerwear" },
                )
            }
        }
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
