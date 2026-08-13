package com.example.data.repository

import com.example.data.db.ProtocolDao
import com.example.data.model.ProtocolEntry
import kotlinx.coroutines.flow.Flow

class ProtocolRepository(private val protocolDao: ProtocolDao) {
    val allEntries: Flow<List<ProtocolEntry>> = protocolDao.getAllEntries()

    fun getEntriesForDate(dateStr: String): Flow<List<ProtocolEntry>> =
        protocolDao.getEntriesForDate(dateStr)

    fun getEntriesForChapter(chapterId: String): Flow<List<ProtocolEntry>> =
        protocolDao.getEntriesForChapter(chapterId)

    fun searchEntries(query: String): Flow<List<ProtocolEntry>> =
        protocolDao.searchEntries(query)

    suspend fun getEntryById(id: Long): ProtocolEntry? =
        protocolDao.getEntryById(id)

    suspend fun insertEntry(entry: ProtocolEntry): Long =
        protocolDao.insertEntry(entry)

    suspend fun updateEntry(entry: ProtocolEntry) =
        protocolDao.updateEntry(entry)

    suspend fun deleteEntry(entry: ProtocolEntry) =
        protocolDao.deleteEntry(entry)

    suspend fun deleteById(id: Long) =
        protocolDao.deleteEntryById(id)
}
