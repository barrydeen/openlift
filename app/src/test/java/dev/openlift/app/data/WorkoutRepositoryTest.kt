package dev.openlift.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.openlift.app.data.local.AppDatabase
import dev.openlift.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkoutRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: WorkoutRepository
    private var benchId: Long = 0L
    private var curlId: Long = 0L

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.inMemory(context)
        repo = WorkoutRepository(db.workoutDao(), db.exerciseDao())
        val ids = db.exerciseDao().insertAll(
            listOf(
                ExerciseEntity(name = "Bench Press", muscleGroup = "Chest", equipment = "BARBELL"),
                ExerciseEntity(name = "Curl", muscleGroup = "Biceps", equipment = "DUMBBELL")
            )
        )
        benchId = ids[0]
        curlId = ids[1]
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `addExercise seeds placeholder sets from previous session`() = runBlocking {
        val w1 = repo.createWorkout("A")
        val entry1 = repo.addExercise(w1, benchId)
        repo.addSet(entry1, 60.0, 10)
        repo.addSet(entry1, 80.0, 8)
        repo.finishWorkout(w1)

        val template = repo.previousSessionTemplate(benchId)
        assertEquals(2, template.size)

        val w2 = repo.createWorkout("B")
        val entry2 = repo.addExercise(w2, benchId)
        val sets = db.workoutDao().setsFor(entry2)
        assertEquals(2, sets.size)
        assertEquals(60.0, sets[0].weightKg, 0.001)
        assertEquals(10, sets[0].reps)
        assertEquals(80.0, sets[1].weightKg, 0.001)
        assertEquals(8, sets[1].reps)
        assertNull("seeded sets are drafts (not complete)", sets[0].completedAt)
    }

    @Test
    fun `finishWorkout marks endedAt and clears active`() = runBlocking {
        val id = repo.createWorkout("A")
        assertNotNull(repo.observeActiveWorkout().first())
        repo.finishWorkout(id)
        assertNull(repo.observeActiveWorkout().first())
        val finished = db.workoutDao().byId(id)
        assertNotNull(finished!!.endedAt)
    }

    @Test
    fun `history summaries compute volume and counts`() = runBlocking {
        val id = repo.createWorkout("A")
        val entry = repo.addExercise(id, benchId)
        repo.addSet(entry, 100.0, 5)
        repo.addSet(entry, 40.0, 10, isWarmup = true)
        repo.finishWorkout(id)

        val summaries = repo.observeHistorySummaries().first()
        assertEquals(1, summaries.size)
        assertEquals(100.0 * 5, summaries[0].totalKg, 0.001)
        assertEquals(1, summaries[0].setCount)
        assertEquals(1, summaries[0].exerciseCount)
    }

    @Test
    fun `setsExerciseHistory returns only completed sets grouped by workout`() = runBlocking {
        val w1 = repo.createWorkout("A")
        repo.addSet(repo.addExercise(w1, benchId), 100.0, 5)
        repo.finishWorkout(w1)

        val w2 = repo.createWorkout("B")
        repo.addSet(repo.addExercise(w2, benchId), 120.0, 3)
        repo.finishWorkout(w2)

        val history = repo.setsExerciseHistory(benchId)
        assertEquals(2, history.size)
        assertEquals(120.0, history[1].weightKg, 0.001)
    }

    @Test
    fun `deleteWorkout cascades`() = runBlocking {
        val id = repo.createWorkout("A")
        repo.addExercise(id, benchId)
        repo.deleteWorkout(id)
        assertEquals(0, repo.observeHistorySummaries().first().size)
    }

    @Test
    fun `removeExercise deletes its entry`() = runBlocking {
        val id = repo.createWorkout("A")
        val entry = repo.addExercise(id, benchId)
        repo.addSet(entry, 100.0, 5)
        repo.removeExercise(entry)
        assertEquals(0, db.workoutDao().setsFor(entry).size)
    }
}
