package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object IconUtils {
    private val iconCache = LruCache<String, ImageBitmap>(800)

    fun drawableToImageBitmap(drawable: Drawable?, cacheKey: String? = null): ImageBitmap? {
        if (drawable == null) return null

        if (cacheKey != null) {
            val cached = iconCache.get(cacheKey)
            if (cached != null) return cached
        }

        val targetSize = 144
        val bitmap = try {
            if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                val src = drawable.bitmap
                if (src.width <= targetSize && src.height <= targetSize) {
                    src
                } else {
                    Bitmap.createScaledBitmap(src, targetSize, targetSize, true)
                }
            } else {
                val bmp = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, targetSize, targetSize)
                drawable.draw(canvas)
                bmp
            }
        } catch (e: Exception) {
            null
        }

        val imageBitmap = bitmap?.asImageBitmap()
        if (imageBitmap != null && cacheKey != null) {
            iconCache.put(cacheKey, imageBitmap)
        }
        return imageBitmap
    }
}

