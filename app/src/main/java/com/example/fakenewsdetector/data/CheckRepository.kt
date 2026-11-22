package com.example.fakenewsdetector.data

import com.example.fakenewsdetector.ml.Classification
import kotlinx.coroutines.flow.Flow

class CheckRepository(private val dao: CheckDao) {

    val history: Flow<List<CheckEntity>> = dao.observeAll()

    suspend fun save(text: String, result: Classification) {
        dao.insert(
            CheckEntity(
                text = text,
                verdict = result.verdict.name,
                confidence = result.confidence,
                reason = result.reason,
                language = result.language.name
            )
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun clear() = dao.clear()
}
