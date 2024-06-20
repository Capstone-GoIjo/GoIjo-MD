package com.example.goijo.core

import android.graphics.Bitmap

interface GoIjoRepo {
    fun classification(bitmap: Bitmap) : String
}