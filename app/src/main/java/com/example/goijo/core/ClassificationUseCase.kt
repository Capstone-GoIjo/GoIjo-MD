package com.example.goijo.core

import android.graphics.Bitmap
import javax.inject.Inject

class ClassificationUseCase @Inject constructor(private val goRepo: GoIjoRepo) {

    operator fun invoke(bitmap: Bitmap): UIState<String> {
        return try {
            val labels = goRepo.classification(bitmap)
            UIState.Success(labels)
        } catch (e: Exception) {
            UIState.Failure(e.message ?: "An error occurred during classification.")
        }
    }
}
