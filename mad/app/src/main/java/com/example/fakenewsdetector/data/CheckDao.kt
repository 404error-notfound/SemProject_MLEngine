package com.example.fakenewsdetector.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao {
    @Insert
    suspend fun insert(check: CheckEntity): Long

    /** Undo re-inserts the original row with its original id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(check: CheckEntity)

    @Update
    suspend fun update(check: CheckEntity)

    @Query("SELECT * FROM checks ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<CheckEntity>>

    @Query("SELECT * FROM checks WHERE id = :id")
    suspend fun getById(id: Long): CheckEntity?

    @Query("DELETE FROM checks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM checks")
    suspend fun clear()

    /** History is capped: everything older than the newest [max] rows is dropped. */
    @Query(
        "DELETE FROM checks WHERE id NOT IN " +
            "(SELECT id FROM checks ORDER BY createdAt DESC, id DESC LIMIT :max)"
    )
    suspend fun trimTo(max: Int)
}
