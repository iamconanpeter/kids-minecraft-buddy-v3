package com.iamconanpeter.kidsminecraftbuddyv3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KidsMinecraftBuddyEngineTest {

    @Test
    fun missionCompletionAwardsStarsAndAllowsAdvance() {
        val engine = KidsMinecraftBuddyEngine()

        completeMission(engine)

        val completed = engine.uiState()
        assertTrue(completed.missionComplete)
        assertEquals(1, completed.missionsCompleted)
        assertTrue(completed.totalStars >= 2)

        val currentIndex = completed.mission.index
        assertTrue(engine.advanceMission())

        val next = engine.uiState()
        assertFalse(next.missionComplete)
        assertEquals(currentIndex + 1, next.mission.index)
    }

    @Test
    fun longPressRemovesBlockAndLowersObjectiveProgress() {
        val engine = KidsMinecraftBuddyEngine()
        val firstCell = engine.uiState().mission.objectiveCells.first()

        engine.tapCell(firstCell.row, firstCell.col)
        assertEquals(1, engine.uiState().objectiveProgress)

        assertTrue(engine.longPressCell(firstCell.row, firstCell.col))
        assertEquals(0, engine.uiState().objectiveProgress)
    }

    @Test
    fun starsIncreaseLevelAndUnlockBlocks() {
        val engine = KidsMinecraftBuddyEngine()

        repeat(6) {
            completeMission(engine)
            engine.advanceMission()
        }

        val state = engine.uiState()
        assertTrue(state.level >= 3)
        assertTrue(state.unlockedBlocks.contains(KidsMinecraftBuddyEngine.BlockType.WOOD))
        assertTrue(state.unlockedBlocks.contains(KidsMinecraftBuddyEngine.BlockType.LIGHT))
    }

    @Test
    fun snapshotRoundTripRestoresProgress() {
        val engine = KidsMinecraftBuddyEngine()
        engine.selectBlock(KidsMinecraftBuddyEngine.BlockType.STONE)
        completeMission(engine)

        val snapshot = engine.progressSnapshot()
        val restored = KidsMinecraftBuddyEngine(snapshot)
        val state = restored.uiState()

        assertEquals(snapshot.totalStars, state.totalStars)
        assertEquals(snapshot.missionsCompleted, state.missionsCompleted)
        assertEquals(KidsMinecraftBuddyEngine.BlockType.STONE, state.selectedBlock)
    }

    private fun completeMission(engine: KidsMinecraftBuddyEngine) {
        val objective = engine.uiState().mission.objectiveCells.toList()
        objective.forEach { cell ->
            engine.tapCell(cell.row, cell.col)
        }
        assertTrue(engine.uiState().missionComplete)
    }
}
