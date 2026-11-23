package com.example.fakenewsdetector

import com.example.fakenewsdetector.data.CheckDao
import com.example.fakenewsdetector.data.CheckEntity
import com.example.fakenewsdetector.data.HistoryRepository
import com.example.fakenewsdetector.data.RoomHistoryRepository
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** In-memory stand-in for Room so the repository logic runs on the JVM. */
private class FakeCheckDao : CheckDao {
    val rows = mutableListOf<CheckEntity>()
    private var nextId = 1L

    override suspend fun insert(check: CheckEntity): Long {
        val id = nextId++
        rows += check.copy(id = id)
        return id
    }

    override suspend fun restore(check: CheckEntity) {
        rows.removeAll { it.id == check.id }
        rows += check
    }

    override suspend fun update(check: CheckEntity) {
        rows.removeAll { it.id == check.id }
        rows += check
    }

    override fun observeAll(): Flow<List<CheckEntity>> =
        flowOf(rows.sortedWith(compareByDescending<CheckEntity> { it.createdAt }.thenByDescending { it.id }))

    override suspend fun getById(id: Long): CheckEntity? = rows.find { it.id == id }

    override suspend fun delete(id: Long) {
        rows.removeAll { it.id == id }
    }

    override suspend fun clear() {
        rows.clear()
    }

    override suspend fun trimTo(max: Int) {
        val keep = rows
            .sortedWith(compareByDescending<CheckEntity> { it.createdAt }.thenByDescending { it.id })
            .take(max)
            .map { it.id }
            .toSet()
        rows.removeAll { it.id !in keep }
    }
}

private fun result(index: Int, time: Long = index.toLong()) = CheckResult(
    text = "post $index",
    verdict = Verdict.UNVERIFIED,
    confidence = 0.4f,
    language = Language.ENGLISH,
    summary = "summary",
    reasons = listOf("reason"),
    checkedAt = time
)

class HistoryRepositoryTest {

    private fun repository(dao: FakeCheckDao) = RoomHistoryRepository(dao)

    @Test
    fun `save returns the stored id and persists fields`() = runBlocking {
        val dao = FakeCheckDao()
        val saved = repository(dao).save(result(1))
        assertNotNull(saved.storedId)
        assertEquals("post 1", dao.rows.single().text)
        assertEquals(Verdict.UNVERIFIED.name, dao.rows.single().verdict)
    }

    @Test
    fun `history is capped at 500 and drops the oldest`() = runBlocking {
        val dao = FakeCheckDao()
        val repo = repository(dao)
        for (index in 1..HistoryRepository.MAX_ENTRIES + 5) {
            repo.save(result(index, time = index.toLong()))
        }
        assertEquals(HistoryRepository.MAX_ENTRIES, dao.rows.size)
        assertNull(dao.rows.find { it.text == "post 1" })
        assertNotNull(dao.rows.find { it.text == "post 501" })
    }

    @Test
    fun `delete removes one and clear removes all`() = runBlocking {
        val dao = FakeCheckDao()
        val repo = repository(dao)
        val first = repo.save(result(1))
        repo.save(result(2))

        repo.delete(first.storedId!!)
        assertEquals(1, dao.rows.size)

        repo.clear()
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun `replace keeps the row id`() = runBlocking {
        val dao = FakeCheckDao()
        val repo = repository(dao)
        val saved = repo.save(result(1))
        val updated = saved.copy(verdict = Verdict.LIKELY_FAKE, confidence = 0.9f)

        val replaced = repo.replace(saved.storedId!!, updated)

        assertEquals(saved.storedId, replaced.storedId)
        assertEquals(Verdict.LIKELY_FAKE.name, dao.rows.single().verdict)
        assertEquals(1, dao.rows.size)
    }

    @Test
    fun `restore brings back a deleted row for undo`() = runBlocking {
        val dao = FakeCheckDao()
        val repo = repository(dao)
        val saved = repo.save(result(1))
        repo.delete(saved.storedId!!)
        assertTrue(dao.rows.isEmpty())

        repo.restore(saved)
        assertEquals(1, dao.rows.size)
        assertEquals(saved.storedId, dao.rows.single().id)
    }

    @Test
    fun `find returns null for unknown ids`() = runBlocking {
        val dao = FakeCheckDao()
        assertNull(repository(dao).find(42))
    }
}
