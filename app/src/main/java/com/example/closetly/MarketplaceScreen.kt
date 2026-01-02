package com.example.closetly

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

// Data Models
data class MarketplaceProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val size: String,
    val brand: String,
    val condition: String,
    val listingType: ListingType,
    val sellerName: String,
    val rentPricePerDay: Double? = null
)

enum class ListingType {
    SALE, RENT, THRIFT
}

@Composable
fun MarketplaceScreen() {
    var searchText by remember { mutableStateOf("") }
    var selectedMarket by remember { mutableStateOf("All") }
    var hoveredProductId by remember { mutableStateOf<String?>(null) }
    val products = remember { getSampleProducts() }

    // Filter products based on selected market
    val filteredProducts = remember(selectedMarket, searchText) {
        products.filter { product ->
            val matchesMarket = when (selectedMarket) {
                "All" -> true
                "Sale" -> product.listingType == ListingType.SALE
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
            // Header
            Text(
                "Marketplace",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brown,
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
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

            // Filter Buttons
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
                    text = "Sale",
                    isSelected = selectedMarket == "Sale",
                    onClick = { selectedMarket = "Sale" }
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

            // Product Grid
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
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isHovered = hoveredProductId == product.id,
                            onHoverChange = {
                                hoveredProductId = if (it) product.id else null
                            },
                            onClick = {
                                // Handle product click
                                // Navigate to product detail
                            }
                        )
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
            .clickable(onClick = onClick),
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
    product: MarketplaceProduct,
    isHovered: Boolean,
    onHoverChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardHovered by interactionSource.collectIsHoveredAsState()
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(cardHovered) {
        onHoverChange(cardHovered)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.70f)
            .hoverable(interactionSource)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (cardHovered) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Box {
            Column(modifier = Modifier.fillMaxSize()) {
                // Product Image
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

                    // Listing Type Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = when (product.listingType) {
                            ListingType.SALE -> Brown
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

                    // Favorite Button
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Basic Info (Always Visible)
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
                        text = "$${product.price}",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brown
                        )
                    )
                }

                // Expanded Details (Shown on Hover or can be always visible)
                AnimatedVisibility(
                    visible = cardHovered,
                    enter = expandVertically(
                        spring(stiffness = Spring.StiffnessLow)
                    ) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Light_grey)
                            .padding(12.dp)
                    ) {
                        // Brand & Size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = product.brand,
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 11.sp,
                                    color = Grey
                                )
                            )
                            Text(
                                text = "Size: ${product.size}",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 11.sp,
                                    color = Grey
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Condition
                        Text(
                            text = "Condition: ${product.condition}",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 11.sp,
                                color = Grey
                            )
                        )

                        // Rent Price (if available)
                        if (product.rentPricePerDay != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rent: $${product.rentPricePerDay}/day",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 11.sp,
                                    color = Brown,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Seller Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = Brown.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = product.sellerName.first().toString(),
                                        style = TextStyle(
                                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Brown
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = product.sellerName,
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sample Data
fun getSampleProducts() = listOf(
    MarketplaceProduct(
        id = "1",
        title = "Vintage Denim Jacket",
        description = "Classic blue denim jacket in excellent condition",
        price = 45.0,
        imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400",
        category = "Outerwear",
        size = "M",
        brand = "Levi's",
        condition = "Like New",
        listingType = ListingType.THRIFT,
        sellerName = "Sarah Johnson"
    ),
    MarketplaceProduct(
        id = "2",
        title = "Floral Summer Dress",
        description = "Beautiful floral print dress perfect for summer",
        price = 60.0,
        imageUrl = "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400",
        category = "Dresses",
        size = "S",
        brand = "Zara",
        condition = "New",
        listingType = ListingType.SALE,
        sellerName = "Emma Davis",
        rentPricePerDay = 15.0
    ),
    MarketplaceProduct(
        id = "3",
        title = "Leather Ankle Boots",
        description = "Brown leather ankle boots",
        price = 80.0,
        imageUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=400",
        category = "Shoes",
        size = "8",
        brand = "Dr. Martens",
        condition = "Gently Used",
        listingType = ListingType.SALE,
        sellerName = "Mike Chen"
    ),
    MarketplaceProduct(
        id = "4",
        title = "White Sneakers",
        description = "Classic white sneakers",
        price = 55.0,
        imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400",
        category = "Shoes",
        size = "9",
        brand = "Adidas",
        condition = "Like New",
        listingType = ListingType.RENT,
        sellerName = "Lisa Wong",
        rentPricePerDay = 10.0
    ),
    MarketplaceProduct(
        id = "5",
        title = "Silk Evening Gown",
        description = "Elegant silk evening gown",
        price = 120.0,
        imageUrl = "https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=400",
        category = "Dresses",
        size = "M",
        brand = "Versace",
        condition = "New",
        listingType = ListingType.RENT,
        sellerName = "Anna Martinez",
        rentPricePerDay = 25.0
    ),
    MarketplaceProduct(
        id = "6",
        title = "Casual T-Shirt",
        description = "Comfortable cotton t-shirt",
        price = 20.0,
        imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400",
        category = "Tops",
        size = "L",
        brand = "H&M",
        condition = "Gently Used",
        listingType = ListingType.THRIFT,
        sellerName = "John Smith"
    ),
    MarketplaceProduct(
        id = "7",
        title = "Designer Handbag",
        description = "Luxury designer handbag",
        price = 250.0,
        imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=400",
        category = "Accessories",
        size = "One Size",
        brand = "Gucci",
        condition = "Like New",
        listingType = ListingType.SALE,
        sellerName = "Sophie Taylor"
    ),
    MarketplaceProduct(
        id = "8",
        title = "Wool Sweater",
        description = "Cozy wool sweater for winter",
        price = 40.0,
        imageUrl = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=400",
        category = "Tops",
        size = "M",
        brand = "Uniqlo",
        condition = "New",
        listingType = ListingType.THRIFT,
        sellerName = "David Lee"
    )
)

@Composable
@Preview(showBackground = true)
fun PreviewMarketplace() {
    MarketplaceScreen()
}