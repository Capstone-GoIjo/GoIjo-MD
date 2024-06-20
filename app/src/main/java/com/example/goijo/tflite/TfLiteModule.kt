package com.example.goijo.tflite

import android.content.Context
import com.example.goijo.ml.GoIjo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TfLiteModule {
    @Provides
    fun providesGoIjo(@ApplicationContext context: Context) = GoIjo.newInstance(context)
}