package com.example.goijo.core

import android.graphics.Bitmap
import android.util.Log
import com.example.goijo.ml.GoIjo
import com.example.goijo.core.Constant.BATCH_SIZE
import com.example.goijo.core.Constant.CHANNEL
import com.example.goijo.core.Constant.IMAGE_SIZE
import com.example.goijo.core.Constant.LABELS_CLASS
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class GoIjoModels @Inject constructor(private val goIjo: GoIjo) {

    fun processTrashClassifications(bitmap: Bitmap): String {
        return try {
            val inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(BATCH_SIZE, IMAGE_SIZE, IMAGE_SIZE, CHANNEL), DataType.FLOAT32
            )
            val byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * CHANNEL)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(IMAGE_SIZE * IMAGE_SIZE)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            var pixel = 0
            for (i in 0 until IMAGE_SIZE) {
                for (j in 0 until IMAGE_SIZE) {
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16) and 0xFF) * (1.0f / 255.0f))
                    byteBuffer.putFloat(((value shr 8) and 0xFF) * (1.0f / 255.0f))
                    byteBuffer.putFloat((value and 0xFF) * (1.0f / 255.0f))
                }
            }

            inputFeature0.loadBuffer(byteBuffer)

            val outputs = goIjo.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            val confidences = outputFeature0.floatArray
            var maxPos = 0
            var maxConfidence = 0.0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }

            LABELS_CLASS[maxPos]
        } catch (e: Exception) {
            Log.e(TAG, "processTrashClassifications: ${e.message}")
            e.message ?: "Error during classification."
        }
    }

    companion object {
        private const val TAG = "GoIjoModels"
    }
}
