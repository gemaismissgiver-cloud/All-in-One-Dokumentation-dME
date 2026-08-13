package com.example.data.db

import androidx.room.*
import com.example.data.model.ProtocolEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocol_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<ProtocolEntry>>

    @Query("SELECT * FROM protocol_entries WHERE dateStr = :dateStr ORDER BY timestamp DESC")
    fun getEntriesForDate(dateStr: String): Flow<List<ProtocolEntry>>

    @Query("SELECT * FROM protocol_entries WHERE chapter = :chapterId ORDER BY timestamp DESC")
    fun getEntriesForChapter(chapterId: String): Flow<List<ProtocolEntry>>

    @Query("SELECT * FROM protocol_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): ProtocolEntry?

    @Query("SELECT * FROM protocol_entries WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEntries(query: String): Flow<List<ProtocolEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ProtocolEntry): Long

    @Update
    suspend fun updateEntry(entry: ProtocolEntry)

    @Delete
    suspend fun deleteEntry(entry: ProtocolEntry)

    @Query("DELETE FROM protocol_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}
