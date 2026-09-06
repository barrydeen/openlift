package dev.openlift.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.openlift.app.data.local.AppDatabase
import dev.openlift.app.data.local.entity.Equipment
import dev.openlift.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExerciseRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: ExerciseRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.inMemory(context)
        repo = ExerciseRepository(db.exerciseDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `createCustom returns id and finds it on observeAll`() = runBlocking {
        val id = repo.createCustom("My Lift", "Chest", Equipment.DUMBBELL)
        assertTrue(id > 0)
        val all = repo.observeAll().first()
        assertTrue(all.any { it.id == id && it.name == "My Lift" && it.isCustom })
    }

    @Test
    fun `createCustom trims and defaults blank muscle group`() = runBlocking {
        val id = repo.createCustom("  Row  ", "", Equipment.BARBELL)
        val ex = repo.getById(id)
        assertEquals("Row", ex!!.name)
        assertEquals("Other", ex.muscleGroup)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createCustom rejects blank name`() {
        runBlocking { repo.createCustom("   ", "Chest", Equipment.DUMBBELL) }
    }

    @Test
    fun `search filters by name`() = runBlocking {
        val ids = db.exerciseDao().insertAll(
            listOf(ExerciseEntity(name = "Bench Press", muscleGroup = "Chest", equipment = "BARBELL"))
        )
        val results = repo.search("bench").first()
        assertEquals(1, results.size)
    }
}
