package com.bnyro.clock.db

import android.content.Context
import androidx.room.Room

object DatabaseHolder {
    private const val dbName = "com.bnyro.clock"
    lateinit var instance: AppDatabase

    fun init(context: Context) {
        instance = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
    }
}
