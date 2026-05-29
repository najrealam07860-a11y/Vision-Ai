package com.example.data.dao

import androidx.room.*
import com.example.data.entity.MediaItem
import com.example.data.entity.Album
import com.example.data.entity.AIJob
import com.example.data.entity.SyncLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE isHidden = 0 AND isLocked = 0 AND isDeleted = 0 ORDER BY dateCreated DESC")
    fun getVisibleMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items ORDER BY dateCreated DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 AND isHidden = 0 AND isLocked = 0 AND isDeleted = 0 ORDER BY dateCreated DESC")
    fun getFavoriteMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isDeleted = 1 ORDER BY deletedDate DESC")
    fun getDeletedMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE albumId = :albumId AND isHidden = 0 AND isLocked = 0 AND isDeleted = 0 ORDER BY dateCreated DESC")
    fun getMediaByAlbum(albumId: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isHidden = 1 AND isDeleted = 0 ORDER BY dateCreated DESC")
    fun getHiddenMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isLocked = 1 AND isDeleted = 0 ORDER BY dateCreated DESC")
    fun getLockedMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaItemById(id: Long): MediaItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleMedia(items: List<MediaItem>)

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Delete
    suspend fun deleteMedia(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media_items WHERE isDeleted = 1")
    suspend fun emptyRecycleBin()

    // Query for semantic and search tags manually
    @Query("SELECT * FROM media_items WHERE (title LIKE '%' || :query || '%' OR labels LIKE '%' || :query || '%' OR ocrText LIKE '%' || :query || '%' OR albumId LIKE '%' || :query || '%') AND isHidden = 0 AND isLocked = 0 AND isDeleted = 0")
    fun searchMedia(query: String): Flow<List<MediaItem>>
}

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY isSmart DESC, name ASC")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE isHidden = 0 AND isLocked = 0 ORDER BY isSmart DESC, name ASC")
    fun getVisibleAlbums(): Flow<List<Album>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album)

    @Update
    suspend fun updateAlbum(album: Album)

    @Query("DELETE FROM albums WHERE id = :albumId")
    suspend fun deleteAlbum(albumId: String)
}

@Dao
interface AIJobDao {
    @Query("SELECT * FROM ai_jobs ORDER BY timestamp DESC")
    fun getAIJobs(): Flow<List<AIJob>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: AIJob)

    @Update
    suspend fun updateJob(job: AIJob)

    @Query("DELETE FROM ai_jobs WHERE id = :id")
    suspend fun deleteJob(id: String)
}

@Dao
interface SyncLogDao {
    @Query("SELECT * FROM sync_history ORDER BY timestamp DESC")
    fun getSyncLogs(): Flow<List<SyncLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SyncLog)

    @Query("DELETE FROM sync_history")
    suspend fun clearHistory()
}
