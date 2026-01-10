package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.closetly.ui.theme.ClosetlyTheme
import com.example.closetly.ui.theme.White

class ListingDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListingDetailsBody()
        }
    }
}

@Composable
fun ListingDetailsBody(){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ){
        Text(text = "Listing Details")
        Text(text = "Listing Details")
        Text(text = "Listing Details")
        Text(text = "Listing Details")
    }
}

@Preview
@Composable
fun PreviewListingDetails() {
    ListingDetailsBody()
}