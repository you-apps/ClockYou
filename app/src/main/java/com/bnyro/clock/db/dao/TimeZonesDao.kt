package com.bnyro.clock.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bnyro.clock.obj.TimeZone

@Dao
interface TimeZonesDao {
    @Query("SELECT * FROM timeZones")
    suspend fun getAll(): List<TimeZone>

    @Insert
    suspend fun insertAll(vararg timeZone: TimeZone)

    @Delete
    suspend fun delete(timeZone: TimeZone)

    @Query("DELETE FROM timeZones")
    suspend fun clear()
}
