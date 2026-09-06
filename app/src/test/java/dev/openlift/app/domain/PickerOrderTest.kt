package dev.openlift.app.domain

import dev.openlift.app.data.local.entity.ExerciseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PickerOrderTest {

    private fun exercise(id: Long, name: String, group: String) =
        ExerciseEntity(id = id, name = name, muscleGroup = group, equipment = "DUMBBELL")

    @Test
    fun `group orders by canonical muscle order`() {
        val list = listOf(
            exercise(1, "Biceps Curl", "Biceps"),
            exercise(2, "Bench Press", "Chest"),
            exercise(3, "Back Squat", "Quads"),
            exercise(4, "Weird", "Unknown")
        )
        val groups = PickerOrder.group(list)
        assertEquals(listOf("Chest", "Quads", "Biceps", "Unknown"), groups.map { it.muscleGroup })
    }

    @Test
    fun `exercises within group are sorted by name`() {
        val list = listOf(
            exercise(1, "Zed", "Chest"),
            exercise(2, "Alpha", "Chest")
        )
        val group = PickerOrder.group(list).first()
        assertEquals(listOf("Alpha", "Zed"), group.exercises.map { it.name })
    }

    @Test
    fun `filter returns all when query blank`() {
        val list = listOf(exercise(1, "Bench Press", "Chest"))
        assertEquals(list, PickerOrder.filterLongestFirst("", list))
    }

    @Test
    fun `filter matches by name case-insensitive`() {
        val list = listOf(exercise(1, "Bench Press", "Chest"))
        assertEquals(1, PickerOrder.filterLongestFirst("bench", list).size)
        assertEquals(0, PickerOrder.filterLongestFirst("curl", list).size)
    }

    @Test
    fun `filter matches by muscle group`() {
        val list = listOf(exercise(1, "Press", "Chest"))
        assertEquals(1, PickerOrder.filterLongestFirst("chest", list).size)
    }
}
