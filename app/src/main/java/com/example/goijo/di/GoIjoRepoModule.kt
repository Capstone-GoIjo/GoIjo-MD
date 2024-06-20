package com.example.goijo.di

import com.example.goijo.core.GoIjoRepoImpl
import com.example.goijo.core.GoIjoRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GoIjoRepoModule {
    @Binds
    abstract fun bindGoIjoRepo(goIjoRepoImpl: GoIjoRepoImpl)
            : GoIjoRepo
}