package com.example.closetly

import android.R.attr.onClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.White

@Composable
fun MarketplaceScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedMarket by remember { mutableStateOf("All") }
    val scrollState = rememberScrollState()
    var counter by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(10.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Marketplace",
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brown,
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = {
                        Text(
                            "Search items or sellers......",
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row{
                Surface(
                    modifier = Modifier
                        .width(122.dp)
                        .height(57.dp)
                        .padding(7.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Light_grey
                ) {

                }
                Surface(
                    modifier = Modifier
                        .width(122.dp)
                        .height(57.dp)
                        .padding(7.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Light_grey
                ) {

                }
                Surface(
                    modifier = Modifier
                        .width(122.dp)
                        .height(57.dp)
                        .padding(7.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Light_grey
                ) {

                }

            }
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Card(
                    modifier = Modifier
                        .height(375.dp)
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.longgown),
                            modifier = Modifier
                                .size(250.dp),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Text(
                            "Long Gown", style = TextStyle(
                                color = Black,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Start
                            )
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.profile),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(25.dp)
                                    .width(25.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Text("@emma_002", style = TextStyle(
                                fontWeight = FontWeight.Light

                            ))

                        }
                    }

                }
                Spacer(modifier = Modifier.width(5.dp))
                Card(
                    modifier = Modifier
                        .height(375.dp)
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bag),
                            modifier = Modifier
                                .size(250.dp),
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            "Bag", style = TextStyle(
                                color = Black,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Start
                            )
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.profile),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(25.dp)
                                    .width(25.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Text("@emma_002", style = TextStyle(
                                fontWeight = FontWeight.Light

                                ))

                        }
                    }

                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewMarketplace(){
    MarketplaceScreen()
}