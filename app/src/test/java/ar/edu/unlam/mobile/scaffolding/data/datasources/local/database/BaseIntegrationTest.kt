package ar.edu.unlam.mobile.scaffolding.data.datasources.local.database

import org.junit.After
import org.junit.Before

abstract class BaseIntegrationTest {
    protected lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = RoomTestDatabase.build(AppDatabase::class.java)
    }

    @After
    fun tearDown() {
        database.close()
    }
}
