package com.example.apexfitness.ui.body

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Progress photos are kept on the phone only (in the app's private storage), one folder per user.
// They are never uploaded anywhere.
object ProgressPhotoStore {
    private const val MAX_SIZE = 1600

    private fun userDir(context: Context, uid: String): File =
        File(context.filesDir, "progress_photos/$uid").apply { mkdirs() }

    // Newest first. The file name is the time the photo was added, so sorting by name works.
    fun list(context: Context, uid: String): List<File> =
        userDir(context, uid).listFiles()
            ?.filter { it.extension == "jpg" && it.length() > 0 }
            ?.sortedByDescending { it.name }
            ?: emptyList()

    fun dateMillis(file: File): Long = file.nameWithoutExtension.toLongOrNull() ?: file.lastModified()

    fun delete(file: File) {
        file.delete()
    }

    // Used when the account is deleted
    fun deleteAll(context: Context) {
        File(context.filesDir, "progress_photos").deleteRecursively()
    }

    // A temporary file for the camera to write into, plus the Uri to hand to the camera app
    fun cameraTempFile(context: Context): File {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        // Some camera apps need the file to exist before they can write into it
        return File(dir, "capture.jpg").apply { if (!exists()) createNewFile() }
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    // Reads a picked or captured photo, fixes its rotation, shrinks it and saves it in the app's storage
    suspend fun saveFromUri(context: Context, uid: String, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, MAX_SIZE)
            }
            val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return@withContext null

            val rotation = resolver.openInputStream(uri)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

            val upright = if (rotation != 0f) {
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(rotation) }, true)
            } else {
                decoded
            }

            val target = File(userDir(context, uid), "${System.currentTimeMillis()}.jpg")
            target.outputStream().use { upright.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            target
        } catch (e: Exception) {
            null
        }
    }

    // Loads a small version of a saved photo for the screen
    suspend fun loadBitmap(file: File, maxSize: Int): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSize)
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            null
        }
    }

    // Powers of two are the sizes the decoder handles best
    private fun sampleSizeFor(width: Int, height: Int, maxSize: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= maxSize || height / (sample * 2) >= maxSize) sample *= 2
        return sample
    }
}
