package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object IconUtils {
    private val iconCache = LruCache<String, ImageBitmap>(500)

    fun drawableToImageBitmap(drawable: Drawable?, cacheKey: String? = null): ImageBitmap? {
        if (drawable == null) return null

        if (cacheKey != null) {
            val cached = iconCache.get(cacheKey)
            if (cached != null) return cached
        }

        val bitmap = try {
            if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                drawable.bitmap
            } else {
                val width = if (drawable.intrinsicWidth in 1..256) drawable.intrinsicWidth else 144
                val height = if (drawable.intrinsicHeight in 1..256) drawable.intrinsicHeight else 144
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
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

