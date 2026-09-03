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

    fun getFallbackAppIcon(label: String, colorLong: Long): ImageBitmap {
        val key = "fallback_${label.take(1).uppercase()}_$colorLong"
        val cached = iconCache.get(key)
        if (cached != null) return cached

        val targetSize = 144
        val bmp = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = colorLong.toInt()
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, paint)

        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = targetSize * 0.44f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val charStr = label.take(1).uppercase()
        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(charStr, 0, charStr.length, bounds)
        val yPos = (targetSize / 2f) + (bounds.height() / 2f)
        canvas.drawText(charStr, targetSize / 2f, yPos, textPaint)

        val imgBitmap = bmp.asImageBitmap()
        iconCache.put(key, imgBitmap)
        return imgBitmap
    }
}

