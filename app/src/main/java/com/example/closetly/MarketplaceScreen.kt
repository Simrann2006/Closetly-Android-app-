package com.example.closetly

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.*
import com.example.closetly.model.ProductModel
import com.example.closetly.model.ListingType
import com.example.closetly.viewmodel.ProductViewModel
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.repository.ChatRepoImpl
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast

@Composable
fun MarketplaceScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedMarket by remember { mutableStateOf("All") }
    val context = LocalContext.current

    val productRepo = remember { ProductRepoImpl() }
    val productViewModel: ProductViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(productRepo) as T
        }
    })
    val products by productViewModel.products.collectAsState()

    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }

    val filteredProducts = remember(selectedMarket, searchText, products) {
        products.filter { product ->
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
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No products found",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 16.sp,
                            color = Grey
                        )
                    )
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

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Brown else Light_grey
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    color = if (isSelected) White else Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
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
                            .padding(8.dp)
                            .size(36.dp),
                        shape = CircleShape,
                        color = White,
                        shadowElevation = 2.dp
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
                            .padding(8.dp),
                        color = when (product.listingType) {
                            ListingType.RENT -> Light_brown
                            ListingType.THRIFT -> Grey
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = product.listingType.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                                .padding(8.dp),
                            color = when (product.status) {
                                "Sold Out" -> Red
                                "On Rent" -> DarkYellow
                                else -> Grey
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = product.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                        .background(White)
                        .padding(12.dp)
                ) {
                    Text(
                        text = product.title,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (product.listingType == ListingType.RENT && product.rentPricePerDay != null) {
                            "$${product.rentPricePerDay}/day"
                        } else {
                            "$${product.price}"
                        },
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
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
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
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (product.listingType) {
                            ListingType.RENT -> Light_brown
                            ListingType.THRIFT -> Grey
                        }
                    ) {
                        Text(
                            text = product.listingType.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (product.listingType == ListingType.RENT && product.rentPricePerDay != null) {
                        "$${product.rentPricePerDay}/day"
                    } else {
                        "$${product.price}"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brown
                )
                
                if (product.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Description:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Grey
                    )
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
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
                    ProductDetailRow(label = "Sale Price", value = "$${product.price}")
                }
                ProductDetailRow(label = "Posted", value = getTimeAgoListing(product.timestamp))
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Light_grey)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (product.sellerProfilePic.isNotEmpty()) {
                        AsyncImage(
                            model = product.sellerProfilePic,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Brown.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = product.sellerName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Brown
                                )
                            }
                        }
                    }
                    
                    Column {
                        Text(
                            text = product.sellerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        )
                        Text(
                            text = "Seller",
                            fontSize = 12.sp,
                            color = Grey
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                                        putExtra("otherUserName", product.sellerName)
                                        putExtra("otherUserImage", product.sellerProfilePic ?: "")
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
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                                tint = White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Message Seller",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Grey
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Black
        )
    }
}

fun getTimeAgoListing(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 30 -> "${days / 30} month${if (days / 30 > 1) "s" else ""} ago"
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewMarketplace() {
    MarketplaceScreen()
}