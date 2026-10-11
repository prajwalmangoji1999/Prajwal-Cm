package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraHelper {

    suspend fun processAndSaveGpsPhoto(
        context: Context,
        imageProxy: ImageProxy,
        employeeName: String,
        employeeId: String,
        gpsData: GpsLocationData,
        actionType: String // "LOGIN" or "LOGOUT"
    ): String = withContext(Dispatchers.IO) {
        val bitmap = imageProxyToBitmap(imageProxy)
        imageProxy.close()

        val stampedBitmap = stampGpsWatermark(
            bitmap = bitmap,
            employeeName = employeeName,
            employeeId = employeeId,
            gpsData = gpsData,
            actionType = actionType
        )

        // Save to internal storage
        val photoDir = File(context.filesDir, "photos").apply { mkdirs() }
        val filename = "ATTEND_${employeeId}_${System.currentTimeMillis()}.jpg"
        val photoFile = File(photoDir, filename)

        FileOutputStream(photoFile).use { out ->
            stampedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        photoFile.absolutePath
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val rotationDegrees = image.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        } else {
            originalBitmap
        }
    }

    fun stampGpsWatermark(
        bitmap: Bitmap,
        employeeName: String,
        employeeId: String,
        gpsData: GpsLocationData,
        actionType: String
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val width = mutableBitmap.width
        val height = mutableBitmap.height

        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date())

        // Calculate proportional sizes
        val baseFontSize = (height * 0.024f).coerceAtLeast(24f)
        val padding = baseFontSize * 0.8f

        // Draw dark translucent banner at bottom
        val bannerHeight = baseFontSize * 5.5f
        val bannerPaint = Paint().apply {
            color = Color.argb(190, 15, 23, 42) // Slate 900 translucent
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, height - bannerHeight, width.toFloat(), height.toFloat(), bannerPaint)

        // Action badge (Green for LOGIN, Rose for LOGOUT)
        val badgePaint = Paint().apply {
            color = if (actionType.contains("IN", ignoreCase = true)) Color.rgb(16, 185, 129) else Color.rgb(244, 63, 94)
            style = Paint.Style.FILL
        }
        val badgeRect = Rect(
            padding.toInt(),
            (height - bannerHeight + padding * 0.5f).toInt(),
            (padding + baseFontSize * 4.5f).toInt(),
            (height - bannerHeight + padding * 0.5f + baseFontSize * 1.2f).toInt()
        )
        canvas.drawRect(badgeRect, badgePaint)

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = baseFontSize * 0.75f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(
            if (actionType.contains("IN", ignoreCase = true)) "CHECK IN" else "CHECK OUT",
            padding + 12f,
            height - bannerHeight + padding * 0.5f + baseFontSize * 0.85f,
            badgeTextPaint
        )

        // Text paint
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = baseFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(4f, 1f, 1f, Color.BLACK)
        }

        val subTextPaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200
            textSize = baseFontSize * 0.82f
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        var currentY = height - bannerHeight + padding * 0.5f + baseFontSize * 2.2f
        canvas.drawText("$employeeName ($employeeId)", padding, currentY, textPaint)

        currentY += baseFontSize * 1.15f
        canvas.drawText("🕒 $formattedTime", padding, currentY, subTextPaint)

        currentY += baseFontSize * 1.15f
        val latStr = String.format(Locale.US, "%.5f", gpsData.latitude)
        val lngStr = String.format(Locale.US, "%.5f", gpsData.longitude)
        val gpsLine = "📍 Lat: $latStr, Lng: $lngStr | ${gpsData.address}"
        // Truncate if too long for screen width
        val displayGps = if (gpsLine.length > 55) gpsLine.substring(0, 52) + "..." else gpsLine
        canvas.drawText(displayGps, padding, currentY, subTextPaint)

        return mutableBitmap
    }
}
