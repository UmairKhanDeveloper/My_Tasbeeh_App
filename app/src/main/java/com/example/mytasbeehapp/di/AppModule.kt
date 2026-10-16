package com.example.mytasbeehapp.di

import android.content.Context
import com.example.mytasbeehapp.data.ThemeManager
import com.example.mytasbeehapp.data.repository.TasbeehRepository
import com.example.mytasbeehapp.data.repository.TasbeehRepositoryImpl
import com.example.mytasbeehapp.data.storage.TasbeehStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideThemeManager(@ApplicationContext context: Context): ThemeManager {
        return ThemeManager(context)
    }

    @Provides
    @Singleton
    fun provideTasbeehStorage(@ApplicationContext context: Context): TasbeehStorage {
        return TasbeehStorage(context)
    }

    @Provides
    @Singleton
    fun provideTasbeehRepository(storage: TasbeehStorage): TasbeehRepository {
        return TasbeehRepositoryImpl(storage)
    }
}