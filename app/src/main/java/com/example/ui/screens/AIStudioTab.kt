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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.entity.MediaItem
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PremiumGlowingButton
import com.example.viewmodel.GalleryViewModel
import java.util.*

@Composable
fun AIStudioTab(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val allMedia by viewModel.visibleMedia.collectAsState()
    val selectedItem by viewModel.aiSelectedMediaItem.collectAsState()
    val isAIProcessing by viewModel.isAIProcessing.collectAsState()
    val aiProgress by viewModel.aiProgress.collectAsState()
    val finishedJob by viewModel.aiFinishedJob.collectAsState()
    val scrollState = rememberScrollState()

    var selectedJobType by remember { mutableStateOf("ENHANCE_4K") }
    var beforeAndAfterToggle by remember { mutableStateOf(false) }

    val jobTypes = listOf(
        Triple("ENHANCE_4K", "4K Ultra HD Upscaler", "Super-resolution restoration upscales, sharpens edges, and removes color grain artifacts natively."),
        Triple("REMOVE_BG", "Alpha BG Extractor", "Segment and isolate the foreground subject into a transparent visual cutout layout PNG."),
        Triple("PORTRAIT_BLUR", "DSLR Bokeh Portrait", "Creates high-end focal field gradients mimicking premium 85mm f/1.2 optical blur."),
        Triple("COLORIZE", "Vintage B&W Colorizer", "Deep-learning generative model introduces natural realistic color layers back to monochrome imagery.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "AI Studio Labs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Flagship Neural Core accelerator algorithms running natively",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Selected media preview container
        if (selectedItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111113))
                    .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.PhotoLibrary, "Library", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pick a photo from the roll below to start neural edit",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = if (beforeAndAfterToggle && finishedJob != null) selectedItem!!.uri else selectedItem!!.uri,
                    contentDescription = "Working Area",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Before and After indicator badge
                if (finishedJob != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .clickable { beforeAndAfterToggle = !beforeAndAfterToggle }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (beforeAndAfterToggle) "Display Enhanced [AI Output]" else "Display Original Source [RAW]",
                            color = Color(0xFF60A5FA),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Linear scrollable Photo Roll index for fast selection
        Text(
            text = "SELECT PHOTO",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allMedia) { item ->
                val isSelected = selectedItem?.id == item.id
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            brush = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectMediaForAI(item.id) }
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

        Spacer(modifier = Modifier.height(24.dp))

        // Actions selector blocks
        Text(
            text = "CHOOSE NEURAL MODE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        jobTypes.forEach { jobInfo ->
            val isSelected = selectedJobType == jobInfo.first
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { selectedJobType = jobInfo.first },
                backgroundColor = if (isSelected) Color(0xFF1E293B).copy(alpha = 0.35f) else Color(0xFF111113),
                borderColor = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFF1E293B).copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedJobType = jobInfo.first },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF60A5FA))
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = jobInfo.second,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = jobInfo.third,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar container
        if (isAIProcessing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = aiProgress / 100f,
                    color = Color(0xFF60A5FA),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Vision Core Accelerating Synthesis • $aiProgress%",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (finishedJob != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, "Finished", tint = Color(0xFF60A5FA))
                Column {
                    Text(
                        text = "AI Render Cycle Completed",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "The enhanced 4K asset has been successfully created and saved in the local albums.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action submit button
        PremiumGlowingButton(
            text = if (isAIProcessing) "Processing Core Synthesis..." else "Initialize Neural Synthesis",
            onClick = {
                if (selectedItem != null && !isAIProcessing) {
                    viewModel.runAIStudioJob(selectedJobType)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            glowColor = Color(0xFF60A5FA)
        )
    }
}
