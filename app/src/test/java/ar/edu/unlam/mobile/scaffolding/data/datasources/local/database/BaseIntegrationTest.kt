package ar.edu.unlam.mobile.scaffolding.data.datasources.local.database

import org.junit.After
import org.junit.Before

/**
 * Base class for all database integration tests.
 * Manages the lifecycle of the in-memory ClinicsDataBase.
 */
abstract class BaseIntegrationTest {
    protected lateinit var database: ClinicsDataBase

    @Before
    fun setup() {
        database = RoomTestDatabase.build(ClinicsDataBase::class.java)
    }

    @After
    fun tearDown() {
        database.close()
    }
}
