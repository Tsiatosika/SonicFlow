package com.example.sonicflow

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
@HiltAndroidApp
class SonicFlowApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("SonicFlowApp", "Application démarrée avec succès")
    }


    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50 MB de cache
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}