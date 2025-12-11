package com.example.closetly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color(0xFF666666)
                        )
                    }

                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                val firstDayOfMonth = currentMonth.atDay(1)
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                val daysInMonth = currentMonth.lengthOfMonth()
                val today = LocalDate.now()

                var dayCounter = 1
                for (week in 0..5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            val dayIndex = week * 7 + dayOfWeek

                            if (dayIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                                val date = currentMonth.atDay(dayCounter)
                                val isToday = date == today
                                val isSelected = date == selectedDate

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> Color(0xFF8B5FBF)
                                                isSelected -> Color(0xFF4A6FDC)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else 0.dp,
                                            color = if (isToday) Color(0xFF8B5FBF) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedDate = date
                                            showDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayCounter.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isToday || isSelected -> Color.White
                                            else -> Color(0xFF333333)
                                        }
                                    )
                                }
                                dayCounter++
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }

    if (showDialog && selectedDate != null) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("×", fontSize = 24.sp, color = Color(0xFF666666))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate!!.dayOfMonth}, ${selectedDate!!.year}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "No outfit scheduled",
                        fontSize = 15.sp,
                        color = Color(0xFF999999)
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showDialog = false
                            TODO("Navigate to Avatar section")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5FBF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Plan Outfit",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCalendar() {
    CalendarScreen()
}