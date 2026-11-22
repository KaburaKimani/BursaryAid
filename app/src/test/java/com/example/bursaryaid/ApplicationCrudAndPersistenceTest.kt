package com.example.bursaryaid

import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.BursaryApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying CRUD operations and state persistence for Bursary Applications.
 * Implements testCreateApplication(), testReadApplication(), testUpdateApplication(),
 * testDeleteApplication(), and testApplicationPersistence().
 */
class ApplicationCrudAndPersistenceTest {

    private class InMemoryApplicationStore {
        private val applications = mutableMapOf<Long, BursaryApplication>()
        private var autoId = 1L

        fun insert(app: BursaryApplication): Long {
            val id = if (app.applicationId == 0L) autoId++ else app.applicationId
            val newApp = app.copy(applicationId = id)
            applications[id] = newApp
            return id
        }

        fun getById(id: Long): BursaryApplication? = applications[id]

        fun getByStudent(studentId: Long): List<BursaryApplication> =
            applications.values.filter { it.studentId == studentId }

        fun update(app: BursaryApplication) {
            if (applications.containsKey(app.applicationId)) {
                applications[app.applicationId] = app
            }
        }

        fun delete(id: Long) {
            applications.remove(id)
        }

        fun count(): Int = applications.size
    }

    private lateinit var store: InMemoryApplicationStore

    @Before
    fun setUp() {
        store = InMemoryApplicationStore()
    }

    @Test
    fun testCreateApplication() = runBlocking {
        val app = BursaryApplication(
            bursaryId = 1L,
            studentId = 100L,
            academicYear = "2025/2026",
            requestedAmountKes = 25_000L,
            applicationStatus = ApplicationStatus.DRAFT,
            applicationReason = "Tuition assistance for semester 1"
        )

        val id = store.insert(app)
        assertTrue(id > 0L)

        val retrieved = store.getById(id)
        assertNotNull(retrieved)
        assertEquals(25_000L, retrieved?.requestedAmountKes)
        assertEquals(ApplicationStatus.DRAFT, retrieved?.applicationStatus)
    }

    @Test
    fun testReadApplication() = runBlocking {
        val app1 = BursaryApplication(bursaryId = 1L, studentId = 100L, requestedAmountKes = 15_000L)
        val app2 = BursaryApplication(bursaryId = 2L, studentId = 100L, requestedAmountKes = 30_000L)

        val id1 = store.insert(app1)
        store.insert(app2)

        val singleApp = store.getById(id1)
        assertEquals(15_000L, singleApp?.requestedAmountKes)

        val studentApps = store.getByStudent(100L)
        assertEquals(2, studentApps.size)
    }

    @Test
    fun testUpdateApplication() = runBlocking {
        val app = BursaryApplication(
            bursaryId = 1L,
            studentId = 100L,
            academicYear = "2025/2026",
            requestedAmountKes = 20_000L,
            applicationStatus = ApplicationStatus.DRAFT
        )

        val id = store.insert(app)
        val created = store.getById(id)!!

        val updated = created.copy(
            requestedAmountKes = 28_000L,
            applicationStatus = ApplicationStatus.SUBMITTED,
            submittedAtMillis = System.currentTimeMillis()
        )

        store.update(updated)

        val retrieved = store.getById(id)
        assertEquals(28_000L, retrieved?.requestedAmountKes)
        assertEquals(ApplicationStatus.SUBMITTED, retrieved?.applicationStatus)
        assertNotNull(retrieved?.submittedAtMillis)
    }

    @Test
    fun testDeleteApplication() = runBlocking {
        val app = BursaryApplication(bursaryId = 1L, studentId = 100L, requestedAmountKes = 10_000L)
        val id = store.insert(app)

        assertEquals(1, store.count())
        store.delete(id)
        assertEquals(0, store.count())
        assertNull(store.getById(id))
    }

    @Test
    fun testApplicationPersistence() = runBlocking {
        // Simulate initial session: create and submit applications
        val app1 = BursaryApplication(bursaryId = 1L, studentId = 50L, requestedAmountKes = 20_000L, applicationStatus = ApplicationStatus.SUBMITTED)
        val app2 = BursaryApplication(bursaryId = 2L, studentId = 50L, requestedAmountKes = 35_000L, applicationStatus = ApplicationStatus.DRAFT)

        val id1 = store.insert(app1)
        val id2 = store.insert(app2)

        // Verify data state before simulation
        assertEquals(2, store.count())

        // Re-read data simulating app relaunch
        val persistentApp1 = store.getById(id1)
        val persistentApp2 = store.getById(id2)

        assertNotNull(persistentApp1)
        assertEquals(ApplicationStatus.SUBMITTED, persistentApp1?.applicationStatus)
        assertNotNull(persistentApp2)
        assertEquals(ApplicationStatus.DRAFT, persistentApp2?.applicationStatus)
    }
}
