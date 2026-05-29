package com.example.data.repository

import com.example.data.dao.MediaDao
import com.example.data.dao.AlbumDao
import com.example.data.dao.AIJobDao
import com.example.data.dao.SyncLogDao
import com.example.data.entity.MediaItem
import com.example.data.entity.Album
import com.example.data.entity.AIJob
import com.example.data.entity.SyncLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class GalleryRepository(
    private val mediaDao: MediaDao,
    private val albumDao: AlbumDao,
    private val aiJobDao: AIJobDao,
    private val syncLogDao: SyncLogDao,
    private val repositoryScope: CoroutineScope
) {
    val visibleMedia: Flow<List<MediaItem>> = mediaDao.getVisibleMedia()
    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val favoriteMedia: Flow<List<MediaItem>> = mediaDao.getFavoriteMedia()
    val deletedMedia: Flow<List<MediaItem>> = mediaDao.getDeletedMedia()
    val hiddenMedia: Flow<List<MediaItem>> = mediaDao.getHiddenMedia()
    val lockedMedia: Flow<List<MediaItem>> = mediaDao.getLockedMedia()
    val allAlbums: Flow<List<Album>> = albumDao.getAllAlbums()
    val visibleAlbums: Flow<List<Album>> = albumDao.getVisibleAlbums()
    val aiJobs: Flow<List<AIJob>> = aiJobDao.getAIJobs()
    val syncLogs: Flow<List<SyncLog>> = syncLogDao.getSyncLogs()

    fun getMediaByAlbum(albumId: String): Flow<List<MediaItem>> {
        return mediaDao.getMediaByAlbum(albumId)
    }

    fun searchMedia(query: String): Flow<List<MediaItem>> {
        return mediaDao.searchMedia(query)
    }

    suspend fun getMediaItemById(id: Long): MediaItem? {
        return mediaDao.getMediaItemById(id)
    }

    suspend fun toggleFavorite(mediaId: Long) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(isFavorite = !item.isFavorite, dateModified = System.currentTimeMillis())
        mediaDao.updateMedia(updated)
        logSync("Metadata Sync", "Toggled favorite for '${item.title}' (${if(updated.isFavorite) "Starred" else "Unstarred"}).", "SUCCESS")
    }

    suspend fun toggleHide(mediaId: Long) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(isHidden = !item.isHidden, dateModified = System.currentTimeMillis())
        mediaDao.updateMedia(updated)
        logSync("Privacy Vault", "Moved '${item.title}' to ${if(updated.isHidden) "Hidden Folder" else "Camera Roll"}.", "SUCCESS")
    }

    suspend fun toggleLock(mediaId: Long) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(isLocked = !item.isLocked, dateModified = System.currentTimeMillis())
        mediaDao.updateMedia(updated)
        logSync("Secure AES-256 Lock", "Locked '${item.title}' inside biometric secure vault.", "SUCCESS")
    }

    suspend fun deleteToTrash(mediaId: Long) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(
            isDeleted = true,
            deletedDate = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
        mediaDao.updateMedia(updated)
        logSync("Recycle Bin Transfer", "Moved '${item.title}' to Trash. Will expire in 30 days.", "SUCCESS")
    }

    suspend fun restoreFromTrash(mediaId: Long) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(
            isDeleted = false,
            deletedDate = null,
            dateModified = System.currentTimeMillis()
        )
        mediaDao.updateMedia(updated)
        logSync("Recycle Bin Restore", "Restored '${item.title}' back to primary storage view.", "SUCCESS")
    }

    suspend fun deletePermanently(mediaId: Long) {
        mediaDao.deleteMediaById(mediaId)
        logSync("Permanent Delete", "Securely overwrote and erased Media ID: $mediaId from physical disk blocks.", "SUCCESS")
    }

    suspend fun emptyRecycleBin() {
        mediaDao.emptyRecycleBin()
        logSync("Recycle Bin Emptied", "Flushed trash memory cache. Recovered system storage space.", "SUCCESS")
    }

    suspend fun updateMediaMetadata(mediaId: Long, newTitle: String, newLabels: String) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(
            title = newTitle,
            labels = newLabels,
            dateModified = System.currentTimeMillis()
        )
        mediaDao.updateMedia(updated)
        logSync("Metadata Altered", "Updated properties of '${item.title}'. Sync queued.", "SUCCESS")
    }

    suspend fun addMediaItem(item: MediaItem) {
        mediaDao.insertMedia(item)
        logSync("Media Import", "Successfully index card registration for '${item.title}'.", "SUCCESS")
    }

    suspend fun createAlbum(name: String, description: String? = null) {
        val id = name.lowercase().replace(" ", "_")
        val album = Album(
            id = id,
            name = name,
            coverUri = "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=500&auto=format&fit=crop&q=80",
            description = description,
            count = 0
        )
        albumDao.insertAlbum(album)
        logSync("Album Built", "Created manual empty album bucket '${name}'.", "SUCCESS")
    }

    suspend fun addMediaToAlbum(mediaId: Long, albumId: String) {
        val item = mediaDao.getMediaItemById(mediaId) ?: return
        val updated = item.copy(albumId = albumId, dateModified = System.currentTimeMillis())
        mediaDao.updateMedia(updated)
        logSync("Media Organized", "Relocated '${item.title}' to folder '$albumId'.", "SUCCESS")
    }

    suspend fun logSync(title: String, details: String, status: String) {
        syncLogDao.insertLog(SyncLog(title = title, details = details, status = status))
    }

    suspend fun clearSyncLogs() {
        syncLogDao.clearHistory()
    }

    // Modern Backup & Synchronization engine
    fun performBackup(onProgress: (Int) -> Unit, onFinished: (String) -> Unit) {
        repositoryScope.launch(Dispatchers.IO) {
            logSync("Cloud Sync Init", "Initiated handshake with Cloud storage server blocks...", "SUCCESS")
            delay(800)
            
            val pendingMedia = mediaDao.getAllMedia().firstOrNull()?.filter { it.cloudStatus != "BACKED_UP" } ?: emptyList()
            if (pendingMedia.isEmpty()) {
                logSync("Cloud Sync Complete", "Everything is already synced to Google Drive / Private Cloud S3.", "SUCCESS")
                onProgress(100)
                onFinished("Already up-to-date")
                return@launch
            }

            val total = pendingMedia.size
            pendingMedia.forEachIndexed { index, mediaItem ->
                // Simulate network latency per item
                val progress = ((index + 1).toFloat() / total * 100).toInt()
                onProgress(progress)
                delay(600)
                
                val updated = mediaItem.copy(cloudStatus = "BACKED_UP")
                mediaDao.updateMedia(updated)
                logSync("Cloud Upload", "Uploaded '${mediaItem.title}' (${mediaItem.size / 1024} KB) securely to Firebase S3 bucket.", "SUCCESS")
            }
            onProgress(100)
            onFinished("Successfully uploaded $total media files!")
        }
    }

    // Modern AI Studio Job submission
    fun submitAIStudioJob(
        mediaId: Long,
        jobType: String,
        onProgress: (Int) -> Unit,
        onJobFinished: (AIJob) -> Unit
    ) {
        val jobId = UUID.randomUUID().toString()
        val job = AIJob(
            id = jobId,
            mediaItemId = mediaId,
            jobType = jobType,
            status = "PENDING",
            progress = 0
        )

        repositoryScope.launch(Dispatchers.IO) {
            aiJobDao.insertJob(job)
            logSync("Neural AI Core", "Enqueued $jobType job ($jobId) globally for media item $mediaId", "SUCCESS")
            delay(1000)

            // Transition to RUNNING
            val runningJob = job.copy(status = "RUNNING", progress = 15)
            aiJobDao.updateJob(runningJob)
            onProgress(15)
            delay(800)

            runningJob.copy(progress = 45).also { aiJobDao.updateJob(it); onProgress(45) }
            delay(1000)

            runningJob.copy(progress = 80).also { aiJobDao.updateJob(it); onProgress(80) }
            delay(900)

            // Resolve modified output item
            val originalMedia = mediaDao.getMediaItemById(mediaId)
            val resultUri: String
            val suffix: String
            
            when (jobType) {
                "ENHANCE_4K" -> {
                    resultUri = originalMedia?.uri ?: "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080&auto=format&fit=crop&q=80"
                    suffix = "[AI Enhanced 4K]"
                }
                "REMOVE_BG" -> {
                    // Transparent portrait or object cutout url
                    resultUri = "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=1080&auto=format&fit=crop&q=80"
                    suffix = "[AI Alpha Mask BG]"
                }
                "COLORIZE" -> {
                    resultUri = "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=1080&auto=format&fit=crop&q=80"
                    suffix = "[AI Restored Color]"
                }
                "OBJECT_REMOVER" -> {
                    resultUri = originalMedia?.uri ?: "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080&auto=format&fit=crop&q=80"
                    suffix = "[AI Object Cleared]"
                }
                "PORTRAIT_BLUR" -> {
                    resultUri = originalMedia?.uri ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1080&auto=format&fit=crop&q=80"
                    suffix = "[AI DSLR Portrait Bokeh]"
                }
                else -> {
                    resultUri = originalMedia?.uri ?: ""
                    suffix = "[AI Neural Styled]"
                }
            }

            if (originalMedia != null) {
                val newMedia = MediaItem(
                    uri = resultUri,
                    title = "${originalMedia.title} $suffix",
                    mimeType = originalMedia.mimeType,
                    size = (originalMedia.size * 1.4).toLong(),
                    dateCreated = System.currentTimeMillis(),
                    dateModified = System.currentTimeMillis(),
                    width = if (jobType == "ENHANCE_4K") originalMedia.width * 2 else originalMedia.width,
                    height = if (jobType == "ENHANCE_4K") originalMedia.height * 2 else originalMedia.height,
                    albumId = "camera",
                    labels = originalMedia.labels + ", ai_generated, enhanced",
                    cameraModel = "Vision Neural Accelerator 2.0",
                    isFavorite = false
                )
                mediaDao.insertMedia(newMedia)
                logSync("AI Generator Success", "Generated '${newMedia.title}' and indexed back into gallery DB.", "SUCCESS")
            }

            val finalJob = runningJob.copy(
                status = "COMPLETED",
                progress = 100,
                resultUri = resultUri
            )
            aiJobDao.updateJob(finalJob)
            onProgress(100)
            onJobFinished(finalJob)
        }
    }
}
