package com.example.goijo.core

import android.graphics.Bitmap
import javax.inject.Inject

class GoIjoRepoImpl @Inject constructor(private val models: GoIjoModels)
    : GoIjoRepo {

    override fun classification(bitmap: Bitmap) : String {
        return models.processTrashClassifications(bitmap)
    }
}