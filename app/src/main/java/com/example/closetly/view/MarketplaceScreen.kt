package com.example.closetly.view

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.closetly.ui.theme.*
import com.example.closetly.model.ProductModel
import com.example.closetly.model.ListingType
import com.example.closetly.viewmodel.ProductViewModel
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.repository.ChatRepoImpl
import com.example.closetly.utils.getTimeAgo
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.closetly.R
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedMarket by remember { mutableStateOf("All") }
    val context = LocalContext.current

    val productRepo = remember { ProductRepoImpl() }
    val productViewModel: ProductViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(productRepo) as T
        }
    })
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }
    
    fun refreshData() {
        coroutineScope.launch {
            isRefreshing = true
            productViewModel.loadProducts()
            delay(800) // Smooth animation delay
            refreshTrigger++ // Trigger reshuffle
            isRefreshing = false
        }
    }

    val filteredProducts = remember(selectedMarket, searchText, products, refreshTrigger) {
        val filtered = products.filter { product ->
            val matchesMarket = when (selectedMarket) {
                "All" -> true
                "Rent" -> product.listingType == ListingType.RENT
                "Thrift" -> product.listingType == ListingType.THRIFT
                else -> true
            }
            val matchesSearch = searchText.isEmpty() ||
                    product.title.contains(searchText, ignoreCase = true) ||
                    product.sellerName.contains(searchText, ignoreCase = true)
            matchesMarket && matchesSearch
        }
        filtered.shuffled()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshData() },
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = if (ThemeManager.isDarkMode) Black else White,
                    color = if (ThemeManager.isDarkMode) White else Black
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (ThemeManager.isDarkMode) Background_Dark else Background_Light)
                    .padding(16.dp)
            ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = {
                    Text(
                        "Search items or sellers...",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            color = Grey
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_search_24),
                        contentDescription = null,
                        tint = Grey,
                        modifier = Modifier.size(22.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else Light_grey,
                    focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else Light_grey,
                    unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Color.Transparent,
                    focusedBorderColor = Brown,
                    focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                    unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black
                ),
                textStyle = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 14.sp
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterButton(
                    text = "All",
                    isSelected = selectedMarket == "All",
                    onClick = { selectedMarket = "All" }
                )
                FilterButton(
                    text = "Rent",
                    isSelected = selectedMarket == "Rent",
                    onClick = { selectedMarket = "Rent" }
                )
                FilterButton(
                    text = "Thrift",
                    isSelected = selectedMarket == "Thrift",
                    onClick = { selectedMarket = "Thrift" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading && products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Brown,
                        strokeWidth = 3.dp
                    )
                }
            } else if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_shopping_cart_24),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Grey.copy(alpha = 0.5f)
                        )
                        Text(
                            "No products found",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Grey
                            )
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredProducts.size) { index ->
                        val product = filteredProducts[index]
                        ProductCard(product = product)
                    }
                }
            }
        }
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            Brown
        } else {
            if (ThemeManager.isDarkMode) Surface_Dark else Light_grey
        }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    color = if (isSelected) White else (if (ThemeManager.isDarkMode) White else Black),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductModel
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.70f)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeManager.isDarkMode)
                Surface_Dark.copy(alpha = 0.7f)
            else
                White.copy(alpha = 0.95f)
        )
    ) {
        Box {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .size(38.dp),
                        shape = CircleShape,
                        color = if (ThemeManager.isDarkMode) Surface_Dark else White,
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (product.sellerProfilePic.isNotEmpty()) {
                                AsyncImage(
                                    model = product.sellerProfilePic,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brown.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = product.sellerName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                        style = TextStyle(
                                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Brown
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        color = when (product.listingType) {
                            ListingType.RENT -> Light_brown
                            ListingType.THRIFT -> DarkGrey
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = product.listingType.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        )
                    }

                    if (product.status != "Available") {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            color = when (product.status) {
                                "Sold Out" -> Red
                                "On Rent" -> DarkYellow
                                else -> Grey
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = product.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (ThemeManager.isDarkMode)
                                Surface_Dark.copy(alpha = 0.9f)
                            else
                                White.copy(alpha = 0.95f)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = product.title,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) White else Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (product.listingType == ListingType.RENT && product.rentPricePerDay != null) {
                            "Rs.${product.rentPricePerDay}/day"
                        } else {
                            "Rs.${product.price}"
                        },
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode) Light_brown else Brown
                        )
                    )
                }
            }
        }
    }
    if (showDialog) {
        ProductDetailsDialog(
            product = product,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ProductDetailsDialog(
    product: ProductModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userRepo = remember { UserRepoImpl(context) }

    var sellerName by remember { mutableStateOf(product.sellerName) }
    var sellerProfilePic by remember { mutableStateOf(product.sellerProfilePic) }

    LaunchedEffect(product.sellerId) {
        userRepo.getUserById(product.sellerId) { success, _, userData ->
            if (success && userData != null) {
                sellerName = userData.username
                sellerProfilePic = userData.profilePicture
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (ThemeManager.isDarkMode)
                    Surface_Dark.copy(alpha = 0.95f)
                else
                    White.copy(alpha = 0.98f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.title,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode) White else Black
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when (product.listingType) {
                            ListingType.RENT -> Light_brown
                            ListingType.THRIFT -> DarkGrey
                        }
                    ) {
                        Text(
                            text = product.listingType.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (product.listingType == ListingType.RENT && product.rentPricePerDay != null) {
                        "Rs.${product.rentPricePerDay}/day"
                    } else {
                        "Rs.${product.price}"
                    },
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) Light_brown else Brown
                    )
                )

                if (product.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Description",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Grey
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = product.description,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black,
                            lineHeight = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (product.brand.isNotEmpty()) {
                    ProductDetailRow(label = "Brand", value = product.brand)
                }
                if (product.size.isNotEmpty()) {
                    ProductDetailRow(label = "Size", value = product.size)
                }
                if (product.condition.isNotEmpty()) {
                    ProductDetailRow(label = "Condition", value = product.condition)
                }
                if (product.listingType == ListingType.RENT && product.rentPricePerDay != null) {
                    ProductDetailRow(label = "Sale Price", value = "Rs.${product.price}")
                }
                ProductDetailRow(label = "Posted", value = getTimeAgo(product.timestamp))

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (sellerProfilePic.isNotEmpty()) {
                        AsyncImage(
                            model = sellerProfilePic,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = Brown.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = sellerName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Brown
                                    )
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = sellerName,
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) White else Black
                            )
                        )
                        Text(
                            text = "Seller",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                color = Grey
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (product.sellerId != currentUserId) {
                    var isLoadingChat by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            isLoadingChat = true
                            val chatRepo = ChatRepoImpl()
                            chatRepo.getOrCreateChat(currentUserId, product.sellerId) { success, message, chatId ->
                                isLoadingChat = false
                                if (success && chatId != null) {
                                    val intent = Intent(context, ChatActivity::class.java).apply {
                                        putExtra("chatId", chatId)
                                        putExtra("otherUserId", product.sellerId)
                                        putExtra("otherUserName", sellerName)
                                        putExtra("otherUserImage", sellerProfilePic)
                                        putExtra("productId", product.id)
                                        putExtra("productTitle", product.title)
                                        putExtra("productImageUrl", product.imageUrl)
                                        putExtra("productPrice", product.price)
                                        putExtra("productListingType", product.listingType.name)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Failed to open chat: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isLoadingChat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isLoadingChat) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.chat),
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Message Seller",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Grey
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (ThemeManager.isDarkMode) White else Black
            )
        )
    }
}