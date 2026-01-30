package com.example.closetly.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.R
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager

class HelpCenterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                HelpCenterBody()
            }
        }
    }
}

data class FAQItem(
    val question: String,
    val answer: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterBody() {
    val context = LocalContext.current
    val activity = context as Activity
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    val scrollState = rememberScrollState()

    val categories = listOf("General", "Wardrobe", "Outfits", "Account")

    val faqList = remember {
        listOf(
            // General - Posts, Chat, Marketplace, Analysis
            FAQItem(
                "How does the Home feed work?",
                "The Home screen shows posts from the community where users share their outfit ideas, styling tips, and fashion inspiration. You can like, comment, and interact with posts to connect with other fashion enthusiasts.",
                "General"
            ),
            FAQItem(
                "What is the Chat system for?",
                "The Chat feature allows you to message other users to ask questions about thrift items, rental details, styling advice, or general fashion queries. Use it to negotiate prices or discuss item conditions.",
                "General"
            ),
            FAQItem(
                "How does the Marketplace work?",
                "The Marketplace lets you browse and list items for thrift (buy/sell) or rent. You can view other users' listings, see your own listings, filter by category, and connect with sellers or renters through chat.",
                "General"
            ),
            FAQItem(
                "How do wardrobe analytics work?",
                "The Analysis screen shows insights like total items, outfits created, most worn items, underused items, favorite colors, and spending by category. These metrics help you make better wardrobe decisions.",
                "General"
            ),
            FAQItem(
                "How do I create a listing for thrift or rent?",
                "Go to the Profile, click on + icon and select Listing, upload photos of your item, set the price, choose thrift or rent option, and add details like size, condition, and description. Your listing will be visible to all users.",
                "General"
            ),
            // Wardrobe
            FAQItem(
                "How do I add clothes to my digital wardrobe?",
                "Tap the '+' button in the Closet screen. You can either take a photo of your clothing item or select from your gallery. Fill in details like category, color, brand, season, and occasion to better organize your wardrobe.",
                "Wardrobe"
            ),
            FAQItem(
                "Can I edit or delete items from my wardrobe?",
                "Yes! Click the item card in your closet to access edit or delete options. You can update details, change the photo, or remove the item permanently.",
                "Wardrobe"
            ),
            FAQItem(
                "How does the wear count tracking work?",
                "Every time you mark an outfit as worn, the wear count for each clothing item in that outfit increases. This helps you identify which items you use most and which are underused.",
                "Wardrobe"
            ),
            FAQItem(
                "What are categories and how do I use them?",
                "Categories help organize your wardrobe by type (tops, bottoms, shoes, accessories, etc.). When creating an outfit, you can filter items by category to quickly find what you need.",
                "Wardrobe"
            ),
            FAQItem(
                "What is the 'underused item' feature?",
                "Closetly identifies items with the lowest wear count to help you rediscover forgotten pieces. This encourages you to maximize your existing wardrobe before buying new items.",
                "Wardrobe"
            ),
            // Outfits
            FAQItem(
                "How do I create an outfit?",
                "Go to the Plan Outfit screen and tap 'Add Item' to select clothing pieces from your wardrobe. Arrange them on the canvas, name your outfit, set an occasion, and optionally schedule it for a specific date.",
                "Outfits"
            ),
            FAQItem(
                "Can I schedule outfits for specific dates?",
                "Absolutely! When creating or editing an outfit, you can set a date or date range. Your scheduled outfits will appear in the calendar view and help you plan your week.",
                "Outfits"
            ),
            FAQItem(
                "How do I mark an outfit as favorite?",
                "Click edit icon on any outfit card and select the heart icon, or tap the outfit to view details and mark it as favorite. Your favorite outfits can be filtered in the Saved Outfits tab.",
                "Outfits"
            ),
            FAQItem(
                "What does 'Mark as Worn' do?",
                "Marking an outfit as worn records when you wore it and updates the wear count for each item in that outfit. This data powers your wardrobe analytics and helps identify underused items.",
                "Outfits"
            ),
            FAQItem(
                "Can I share my outfits with others?",
                "Yes! You can share your outfit creations on the Home feed for others to see, get inspired by, and comment on. This helps build a community of fashion enthusiasts.",
                "Outfits"
            ),
            // Account
            FAQItem(
                "How do I change my profile information?",
                "Go to your profile, tap 'Edit Profile', and update your name, bio, or profile picture. Don't forget to save your changes.",
                "Account"
            ),
            FAQItem(
                "How do I enable dark mode?",
                "Go to Settings > Appearance and toggle Dark Mode. The app will immediately switch to your preferred theme.",
                "Account"
            ),
            FAQItem(
                "How do I change my password?",
                "Navigate to Settings > Security > Change Password. Enter your current password and create a new one with at least 6 characters.",
                "Account"
            ),
            FAQItem(
                "Is my wardrobe data private?",
                "Yes, your wardrobe data is private and secured. Only you can access your clothing items and outfits. We use industry-standard encryption to protect your information.",
                "Account"
            ),
            FAQItem(
                "How do I contact support?",
                "You can reach us at support@closetly.com. We typically respond within 24 hours to help with any questions or issues.",
                "Account"
            )
        )
    }

    val filteredFAQs = remember(selectedCategory, searchQuery) {
        faqList.filter { faq ->
            faq.category == selectedCategory &&
                    (searchQuery.isEmpty() ||
                            faq.question.contains(searchQuery, ignoreCase = true) ||
                            faq.answer.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help Center",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode) White else Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = if (ThemeManager.isDarkMode) White else Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
                )
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Search Bar
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // Category Chips - Horizontally scrollable
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(categories.size) { index ->
                    val category = categories[index]
                    CategoryChip(
                        text = category,
                        isSelected = category == selectedCategory,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // FAQ List
            filteredFAQs.forEachIndexed { index, faq ->
                key(faq.question + index) {
                    FAQCard(faq = faq)
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (filteredFAQs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No FAQs found",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.6f) else Black.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                "Search for help...",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 14.sp,
                    color = if (ThemeManager.isDarkMode)
                        OnBackground_Dark.copy(alpha = 0.4f)
                    else
                        Grey.copy(alpha = 0.6f)
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Light_brown,
                modifier = Modifier.size(22.dp)
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
            focusedBorderColor = Brown,
            unfocusedBorderColor = if (ThemeManager.isDarkMode)
                Grey.copy(alpha = 0.3f)
            else
                Light_grey1,
            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
        ),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = FontFamily(Font(R.font.poppins_regular)),
            fontSize = 14.sp
        )
    )
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            if (ThemeManager.isDarkMode) Brown else Brown
        } else {
            if (ThemeManager.isDarkMode)
                Surface_Dark
            else
                Light_grey
        },
        border = if (!isSelected) {
            BorderStroke(
                1.dp,
                if (ThemeManager.isDarkMode)
                    Grey.copy(alpha = 0.3f)
                else
                    Light_grey1
            )
        } else null,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) White else {
                        if (ThemeManager.isDarkMode) OnSurface_Dark else DarkGrey
                    }
                )
            )
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (ThemeManager.isDarkMode) 0.dp else 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (ThemeManager.isDarkMode) Surface_Dark else White,
        border = if (ThemeManager.isDarkMode) {
            BorderStroke(1.dp, Grey.copy(alpha = 0.2f))
        } else null,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    faq.question,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ThemeManager.isDarkMode) OnSurface_Dark else DarkGrey,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Light_brown,
                    modifier = Modifier
                        .size(24.dp)
                        .animateContentSize()
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Divider(
                        color = if (ThemeManager.isDarkMode)
                            Grey.copy(alpha = 0.2f)
                        else
                            Light_grey1,
                        thickness = 1.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        faq.answer,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            color = if (ThemeManager.isDarkMode)
                                OnSurface_Dark.copy(alpha = 0.8f)
                            else
                                Grey,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NoResultsFound() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (ThemeManager.isDarkMode)
                        Light_brown.copy(alpha = 0.1f)
                    else
                        Light_brown.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Light_brown,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No results found",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ThemeManager.isDarkMode) OnSurface_Dark else DarkGrey
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Try adjusting your search or filters",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 14.sp,
                color = if (ThemeManager.isDarkMode)
                    OnBackground_Dark.copy(alpha = 0.7f)
                else
                    Grey
            )
        )
    }
}

@Composable
fun ContactSupportCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (ThemeManager.isDarkMode) 0.dp else 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (ThemeManager.isDarkMode) Surface_Dark else White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Brown.copy(alpha = 0.1f),
                            Light_brown.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Brown, Light_brown)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_mail_outline_24),
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Still need help?",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Contact our support team",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                color = if (ThemeManager.isDarkMode)
                                    OnSurface_Dark.copy(alpha = 0.7f)
                                else
                                    Grey
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { /* Open contact form */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Brown, Light_brown)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Contact Support",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HelpCenterPreview() {
    HelpCenterBody()
}