package com.example.guniattendance.di

import com.example.guniattendance.student.repository.DefaultStudentRepository
import com.example.guniattendance.student.repository.StudentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StudentModule {

    @Singleton
    @Provides
    fun provideStudentRepository() = DefaultStudentRepository() as StudentRepository
}