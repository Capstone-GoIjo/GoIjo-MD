package com.example.goijo.core

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import com.example.goijo.core.Constant.IMAGE_SIZE

/**
 * Processes an image from a given Uri to prepare it for classification.
 * It creates a square thumbnail and scales it to the defined image size.
 *
 * @param context The context used to access the content resolver.
 * @param uri The Uri of the image to be processed.
 * @return A Bitmap of the processed image.
 */
object imageHelper {

    fun imageProcessing(context: Context, uri: Uri) : Bitmap{
        val imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val dimension = Integer.min(imageBitmap.width, imageBitmap.height)
        val thumbnail = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension)

        return Bitmap.createScaledBitmap(thumbnail, IMAGE_SIZE, IMAGE_SIZE, false)
    }
}