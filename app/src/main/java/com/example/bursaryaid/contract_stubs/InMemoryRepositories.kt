package com.example.bursaryaid.contract_stubs

import com.example.bursaryaid.data.model.ApplicationRepository
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.data.model.BursaryOpportunity
import com.example.bursaryaid.data.model.BursaryRepository
import com.example.bursaryaid.data.model.StudentProfile
import com.example.bursaryaid.data.model.StudentProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class InMemoryStudentProfileRepository : StudentProfileRepository {
    private val profiles = mutableMapOf<Long, StudentProfile>()
    private val nextId = AtomicLong(1L)

    override suspend fun getStudentProfile(userId: Long): StudentProfile? {
        return profiles[userId]
    }

    override suspend fun insertStudentProfile(profile: StudentProfile): Long {
        val id = nextId.getAndIncrement()
        val saved = profile.copy(studentId = id)
        profiles[profile.userId] = saved
        return id
    }

    override suspend fun updateStudentProfile(profile: StudentProfile) {
        profiles[profile.userId] = profile
    }
}

class InMemoryApplicationRepository : ApplicationRepository {
    private val applications = MutableStateFlow<List<BursaryApplication>>(emptyList())
    private val nextId = AtomicLong(1L)

    override suspend fun getApplicationById(applicationId: Long): BursaryApplication? {
        return applications.value.firstOrNull { it.applicationId == applicationId }
    }

    override fun getApplicationsByStudent(studentId: Long): Flow<List<BursaryApplication>> {
        return applications.map { list -> list.filter { it.studentId == studentId } }
    }

    override suspend fun insertApplication(application: BursaryApplication): Long {
        val id = nextId.getAndIncrement()
        val saved = application.copy(applicationId = id)
        applications.value = applications.value + saved
        return id
    }

    override suspend fun updateApplication(application: BursaryApplication) {
        applications.value = applications.value.map {
            if (it.applicationId == application.applicationId) application else it
        }
    }

    override suspend fun deleteApplication(applicationId: Long) {
        applications.value = applications.value.filter { it.applicationId != applicationId }
    }
}

class InMemoryBursaryRepository : BursaryRepository {
    private val sampleBursaries = listOf(
        BursaryOpportunity(
            bursaryId = 1L,
            bursaryName = "County Education Fund",
            fundingOrganization = "County Government",
            maximumAwardKes = 50_000L,
            applicationDeadlineMillis = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            isBursaryOpen = true
        ),
        BursaryOpportunity(
            bursaryId = 2L,
            bursaryName = "Women in STEM Scholarship",
            fundingOrganization = "STEM Foundation",
            maximumAwardKes = 80_000L,
            applicationDeadlineMillis = System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000,
            isBursaryOpen = true
        ),
        BursaryOpportunity(
            bursaryId = 3L,
            bursaryName = "Needy Student Support Grant",
            fundingOrganization = "Alumni Association",
            maximumAwardKes = 30_000L,
            applicationDeadlineMillis = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000,
            isBursaryOpen = true
        )
    )

    private val flow = MutableStateFlow(sampleBursaries)

    override fun getOpenBursaries(): Flow<List<BursaryOpportunity>> = flow
}
