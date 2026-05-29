package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.entity.Album
import com.example.viewmodel.GalleryViewModel
import java.util.*

@Composable
fun AlbumsTab(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    var activeAlbumId by remember { mutableStateOf<String?>(null) }
    var showCreateAlbumDialog by remember { mutableStateOf(false) }

    val visibleAlbums by viewModel.visibleAlbums.collectAsState()
    val allMedia by viewModel.visibleMedia.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        if (activeAlbumId == null) {
            // Primary Albums Overview Screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Gallery Collections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { showCreateAlbumDialog = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, "Create Album", tint = Color(0xFF60A5FA))
                }
            }

            if (visibleAlbums.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Albums. Create one using the blue plus button above.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleAlbums) { album ->
                        val count = allMedia.count { it.albumId == album.id || (album.isSmart && album.id == "favorites" && it.isFavorite) }
                        AlbumCard(
                            album = album,
                            computedCount = count,
                            onClick = { activeAlbumId = album.id }
                        )
                    }
                }
            }
        } else {
            // Inside Specific Album Screen
            val currentAlbum = visibleAlbums.firstOrNull { it.id == activeAlbumId }
            val albumMedia = allMedia.filter { 
                it.albumId == activeAlbumId || (currentAlbum?.isSmart == true && activeAlbumId == "favorites" && it.isFavorite) 
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeAlbumId = null }) {
                    Icon(Icons.Default.ArrowBack, "Back to Albums list", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = currentAlbum?.name ?: "Photos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${albumMedia.size} premium assets found",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            if (albumMedia.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoAlbum, "Empty Album", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This folder is currently empty.",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(albumMedia) { item ->
                        Box(
                            modifier = Modifier
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
                }
            }
        }
    }

    // Modal Popup dialog for Album creation
    if (showCreateAlbumDialog) {
        var albumNameInput by remember { mutableStateOf("") }
        var albumDescInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateAlbumDialog = false },
            title = { Text("Create Custom Album", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = albumNameInput,
                        onValueChange = { albumNameInput = it },
                        label = { Text("Album Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF87),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFF00FF87),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = albumDescInput,
                        onValueChange = { albumDescInput = it },
                        label = { Text("Description (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF87),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFF00FF87),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (albumNameInput.isNotBlank()) {
                            viewModel.createAlbum(albumNameInput, albumDescInput)
                            showCreateAlbumDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87))
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlbumDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }
}

@Composable
fun AlbumCard(
    album: Album,
    computedCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = album.coverUri,
                contentDescription = album.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic bottom gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Floating stats or icons
            if (album.isSmart) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xFF60A5FA).copy(alpha = 0.2f), CircleShape)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = if (album.id == "favorites") Icons.Default.Star else Icons.Default.AutoAwesome,
                        contentDescription = "Smart Grouping",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "$computedCount items • ${if (album.isSmart) "Smart AI System" else "User Collection"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
