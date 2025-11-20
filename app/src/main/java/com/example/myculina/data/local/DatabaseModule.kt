package com.example.myculina.data.local

import android.content.Context
import androidx.room.Room

object DatabaseModule {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "myculina.db"
            ).build().also { INSTANCE = it }
        }
}

