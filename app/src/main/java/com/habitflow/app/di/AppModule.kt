package com.habitflow.app.di

import android.content.Context
import androidx.room.Room
import com.habitflow.app.data.local.HabitDatabase
import com.habitflow.app.data.local.HabitDao
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.data.repository.HabitRepositoryImpl
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
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitRepository(habitDao: HabitDao): HabitRepository {
        return HabitRepositoryImpl(habitDao)
    }
}
