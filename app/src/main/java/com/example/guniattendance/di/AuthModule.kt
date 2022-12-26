package com.example.guniattendance.di

import com.example.guniattendance.authorization.repository.AuthRepository
import com.example.guniattendance.authorization.repository.DefaultAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideAuthRepository() = DefaultAuthRepository() as AuthRepository

}