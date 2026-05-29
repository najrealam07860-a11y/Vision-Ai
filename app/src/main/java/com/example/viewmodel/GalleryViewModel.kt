package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.entity.AIJob
import com.example.data.entity.Album
import com.example.data.entity.MediaItem
import com.example.data.entity.SyncLog
import com.example.data.repository.GalleryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    // Bottom Navigation tab Selection
    private val _currentTab = MutableStateFlow("photos")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // List States from Repository
    val visibleMedia: StateFlow<List<MediaItem>> = repository.visibleMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMedia: StateFlow<List<MediaItem>> = repository.favoriteMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedMedia: StateFlow<List<MediaItem>> = repository.deletedMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenMedia: StateFlow<List<MediaItem>> = repository.hiddenMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lockedMedia: StateFlow<List<MediaItem>> = repository.lockedMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<Album>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleAlbums: StateFlow<List<Album>> = repository.visibleAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiJobs: StateFlow<List<AIJob>> = repository.aiJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncLogs: StateFlow<List<SyncLog>> = repository.syncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sub-category media selections within the Photos Tab
    private val _photosCategory = MutableStateFlow("ALL") // ALL, FAVORITES, VIDEOS, RAW, SCREENSHOTS
    val photosCategory: StateFlow<String> = _photosCategory.asStateFlow()

    fun setPhotosCategory(category: String) {
        _photosCategory.value = category
    }

    val filteredMedia: StateFlow<List<MediaItem>> = combine(visibleMedia, photosCategory) { media, cat ->
        when (cat) {
            "ALL" -> media
            "FAVORITES" -> media.filter { it.isFavorite }
            "VIDEOS" -> media.filter { it.mimeType.contains("video", ignoreCase = true) }
            "RAW" -> media.filter { it.mimeType.contains("raw", ignoreCase = true) || it.cameraModel?.contains("Canon", ignoreCase = true) == true }
            "SCREENSHOTS" -> media.filter { it.albumId == "screenshots" || it.title.contains("screenshot", ignoreCase = true) }
            else -> media
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Immersive display & detail viewer
    private val _selectedMediaId = MutableStateFlow<Long?>(null)
    val selectedMediaId: StateFlow<Long?> = _selectedMediaId.asStateFlow()

    val currentDetailMediaItem: StateFlow<MediaItem?> = combine(visibleMedia, selectedMediaId) { media, id ->
        if (id == null) null else media.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectMediaItem(id: Long?) {
        _selectedMediaId.value = id
    }

    // Multi Selection States
    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    fun toggleMultiSelect() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        _selectedIds.value = emptySet()
    }

    fun exitMultiSelect() {
        _isMultiSelectMode.value = false
        _selectedIds.value = emptySet()
    }

    fun toggleSelectItem(id: Long) {
        val currentSet = _selectedIds.value.toMutableSet()
        if (currentSet.contains(id)) {
            currentSet.remove(id)
        } else {
            currentSet.add(id)
        }
        _selectedIds.value = currentSet
        if (currentSet.isEmpty()) {
            _isMultiSelectMode.value = false
        }
    }

    // Multi items operations
    fun applyActionToSelected(action: String) {
        viewModelScope.launch {
            val ids = _selectedIds.value
            ids.forEach { id ->
                when (action) {
                    "FAVORITE" -> repository.toggleFavorite(id)
                    "TRASH" -> repository.deleteToTrash(id)
                    "HIDE" -> repository.toggleHide(id)
                    "LOCK" -> repository.toggleLock(id)
                }
            }
            exitMultiSelect()
        }
    }

    // Biometric PIN Vault Lock state
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    fun verifyVaultPin(pin: String): Boolean {
        if (pin == "1111") {
            _isVaultUnlocked.value = true
            _pinError.value = null
            return true
        } else {
            _pinError.value = "Incorrect Biometric Code pin (Try '1111')."
            return false
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    // Search Engine
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val localSearchResults: StateFlow<List<MediaItem>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchMedia(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Advanced Gemini Neural Search
    private val _geminiResult = MutableStateFlow<String?>(null)
    val geminiResult: StateFlow<String?> = _geminiResult.asStateFlow()

    private val _isGeminiSearching = MutableStateFlow(false)
    val isGeminiSearching: StateFlow<Boolean> = _isGeminiSearching.asStateFlow()

    fun searchWithGeminiAI(userQuery: String) {
        if (userQuery.isBlank()) return
        _searchQuery.value = userQuery
        _isGeminiSearching.value = true
        _geminiResult.value = null

        viewModelScope.launch {
            // Retrieve labels from existing photos to feed to Gemini as grounding context!
            val photos = visibleMedia.value
            val metadataContext = photos.joinToString("\n") { 
                "- Title: '${it.title}', Location: '${it.latitude ?: "N/A"}, ${it.longitude ?: "N/A"}', Camera: '${it.cameraModel ?: "N/A"}', Tags: [${it.labels}], OCR: '${it.ocrText}'"
            }

            val prompt = "The user is looking for this query: '$userQuery' inside their photo gallery database.\n" +
                    "Here is the database file structure index containing active photo cards with titles and labels:\n" +
                    metadataContext + "\n\n" +
                    "Analyze which photo matches best. Return a concise, high-end response detailing exactly which photo matches, why it matches, and highlight its tags."
            
            val systemInstruction = "You are the advanced 'Vision Gallery neural parser' running on-device for an AMOLED flagship Android phone gallery."
            
            val responseText = GeminiService.askGemini(prompt, systemInstruction)
            _geminiResult.value = responseText
            _isGeminiSearching.value = false
        }
    }

    // AI Studio State
    private val _aiSelectedMediaId = MutableStateFlow<Long?>(null)
    val aiSelectedMediaId: StateFlow<Long?> = _aiSelectedMediaId.asStateFlow()

    val aiSelectedMediaItem: StateFlow<MediaItem?> = combine(visibleMedia, aiSelectedMediaId) { media, id ->
        if (id == null) null else media.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectMediaForAI(id: Long?) {
        _aiSelectedMediaId.value = id
    }

    private val _aiProgress = MutableStateFlow(0)
    val aiProgress: StateFlow<Int> = _aiProgress.asStateFlow()

    private val _isAIProcessing = MutableStateFlow(false)
    val isAIProcessing: StateFlow<Boolean> = _isAIProcessing.asStateFlow()

    private val _aiFinishedJob = MutableStateFlow<AIJob?>(null)
    val aiFinishedJob: StateFlow<AIJob?> = _aiFinishedJob.asStateFlow()

    fun runAIStudioJob(jobType: String) {
        val mediaId = _aiSelectedMediaId.value ?: return
        _isAIProcessing.value = true
        _aiProgress.value = 0
        _aiFinishedJob.value = null

        repository.submitAIStudioJob(mediaId, jobType,
            onProgress = { progress ->
                _aiProgress.value = progress
            },
            onJobFinished = { job ->
                _isAIProcessing.value = false
                _aiFinishedJob.value = job
                // Refresh selection to enhanced output
                viewModelScope.launch {
                    val enhancedItem = visibleMedia.value.firstOrNull { it.title.contains("[AI") }
                    enhancedItem?.let { _aiSelectedMediaId.value = it.id }
                }
            }
        )
    }

    // Photo Editor State
    private val _editorMediaId = MutableStateFlow<Long?>(null)
    val editorMediaId: StateFlow<Long?> = _editorMediaId.asStateFlow()

    val editorMediaItem: StateFlow<MediaItem?> = combine(visibleMedia, _editorMediaId) { media, id ->
        if (id == null) null else media.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setEditorMedia(id: Long?) {
        _editorMediaId.value = id
        // Reset controls
        _exposure.value = 0f
        _saturation.value = 0f
        _contrast.value = 0f
        _temperature.value = 0f
        _vignette.value = 0f
        _activeFilter.value = "Original"
    }

    private val _exposure = MutableStateFlow(0f)
    val exposure: StateFlow<Float> = _exposure.asStateFlow()
    fun setExposure(v: Float) { _exposure.value = v }

    private val _saturation = MutableStateFlow(0f)
    val saturation: StateFlow<Float> = _saturation.asStateFlow()
    fun setSaturation(v: Float) { _saturation.value = v }

    private val _contrast = MutableStateFlow(0f)
    val contrast: StateFlow<Float> = _contrast.asStateFlow()
    fun setContrast(v: Float) { _contrast.value = v }

    private val _temperature = MutableStateFlow(0f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()
    fun setTemperature(v: Float) { _temperature.value = v }

    private val _vignette = MutableStateFlow(0f)
    val vignette: StateFlow<Float> = _vignette.asStateFlow()
    fun setVignette(v: Float) { _vignette.value = v }

    private val _activeFilter = MutableStateFlow("Original")
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()
    fun setFilter(name: String) { _activeFilter.value = name }

    private val _isEditorSaving = MutableStateFlow(false)
    val isEditorSaving: StateFlow<Boolean> = _isEditorSaving.asStateFlow()

    fun saveEditorChanges() {
        val item = editorMediaItem.value ?: return
        _isEditorSaving.value = true
        viewModelScope.launch {
            delay(1200) // Simulate drawing and filter render layer cycles
            
            // Build the updated title with the preset filter label
            val filterSuffix = if (_activeFilter.value == "Original") "[Edited]" else "[${_activeFilter.value} Filter]"
            val updatedItem = item.copy(
                title = "${item.title} $filterSuffix",
                size = (item.size * 1.05).toLong(), // Saved filter additions
                dateModified = System.currentTimeMillis()
            )
            repository.addMediaItem(updatedItem)
            _isEditorSaving.value = false
            _editorMediaId.value = null // Done editing
        }
    }

    // Cloud Backup Management
    private val _backupProgress = MutableStateFlow(0)
    val backupProgress: StateFlow<Int> = _backupProgress.asStateFlow()

    private val _isBackupRunning = MutableStateFlow(false)
    val isBackupRunning: StateFlow<Boolean> = _isBackupRunning.asStateFlow()

    private val _backupReport = MutableStateFlow<String?>(null)
    val backupReport: StateFlow<String?> = _backupReport.asStateFlow()

    fun triggerCloudSync() {
        _isBackupRunning.value = true
        _backupProgress.value = 0
        _backupReport.value = null
        repository.performBackup(
            onProgress = { progress ->
                _backupProgress.value = progress
            },
            onFinished = { report ->
                _isBackupRunning.value = false
                _backupReport.value = report
            }
        )
    }

    // Quick album interactions
    fun createAlbum(name: String, desc: String?) {
        viewModelScope.launch {
            repository.createAlbum(name, desc)
        }
    }

    fun associateMediaToAlbum(mediaId: Long, albumId: String) {
        viewModelScope.launch {
            repository.addMediaToAlbum(mediaId, albumId)
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun toggleHide(id: Long) {
        viewModelScope.launch {
            repository.toggleHide(id)
        }
    }

    fun toggleLock(id: Long) {
        viewModelScope.launch {
            repository.toggleLock(id)
        }
    }

    fun deleteToTrash(id: Long) {
        viewModelScope.launch {
            repository.deleteToTrash(id)
        }
    }

    fun restoreFromTrash(id: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            repository.deletePermanently(id)
        }
    }

    fun clearRecycleBin() {
        viewModelScope.launch {
            repository.emptyRecycleBin()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearSyncLogs()
        }
    }
}

class GalleryViewModelFactory(private val repository: GalleryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
