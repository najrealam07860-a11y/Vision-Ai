package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.viewmodel.GalleryViewModel
import com.example.ui.components.GlassmorphicCard

@Composable
fun SearchTab(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    val localResults by viewModel.localSearchResults.collectAsState()
    val geminiResult by viewModel.geminiResult.collectAsState()
    val isGeminiSearching by viewModel.isGeminiSearching.collectAsState()
    val scrollState = rememberScrollState()

    val quickTags = listOf(
        "sunset", "dog", "red car", "receipt", "pasta", "beach"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Neural Search Engine",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Search across objects, locations, OCR text, or natural language",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search text field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { 
                    searchInput = it
                    // Also trigger local search instant query updates
                    viewModel.searchWithGeminiAI("") // Reset Gemini text
                },
                placeholder = { Text("Search 'dog in grass' or 'invoice'...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.6f)) },
                trailingIcon = {
                    if (searchInput.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchInput = ""
                        }) {
                            Icon(Icons.Default.Clear, "Clear text", tint = Color.White)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF60A5FA),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF111113),
                    focusedContainerColor = Color(0xFF111113)
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input")
            )

            // Dynamic Gemini AI Core Solver Trigger Button
            IconButton(
                onClick = { 
                    if (searchInput.isNotBlank()) {
                        viewModel.searchWithGeminiAI(searchInput)
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Ask Gemini AI Expert",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Tags labels
        Text(
            text = "SMART SUGGESTIONS",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickTags) { tag ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            searchInput = tag
                            viewModel.searchWithGeminiAI(tag)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = tag, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Gemini AI Analysis block
        AnimatedVisibility(
            visible = isGeminiSearching || geminiResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                backgroundColor = Color(0xFF1E293B).copy(alpha = 0.35f), // Slate theme
                borderColor = Color(0xFF334155).copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Vision AI Co-Pilot Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isGeminiSearching) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Gemini model 'gemini-3.5-flash' compiling visual index...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    } else if (geminiResult != null) {
                        Text(
                            text = geminiResult!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Live Grid Search Outputs
        Text(
            text = "MEDIA MATCHES",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (searchInput.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Type in the search or pick a tag to generate results.",
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (localResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No direct database matches. Try calling Gemini AI solver!",
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // Displays matches as a grid
            // Note: Since vertical Scroll is enabled on outer Layout, we use a custom Column wrapper instead of nesting grid sizes directly
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                localResults.chunked(3).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectMediaItem(item.id) }
                            ) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // Fill empty weight spacing if last row is incomplete
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
