package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.launch

@Database(
    entities = [MediaItem::class, Album::class, AIJob::class, SyncLog::class],
    version = 1,
    exportSchema = false
)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun albumDao(): AlbumDao
    abstract fun aiJobDao(): AIJobDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        @Volatile
        private var INSTANCE: GalleryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GalleryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GalleryDatabase::class.java,
                    "vision_gallery_database"
                )
                .addCallback(GalleryDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class GalleryDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.mediaDao(), database.albumDao(), database.syncLogDao())
                }
            }
        }

        private suspend fun populateInitialData(
            mediaDao: MediaDao,
            albumDao: AlbumDao,
            syncDao: SyncLogDao
        ) {
            // Seed Albums
            val initialAlbums = listOf(
                Album("camera", "Camera Roll", "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=500&auto=format&fit=crop&q=80", count = 4),
                Album("vacation", "Ibiza Trip", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop&q=80", count = 2),
                Album("documents", "Receipts & Work", "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=500&auto=format&fit=crop&q=80", count = 1),
                Album("favorites", "Starred Highlights", "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=500&auto=format&fit=crop&q=80", count = 2, isSmart = true),
                Album("screenshots", "Device Saves", "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&auto=format&fit=crop&q=80", count = 1, isSmart = true)
            )
            for (album in initialAlbums) {
                albumDao.insertAlbum(album)
            }

            // Seed MediaItems
            val currentTime = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            val initialMedia = listOf(
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080&auto=format&fit=crop&q=80",
                    title = "Glacier Lake at Dawn",
                    mimeType = "image/jpeg",
                    size = 1845120,
                    dateCreated = currentTime - (0.1f * dayMillis).toLong(),
                    dateModified = currentTime - (0.1f * dayMillis).toLong(),
                    width = 3840,
                    height = 2160,
                    albumId = "camera",
                    isFavorite = true,
                    latitude = 46.545,
                    longitude = 8.441,
                    cameraModel = "Pixel 8 Pro",
                    exposureTime = "1/120s",
                    aperture = "f/1.65",
                    iso = 50,
                    labels = "landscape, mountain, lake, snow, dawn, wilderness, reflections, nature",
                    cloudStatus = "BACKED_UP"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&auto=format&fit=crop&q=80",
                    title = "Sunny Coastline",
                    mimeType = "image/jpeg",
                    size = 2304918,
                    dateCreated = currentTime - (1.5f * dayMillis).toLong(),
                    dateModified = currentTime - (1.5f * dayMillis).toLong(),
                    width = 4032,
                    height = 3024,
                    albumId = "vacation",
                    isFavorite = true,
                    latitude = 38.907,
                    longitude = 1.431,
                    cameraModel = "Sony Alpha 7R V",
                    exposureTime = "1/1000s",
                    aperture = "f/4.0",
                    iso = 100,
                    focalLength = "24mm",
                    labels = "beach, sand, ocean, sunny, trip, tropical, vacation, water, island"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=1080&auto=format&fit=crop&q=80",
                    title = "Happy Beagle Pup",
                    mimeType = "image/jpeg",
                    size = 859385,
                    dateCreated = currentTime - (2.1f * dayMillis).toLong(),
                    dateModified = currentTime - (2.1f * dayMillis).toLong(),
                    width = 1920,
                    height = 1440,
                    albumId = "camera",
                    isFavorite = false,
                    latitude = 37.7749,
                    longitude = -122.4194,
                    cameraModel = "Pixel 8 Pro",
                    exposureTime = "1/250s",
                    aperture = "f/1.7",
                    iso = 200,
                    labels = "dog, beagle, pet, puppy, domestic, grass, animal"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=1080&auto=format&fit=crop&q=80",
                    title = "Red Sports Sedan",
                    mimeType = "image/jpeg",
                    size = 2950341,
                    dateCreated = currentTime - (3.8f * dayMillis).toLong(),
                    dateModified = currentTime - (3.8f * dayMillis).toLong(),
                    width = 5472,
                    height = 3648,
                    albumId = "camera",
                    isFavorite = false,
                    cameraModel = "Canon EOS R5",
                    exposureTime = "1/500s",
                    aperture = "f/2.8",
                    iso = 400,
                    focalLength = "85mm",
                    labels = "red car, vehicles, sedan, highway, supercharged, drive"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=1080&auto=format&fit=crop&q=80",
                    title = "Business Expense Receipt",
                    mimeType = "image/png",
                    size = 455201,
                    dateCreated = currentTime - (5.0f * dayMillis).toLong(),
                    dateModified = currentTime - (5.0f * dayMillis).toLong(),
                    width = 1200,
                    height = 1800,
                    albumId = "documents",
                    isFavorite = false,
                    labels = "document, paper, notes, statement, invoice, expense, receipt",
                    ocrText = "VISION CORP GROUP\nDate: May 12, 2026\n------------------\nInvoice ID: #39281\nDescription: Web Server Subscription\nSubtotal: $39.99\nTax (9%): $3.59\nTotal: $43.58 USD\nStatus: PAID VIA VISA\nSignature: APPROVED\nKeep this for your records."
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=1080&auto=format&fit=crop&q=80",
                    title = "Valley of Fire Sunset",
                    mimeType = "image/jpeg",
                    size = 3125432,
                    dateCreated = currentTime - (6.2f * dayMillis).toLong(),
                    dateModified = currentTime - (6.2f * dayMillis).toLong(),
                    width = 4000,
                    height = 2667,
                    albumId = "camera",
                    isFavorite = true,
                    latitude = 36.423,
                    longitude = -114.532,
                    cameraModel = "Fujifilm X-T5",
                    exposureTime = "1/60s",
                    aperture = "f/8.0",
                    iso = 160,
                    focalLength = "18mm",
                    labels = "sunset, desert, mountain, valleys, violet skies, rock formations, clouds",
                    cloudStatus = "BACKED_UP"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1080&auto=format&fit=crop&q=80",
                    title = "Studio Portrait Face",
                    mimeType = "image/jpeg",
                    size = 1450123,
                    dateCreated = currentTime - (8.1f * dayMillis).toLong(),
                    dateModified = currentTime - (8.1f * dayMillis).toLong(),
                    width = 3000,
                    height = 4500,
                    albumId = "vacation",
                    isFavorite = false,
                    cameraModel = "Sony Alpha 7 IV",
                    exposureTime = "1/160s",
                    aperture = "f/1.8",
                    iso = 200,
                    labels = "selfie, portrait, smile, face, girl, expression, eyes, studio",
                    cloudStatus = "BACKED_UP"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=1080&auto=format&fit=crop&q=80",
                    title = "Classic Italian Tomato Pasta",
                    mimeType = "image/jpeg",
                    size = 984122,
                    dateCreated = currentTime - (10.4f * dayMillis).toLong(),
                    dateModified = currentTime - (10.4f * dayMillis).toLong(),
                    width = 2400,
                    height = 3000,
                    albumId = "camera",
                    isFavorite = false,
                    labels = "food, pasta, dinner, recipe, meal, tomato, delicious, cooking"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1080&auto=format&fit=crop&q=80",
                    title = "Screenshot_2026-05-15-104922",
                    mimeType = "image/png",
                    size = 620581,
                    dateCreated = currentTime - (14.1f * dayMillis).toLong(),
                    dateModified = currentTime - (14.1f * dayMillis).toLong(),
                    width = 1080,
                    height = 2400,
                    albumId = "screenshots",
                    isFavorite = false,
                    labels = "screenshot, dashboard, chart, code, screen, tech, graphics, developer"
                )
            )

            mediaDao.insertMultipleMedia(initialMedia)

            syncDao.insertLog(SyncLog(title = "System Restored", details = "Prepopulated premium mock gallery content initialized flawlessly.", status = "SUCCESS"))
            syncDao.insertLog(SyncLog(title = "Cloud Handshake", details = "Offline mode sync channel established with local storage database indexes.", status = "SUCCESS"))
        }
    }
}
