package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GalleryViewModel
import com.example.viewmodel.GalleryViewModelFactory
import com.example.ui.screens.PhotosTab
import com.example.ui.screens.AlbumsTab
import com.example.ui.screens.SearchTab
import com.example.ui.screens.AIStudioTab
import com.example.ui.screens.ProfileTab
import com.example.ui.screens.PhotoDetailDialog
import com.example.ui.screens.PhotoEditorScreen

class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels {
        GalleryViewModelFactory((application as GalleryApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support configuration
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) { // Immersive flagship OLED Dark room
                val currentTab by viewModel.currentTab.collectAsState()
                
                // Track detail viewer and editors layers
                val selectedMediaId by viewModel.selectedMediaId.collectAsState()
                val editorMediaId by viewModel.editorMediaId.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        GalleryTopBar(currentTab)
                    },
                    bottomBar = {
                        GalleryBottomNavigationBar(
                            selectedTab = currentTab,
                            onTabSelected = { tabName ->
                                viewModel.selectTab(tabName)
                            }
                        )
                    },
                    containerColor = Color(0xFF050505) // Clean Minimalism Deep pitch black
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Content tabs switcher with transition reveals
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "TabTransitions"
                        ) { targetTab ->
                            when (targetTab) {
                                "photos" -> PhotosTab(viewModel = viewModel)
                                "albums" -> AlbumsTab(viewModel = viewModel)
                                "search" -> SearchTab(viewModel = viewModel)
                                "aistudio" -> AIStudioTab(viewModel = viewModel)
                                "profile" -> ProfileTab(viewModel = viewModel)
                            }
                        }

                        // Detail immersive overlay layer
                        if (selectedMediaId != null) {
                            PhotoDetailDialog(
                                viewModel = viewModel,
                                onOpenEditor = { id ->
                                    viewModel.setEditorMedia(id)
                                },
                                onOpenAIStudio = { id ->
                                    viewModel.selectMediaForAI(id)
                                    viewModel.selectTab("aistudio")
                                }
                            )
                        }

                        // Professional editing studio layer
                        if (editorMediaId != null) {
                            PhotoEditorScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryTopBar(currentTab: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF050505))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Vision",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF60A5FA)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            val subtitleText = when (currentTab) {
                "photos" -> "12,408 items • Syncing"
                "albums" -> "Smart Bookmarks"
                "search" -> "AI Natural Search"
                "aistudio" -> "Studio Accelerator"
                "profile" -> "System Workspace"
                else -> "Cloud Connected"
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8) // Slate 400
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Studio Action",
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun GalleryBottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("photos", "Photos", Icons.Default.Image),
        Triple("albums", "Albums", Icons.Default.FolderOpen),
        Triple("search", "Search", Icons.Default.Search),
        Triple("aistudio", "AI Studio", Icons.Default.AutoAwesome),
        Triple("profile", "Profile", Icons.Default.AccountCircle)
    )

    NavigationBar(
        containerColor = Color(0xFF050505), // Matte pitch-black minimalist background
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF1E293B).copy(alpha = 0.5f), // subtle slate boundary
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .testTag("app_bottom_bar"),
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = selectedTab == item.first
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.first) },
                icon = {
                    Icon(
                        imageVector = item.third,
                        contentDescription = item.second,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.second,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF60A5FA), // Sleek sky blue
                    selectedTextColor = Color(0xFF60A5FA),
                    indicatorColor = Color(0xFF1E293B).copy(alpha = 0.8f), // elegant slate inactive pill background
                    unselectedIconColor = Color(0xFF94A3B8).copy(alpha = 0.45f),
                    unselectedTextColor = Color(0xFF94A3B8).copy(alpha = 0.45f)
                )
            )
        }
    }
}
