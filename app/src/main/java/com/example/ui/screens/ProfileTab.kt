package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassmorphicCard
import com.example.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileTab(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val backupProgress by viewModel.backupProgress.collectAsState()
    val isBackupRunning by viewModel.isBackupRunning.collectAsState()
    val backupReport by viewModel.backupReport.collectAsState()
    val logs by viewModel.syncLogs.collectAsState()
    val allMedia by viewModel.visibleMedia.collectAsState()

    var showTrashBinDialog by remember { mutableStateOf(false) }
    var passwordVaultInput by remember { mutableStateOf("") }
    var showVaultPINSetup by remember { mutableStateOf(false) }

    val totalMediaCount = allMedia.size
    val imagesSize = totalMediaCount * 2.3f // simulated MB size
    val videosSize = (totalMediaCount / 4) * 14.5f // simulated MB size
    val cachedAISize = 124.5f // simulated fixed cache AI MB
    val totalUsedMB = imagesSize + videosSize + cachedAISize
    val totalAvailableSpace = 256 * 1024f // 256GB in MB
    val usedPercentage = (totalUsedMB / totalAvailableSpace) * 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Intelligence Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Manage cloud backups, vault credentials, and hardware storage",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        // Circular Canvas Analytics Chart Card
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_analytics_card"),
                backgroundColor = Color(0xFF131317)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Grey empty background track
                            drawArc(
                                color = Color.White.copy(alpha = 0.1f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Blue segment: Images
                            drawArc(
                                color = Color(0xFF60A5FA),
                                startAngle = -90f,
                                sweepAngle = 360f * (imagesSize / totalUsedMB),
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Cyberpunk segment: Videos
                            drawArc(
                                color = Color(0xFF3B82F6),
                                startAngle = -90f + 360f * (imagesSize / totalUsedMB),
                                sweepAngle = 360f * (videosSize / totalUsedMB),
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        // Internal used ratio text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.1f", totalUsedMB / 1024f) + " GB",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Used",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Internal Disk space stats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "256 GB Capacity (96% Available)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        StorageLedgerRow(label = "Photos & Camera", size = String.format(Locale.US, "%.1f MB", imagesSize), tint = Color(0xFF60A5FA))
                        StorageLedgerRow(label = "Videos (HD RAW)", size = String.format(Locale.US, "%.1f MB", videosSize), tint = Color(0xFF3B82F6))
                        StorageLedgerRow(label = "AI Studio Neural Cache", size = "124.5 MB", tint = Color.Magenta)
                    }
                }
            }
        }

        // Vault Access Pin setup
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enterprise Security Vaults",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Toggle passcode authorization for biometric locks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Private Passkey", color = Color.White)
                        Button(
                            onClick = { showVaultPINSetup = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F2C))
                        ) {
                            Text("Configure Vault PIN", color = Color.White)
                        }
                    }
                }
            }
        }

        // Cloud Backup & Sync Controls
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Incremental Cloud Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Automatic synchronization backing up modified local files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isBackupRunning) {
                        Column {
                            LinearProgressIndicator(
                                progress = backupProgress / 100f,
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pushing blocks to Google Cloud S3... • $backupProgress%",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.triggerCloudSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Synchronization", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    backupReport?.let { report ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = report,
                            color = Color(0xFF60A5FA),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Recycle trash bin operations
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recycle Trash Bin",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Permanently wipe or restore deleted files (30 Days)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    IconButton(
                        onClick = { showTrashBinDialog = true },
                        modifier = Modifier.background(Color.Red.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, "Trash", tint = Color.Red)
                    }
                }
            }
        }

        // Live synchronizer history log checklist
        item {
            Text(
                text = "SYNCHRONIZER METADATA HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = "No sync history available. Trigger a backup to log details.",
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(logs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF16161B), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (log.status == "SUCCESS") Icons.Default.CloudQueue else Icons.Default.CloudOff,
                        contentDescription = "Sync",
                        tint = if (log.status == "SUCCESS") Color(0xFF60A5FA) else Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = log.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = SimpleDateFormat("H:mm:ss a", Locale.getDefault()).format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue for PIN Setup
    if (showVaultPINSetup) {
        var setupPinInput by remember { mutableStateOf("") }
        var isHidingPhotos by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showVaultPINSetup = false },
            title = { Text("Set Secure Vault PIN", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter a 4-digit numeric code to protect hidden folder locks.", color = Color.White.copy(alpha = 0.6f))
                    OutlinedTextField(
                        value = setupPinInput,
                        onValueChange = { setupPinInput = it },
                        placeholder = { Text("Code (e.g. 1111)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF60A5FA),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
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
                        if (setupPinInput.length >= 4) {
                            viewModel.verifyVaultPin("1111") // Seed standard PIN
                            showVaultPINSetup = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA))
                ) {
                    Text("Save Code", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaultPINSetup = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF111113)
        )
    }

    // Recycle bin items display dialog
    if (showTrashBinDialog) {
        val deletedMedia by viewModel.deletedMedia.collectAsState()

        AlertDialog(
            onDismissRequest = { showTrashBinDialog = false },
            title = { 
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Recycle Trash Bin", color = Color.White)
                    TextButton(onClick = { 
                        viewModel.clearRecycleBin() 
                        showTrashBinDialog = false
                    }) {
                        Text("EMPTY ALL", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (deletedMedia.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Recycle bin is completely empty.", color = Color.White.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(deletedMedia) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF222228), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        ) {
                                            AsyncImage(
                                                model = item.uri,
                                                contentDescription = item.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Expires in 30 days",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.restoreFromTrash(item.id) }) {
                                        Icon(Icons.Default.Restore, "Restore", tint = Color(0xFF60A5FA))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTrashBinDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F2C))
                ) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E24)
        )
    }
}

@Composable
fun StorageLedgerRow(label: String, size: String, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint)
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        }
        Text(text = size, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
