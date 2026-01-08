//package com.example.closetly
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//
//class CommentActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val postId = intent.getStringExtra("POST_ID") ?: "post_1"
//
//        setContent {
//            MaterialTheme {
//                Surface {
//                    CommentScreen(
//                        postId = postId,
//                        onBackClick = { finish() }
//                    )
//                }
//            }
//        }
//    }
//}
