package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val title: String,
    val mimeType: String,
    val size: Long,
    val dateCreated: Long,
    val dateModified: Long,
    val width: Int,
    val height: Int,
    val albumId: String = "camera",
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedDate: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cameraModel: String? = null,
    val exposureTime: String? = null,
    val aperture: String? = null,
    val iso: Int? = null,
    val focalLength: String? = null,
    val labels: String = "", // Comma-separated AI tags (e.g., "sunset, landscape, mountain")
    val ocrText: String = "", // Extracted text inside image
    val cloudStatus: String = "NOT_BACKED_UP" // NOT_BACKED_UP, SYNCING, SPLENDID_CLOUD
)

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val id: String,
    val name: String,
    val coverUri: String,
    val isLocked: Boolean = false,
    val isSmart: Boolean = false,
    val isHidden: Boolean = false,
    val description: String? = null,
    val count: Int = 0
)

@Entity(tableName = "ai_jobs")
data class AIJob(
    @PrimaryKey val id: String,
    val mediaItemId: Long,
    val jobType: String, // "ENHANCE_4K", "REMOVE_BG", "OBJECT_REMOVER", "PORTRAIT_BLUR", "COLORIZE", "CARTOON"
    val status: String, // "PENDING", "RUNNING", "COMPLETED", "FAILED"
    val progress: Int = 0, // 0 - 100
    val resultUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_history")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "SUCCESS", "FAILED"
)
