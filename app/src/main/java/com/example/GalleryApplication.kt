package com.example

import android.app.Application
import com.example.data.database.GalleryDatabase
import com.example.data.repository.GalleryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class GalleryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { GalleryDatabase.getDatabase(this, applicationScope) }
    
    val repository by lazy {
        GalleryRepository(
            database.mediaDao(),
            database.albumDao(),
            database.aiJobDao(),
            database.syncLogDao(),
            applicationScope
        )
    }
}
