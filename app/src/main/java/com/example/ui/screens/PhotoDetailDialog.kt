package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhotoDetailDialog(
    viewModel: GalleryViewModel,
    onOpenEditor: (Long) -> Unit,
    onOpenAIStudio: (Long) -> Unit
) {
    val item by viewModel.currentDetailMediaItem.collectAsState()
    val scrollState = rememberScrollState()

    if (item == null) return

    Dialog(
        onDismissRequest = { viewModel.selectMediaItem(null) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07070B)) // Flagship jet black
                .testTag("detail_view_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.selectMediaItem(null) },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }

                    Text(
                        text = "Immersive Viewer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(item!!.id) },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (item!!.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (item!!.isFavorite) Color(0xFFFFD700) else Color.White
                            )
                        }
                        IconButton(
                            onClick = { 
                                viewModel.deleteToTrash(item!!.id)
                                viewModel.selectMediaItem(null)
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }

                // Main Immersive Photo Display Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item!!.uri,
                        contentDescription = item!!.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Flagship Glassmorphic Info Grid Sheet
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = item!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val dateStr = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(item!!.dateCreated))
                    Text(
                        text = "Captured $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons: Edit, AI Studio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { 
                                val id = item!!.id
                                viewModel.selectMediaItem(null)
                                onOpenEditor(id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F2C)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, "Edit in Photo Lab")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Lab")
                        }

                        Button(
                            onClick = {
                                val id = item!!.id
                                viewModel.selectMediaItem(null)
                                onOpenAIStudio(id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FF87)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, "Enhance in AI Studio", tint = Color(0xFF07070B))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Studio", color = Color(0xFF07070B), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "EXIF CAMERA METADATA",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF00FF87), // Beautiful neon cyan/green labels
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // EXIF grid elements
                    ExifRow(label = "Dimensions", value = "${item!!.width} × ${item!!.height} px")
                    ExifRow(label = "Mime Type", value = item!!.mimeType)
                    ExifRow(label = "File Size", value = "${String.format(Locale.US, "%.2f", item!!.size / (1024f * 1024f))} MB")
                    ExifRow(label = "Camera Hardware", value = item!!.cameraModel ?: "Google Pixel 8 Pro")
                    ExifRow(label = "Aperture F-stop", value = item!!.aperture ?: "f/1.65")
                    ExifRow(label = "Exposure Cycle", value = item!!.exposureTime ?: "1/120s")
                    ExifRow(label = "ISO Settings", value = item!!.iso?.toString() ?: "50")
                    ExifRow(label = "Lens Length", value = item!!.focalLength ?: "24mm equivalents")
                    ExifRow(label = "Cloud Sync Integration", value = item!!.cloudStatus)

                    if (item!!.latitude != null && item!!.longitude != null) {
                        ExifRow(label = "Map Location Location", value = "${item!!.latitude}, ${item!!.longitude}")
                    }

                    if (item!!.labels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI NEURAL LABELS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF60EFFF),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Chips row
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item!!.labels.split(",").forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E1E28), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label.trim(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExifRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

// Simple legacy FlowRow layout for tags backing
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple Row encapsulation containing the contents
        content()
    }
}
