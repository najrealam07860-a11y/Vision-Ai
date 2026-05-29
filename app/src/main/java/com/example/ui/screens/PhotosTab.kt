package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.entity.MediaItem
import com.example.ui.components.GlassmorphicCard
import com.example.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosTab(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val filteredMedia by viewModel.filteredMedia.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val currentCategory by viewModel.photosCategory.collectAsState()

    var showMemoriesDetails by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Clean Minimalism deep black background
    ) {
        // Memories section on top (Daily Recaps)
        if (!isMultiSelectMode) {
            Text(
                text = "My Memories",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            MemoriesSection(
                onMemoryClick = { memoryName ->
                    showMemoriesDetails = memoryName
                }
            )
        }

        // Subcategory pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("ALL", "FAVORITES", "VIDEOS", "RAW", "SCREENSHOTS")
            categories.forEach { cat ->
                val isSelected = currentCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                                    )
                                )
                            } else {
                                Modifier.background(Color(0xFF1E293B).copy(alpha = 0.5f))
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF60A5FA).copy(alpha = 0.3f) else Color(0xFF334155).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setPhotosCategory(cat) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filteredMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "No Photos",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No photos in this category.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // Photos Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("photos_grid")
            ) {
                items(filteredMedia, key = { it.id }) { item ->
                    val isSelected = selectedIds.contains(item.id)
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = {
                                    if (isMultiSelectMode) {
                                        viewModel.toggleSelectItem(item.id)
                                    } else {
                                        viewModel.selectMediaItem(item.id)
                                    }
                                },
                                onLongClick = {
                                    if (!isMultiSelectMode) {
                                        viewModel.toggleMultiSelect()
                                        viewModel.toggleSelectItem(item.id)
                                    }
                                }
                            )
                    ) {
                        AsyncImage(
                            model = item.uri,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Multi-selection Tint overlay
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF60A5FA).copy(alpha = 0.3f))
                            )
                        }

                        // Floating selectors
                        if (isMultiSelectMode) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.25f))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF050505),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Play indicator for videos
                        if (item.mimeType.contains("video", ignoreCase = true)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Favorite star on bottom-left
                        if (item.isFavorite && !isMultiSelectMode) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                                    .size(16.dp)
                                )
                        }
                    }
                }
            }
        }

        // Multi select actions floating bottom sheet
        AnimatedVisibility(
            visible = isMultiSelectMode,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                color = Color(0xFF16161C),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { viewModel.applyActionToSelected("FAVORITE") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.StarBorder, "Favorite", tint = Color.White)
                            Text("Star", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    IconButton(onClick = { viewModel.applyActionToSelected("LOCK") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, "Lock", tint = Color.White)
                            Text("Vault", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    IconButton(onClick = { viewModel.applyActionToSelected("HIDE") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VisibilityOff, "Hide", tint = Color.White)
                            Text("Hide", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    IconButton(onClick = { viewModel.applyActionToSelected("TRASH") }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Delete, "Trash", tint = Color.Red)
                            Text("Trash", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                        }
                    }
                    IconButton(onClick = { viewModel.exitMultiSelect() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Close, "Cancel", tint = Color.White)
                            Text("Cancel", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Story memory slideshow modal popup
    showMemoriesDetails?.let { memory ->
        MemorySlideshowDialog(
            memoryTitle = memory,
            onDismiss = { showMemoriesDetails = null }
        )
    }
}

@Composable
fun MemoriesSection(
    onMemoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleMemories = listOf(
        Pair("Ibiza Escapades", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80"),
        Pair("Alpine Heights", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500&auto=format&fit=crop&q=80"),
        Pair("Fluffy Friends", "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500&auto=format&fit=crop&q=80")
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sampleMemories) { memory ->
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onMemoryClick(memory.first) }
            ) {
                AsyncImage(
                    model = memory.second,
                    contentDescription = memory.first,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                Text(
                    text = memory.first,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun MemorySlideshowDialog(
    memoryTitle: String,
    onDismiss: () -> Unit
) {
    var tickIndex by remember { mutableStateOf(0) }
    val slides = when (memoryTitle) {
        "Ibiza Escapades" -> listOf(
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1080&auto=format&fit=crop&q=80"
        )
        "Alpine Heights" -> listOf(
            "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=1080&auto=format&fit=crop&q=80"
        )
        else -> listOf(
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=1080&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=1080&auto=format&fit=crop&q=80"
        )
    }

    // Auto advancing slideshow timer effect
    LaunchedEffect(key1 = tickIndex) {
        delay(2500)
        tickIndex = (tickIndex + 1) % slides.size
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Background slideshow
            AsyncImage(
                model = slides[tickIndex],
                contentDescription = "Slides",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Dynamic Gradient Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            // Timeline header bar indicator segments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                slides.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == tickIndex) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 28.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close slideshow", tint = Color.White)
            }

            // Captions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = memoryTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Vision AI Cinematic Compilation • Dynamic Slideshow",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
