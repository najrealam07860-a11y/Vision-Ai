package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.viewmodel.GalleryViewModel
import java.util.Locale

@Composable
fun PhotoEditorScreen(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val item by viewModel.editorMediaItem.collectAsState()
    val isSaving by viewModel.isEditorSaving.collectAsState()

    val exposure by viewModel.exposure.collectAsState()
    val saturation by viewModel.saturation.collectAsState()
    val contrast by viewModel.contrast.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val vignette by viewModel.vignette.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    var activeControlCategory by remember { mutableStateOf("LIGHT") } // LIGHT, COLOR, FILTERS

    val filters = listOf(
        "Original", "Vivid", "Cinematic", "Moody Noir", "Warm Autumn", "Cool Indigo", "Cyberpunk Violet"
    )

    if (item == null) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Beautiful flagship desaturated black background
            .testTag("photo_editor_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setEditorMedia(null) }) {
                    Icon(Icons.Default.Close, "Cancel changes", tint = Color.White)
                }

                Text(
                    text = "Professional Studio Lab",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(
                    onClick = { viewModel.saveEditorChanges() },
                    enabled = !isSaving
                ) {
                    Text(
                        text = "SAVE COPY",
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Central Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item!!.uri,
                    contentDescription = "Editing Canvas",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF60A5FA))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Blending filters and exporting image layers...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Adjustments Panel Footer
            Surface(
                color = Color(0xFF111113),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    
                    // Sliders and filters control area based on category selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        when (activeControlCategory) {
                            "LIGHT" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    EditorSlider(
                                        label = "Exposure",
                                        value = exposure,
                                        onValueChange = { viewModel.setExposure(it) },
                                        valueRange = -2f..2f
                                    )
                                    EditorSlider(
                                        label = "Contrast",
                                        value = contrast,
                                        onValueChange = { viewModel.setContrast(it) },
                                        valueRange = -2f..2f
                                    )
                                }
                            }
                            "COLOR" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    EditorSlider(
                                        label = "Saturation",
                                        value = saturation,
                                        onValueChange = { viewModel.setSaturation(it) },
                                        valueRange = -2f..2f
                                    )
                                    EditorSlider(
                                        label = "Kelvin Temperature",
                                        value = temperature,
                                        onValueChange = { viewModel.setTemperature(it) },
                                        valueRange = -2f..2f
                                    )
                                    EditorSlider(
                                        label = "Vignette Dark",
                                        value = vignette,
                                        onValueChange = { viewModel.setVignette(it) },
                                        valueRange = 0f..2f
                                    )
                                }
                            }
                            "FILTERS" -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(filters) { filter ->
                                        val isSelected = activeFilter == filter
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFF3B82F6) else Color(0xFF1E293B).copy(alpha = 0.5f)
                                                )
                                                .clickable { viewModel.setFilter(filter) }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = filter,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Mode toggler buttons: Light, Color, Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CategoryTabButton(
                            label = "Light Tone",
                            icon = Icons.Default.Brightness6,
                            isSelected = activeControlCategory == "LIGHT",
                            onClick = { activeControlCategory = "LIGHT" }
                        )

                        CategoryTabButton(
                            label = "Color Lab",
                            icon = Icons.Default.Palette,
                            isSelected = activeControlCategory == "COLOR",
                            onClick = { activeControlCategory = "COLOR" }
                        )

                        CategoryTabButton(
                            label = "Presets",
                            icon = Icons.Default.FilterList,
                            isSelected = activeControlCategory == "FILTERS",
                            onClick = { activeControlCategory = "FILTERS" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(
                text = String.format(Locale.getDefault(), "%+.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF60A5FA),
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF60A5FA),
                activeTrackColor = Color(0xFF60A5FA),
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun CategoryTabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.4f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
