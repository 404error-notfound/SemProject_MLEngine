package com.example.fakenewsdetector.data

import com.example.fakenewsdetector.domain.CheckResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persistence behind an interface so ViewModels can be tested with a fake and
 * the storage engine could be swapped later.
 */
interface HistoryRepository {
    val history: Flow<List<CheckResult>>

    /** Saves a new check and returns it with its new [CheckResult.storedId]. */
    suspend fun save(result: CheckResult): CheckResult

    /** Replaces a stored check in place, keeping its id. */
    suspend fun replace(id: Long, result: CheckResult): CheckResult

    /** Restores a deleted check (Undo). */
    suspend fun restore(result: CheckResult)

    suspend fun delete(id: Long)

    suspend fun clear()

    suspend fun find(id: Long): CheckResult?

    companion object {
        const val MAX_ENTRIES = 500
    }
}

class RoomHistoryRepository(
    private val dao: CheckDao,
    private val maxEntries: Int = HistoryRepository.MAX_ENTRIES
) : HistoryRepository {

    override val history: Flow<List<CheckResult>> =
        dao.observeAll().map { rows -> rows.map { it.toCheckResult() } }

    override suspend fun save(result: CheckResult): CheckResult {
        val existingId = result.storedId
        if (existingId != null) return replace(existingId, result)
        val id = dao.insert(result.toEntity())
        dao.trimTo(maxEntries)
        return result.copy(storedId = id)
    }

    override suspend fun replace(id: Long, result: CheckResult): CheckResult {
        val entity = result.copy(storedId = id).toEntity()
        dao.update(entity)
        dao.trimTo(maxEntries)
        return result.copy(storedId = id)
    }

    override suspend fun restore(result: CheckResult) {
        dao.restore(result.toEntity())
        dao.trimTo(maxEntries)
    }

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun clear() = dao.clear()

    override suspend fun find(id: Long): CheckResult? = dao.getById(id)?.toCheckResult()
}
