package com.example.fakenewsdetector.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao {
    @Insert
    suspend fun insert(check: CheckEntity): Long

    @Query("SELECT * FROM checks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CheckEntity>>

    @Query("DELETE FROM checks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM checks")
    suspend fun clear()
}
