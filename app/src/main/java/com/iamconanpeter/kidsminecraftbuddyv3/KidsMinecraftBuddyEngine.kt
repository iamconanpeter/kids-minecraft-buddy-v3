package com.iamconanpeter.kidsminecraftbuddyv3

class KidsMinecraftBuddyEngine(initialProgress: ProgressSnapshot = ProgressSnapshot()) {

    enum class BlockType(val colorHex: Int) {
        GRASS(0xFF7ED957.toInt()),
        STONE(0xFF9FA6B2.toInt()),
        WOOD(0xFFC68B59.toInt()),
        LIGHT(0xFFFFE066.toInt())
    }

    enum class MissionType {
        BRIDGE,
        PATH,
        SHELTER,
        RESCUE
    }

    enum class BuddyMood(val emoji: String) {
        EXCITED("🤩"),
        CHEER("😄"),
        THINKING("🤔"),
        PROUD("🥳")
    }

    enum class TapResult {
        PLACED,
        ALREADY_FILLED,
        OUT_OF_BOUNDS,
        BLOCK_LOCKED
    }

    data class Cell(val row: Int, val col: Int)

    data class BuddyState(
        val name: String,
        val mood: BuddyMood,
        val shortHint: String
    )

    data class Mission(
        val index: Int,
        val type: MissionType,
        val title: String,
        val icon: String,
        val objectiveCells: Set<Cell>
    )

    data class ProgressSnapshot(
        val totalStars: Int = 0,
        val missionsCompleted: Int = 0,
        val currentMissionIndex: Int = 0,
        val selectedBlockName: String = "GRASS"
    )

    data class EngineViewState(
        val gridSize: Int,
        val mission: Mission,
        val selectedBlock: BlockType,
        val unlockedBlocks: Set<BlockType>,
        val placedBlocks: Map<Cell, BlockType>,
        val objectiveProgress: Int,
        val objectiveTotal: Int,
        val missionComplete: Boolean,
        val lastMissionStars: Int,
        val totalStars: Int,
        val level: Int,
        val missionsCompleted: Int,
        val buddy: BuddyState,
        val showBreakHint: Boolean
    )

    val gridSize: Int = 8

    private var totalStars: Int = initialProgress.totalStars.coerceAtLeast(0)
    private var missionsCompleted: Int = initialProgress.missionsCompleted.coerceAtLeast(0)
    private var currentMissionIndex: Int = initialProgress.currentMissionIndex.coerceAtLeast(0)

    private var selectedBlock: BlockType = safeBlockName(initialProgress.selectedBlockName)
    private var buddyState: BuddyState = BuddyState(name = "Pip", mood = BuddyMood.EXCITED, shortHint = "Tap green squares")

    private var mission: Mission = createMission(currentMissionIndex)
    private val placedBlocks: MutableMap<Cell, BlockType> = mutableMapOf()

    private var removalsThisMission: Int = 0
    private var missionComplete: Boolean = false
    private var lastMissionStars: Int = 0

    init {
        if (!isBlockUnlocked(selectedBlock)) {
            selectedBlock = BlockType.GRASS
        }
        buddyState = buddyState.copy(shortHint = hintForMission(mission.type))
    }

    fun uiState(): EngineViewState {
        val progress = objectiveProgress()
        return EngineViewState(
            gridSize = gridSize,
            mission = mission,
            selectedBlock = selectedBlock,
            unlockedBlocks = unlockedBlocks(),
            placedBlocks = placedBlocks.toMap(),
            objectiveProgress = progress,
            objectiveTotal = mission.objectiveCells.size,
            missionComplete = missionComplete,
            lastMissionStars = lastMissionStars,
            totalStars = totalStars,
            level = level(),
            missionsCompleted = missionsCompleted,
            buddy = buddyState,
            showBreakHint = missionComplete && missionsCompleted > 0 && missionsCompleted % 3 == 0
        )
    }

    fun selectBlock(blockType: BlockType) {
        if (!isBlockUnlocked(blockType)) return
        selectedBlock = blockType
    }

    fun tapCell(row: Int, col: Int): TapResult {
        if (missionComplete) return TapResult.ALREADY_FILLED
        val cell = Cell(row, col)
        if (!cell.isInGrid()) return TapResult.OUT_OF_BOUNDS
        if (!isBlockUnlocked(selectedBlock)) return TapResult.BLOCK_LOCKED
        if (placedBlocks.containsKey(cell)) return TapResult.ALREADY_FILLED

        placedBlocks[cell] = selectedBlock
        buddyState = buddyState.copy(mood = BuddyMood.CHEER, shortHint = hintForMission(mission.type))

        if (objectiveProgress() >= mission.objectiveCells.size) {
            completeMission()
        }

        return TapResult.PLACED
    }

    fun longPressCell(row: Int, col: Int): Boolean {
        if (missionComplete) return false
        val cell = Cell(row, col)
        if (!cell.isInGrid()) return false

        val removed = placedBlocks.remove(cell) != null
        if (removed) {
            removalsThisMission += 1
            buddyState = buddyState.copy(mood = BuddyMood.THINKING, shortHint = "Nice fix!")
        }
        return removed
    }

    fun resetMission() {
        placedBlocks.clear()
        removalsThisMission = 0
        missionComplete = false
        lastMissionStars = 0
        buddyState = buddyState.copy(mood = BuddyMood.EXCITED, shortHint = hintForMission(mission.type))
    }

    fun advanceMission(): Boolean {
        if (!missionComplete) return false

        currentMissionIndex += 1
        mission = createMission(currentMissionIndex)
        placedBlocks.clear()
        removalsThisMission = 0
        missionComplete = false
        lastMissionStars = 0
        buddyState = buddyState.copy(mood = BuddyMood.EXCITED, shortHint = hintForMission(mission.type))

        if (!isBlockUnlocked(selectedBlock)) {
            selectedBlock = BlockType.GRASS
        }

        return true
    }

    fun progressSnapshot(): ProgressSnapshot = ProgressSnapshot(
        totalStars = totalStars,
        missionsCompleted = missionsCompleted,
        currentMissionIndex = currentMissionIndex,
        selectedBlockName = selectedBlock.name
    )

    private fun objectiveProgress(): Int = mission.objectiveCells.count { placedBlocks.containsKey(it) }

    private fun completeMission() {
        missionComplete = true
        missionsCompleted += 1

        val starsEarned = 2 + if (removalsThisMission <= 2) 1 else 0
        lastMissionStars = starsEarned
        totalStars += starsEarned

        buddyState = buddyState.copy(mood = BuddyMood.PROUD, shortHint = "High-five! 🌟")
    }

    private fun level(): Int = 1 + (totalStars / STARS_PER_LEVEL)

    private fun unlockedBlocks(): Set<BlockType> {
        val blocks = mutableSetOf(BlockType.GRASS, BlockType.STONE)
        if (level() >= 2) blocks += BlockType.WOOD
        if (level() >= 3) blocks += BlockType.LIGHT
        return blocks
    }

    private fun isBlockUnlocked(blockType: BlockType): Boolean = unlockedBlocks().contains(blockType)

    private fun safeBlockName(value: String): BlockType = runCatching { BlockType.valueOf(value) }
        .getOrDefault(BlockType.GRASS)

    private fun hintForMission(type: MissionType): String = when (type) {
        MissionType.BRIDGE -> "Build a bridge"
        MissionType.PATH -> "Light the path"
        MissionType.SHELTER -> "Make a safe hut"
        MissionType.RESCUE -> "Reach buddy flag"
    }

    private fun createMission(index: Int): Mission {
        val type = MissionType.entries[index % MissionType.entries.size]
        val tier = (index / MissionType.entries.size).coerceAtMost(4)

        val cells = when (type) {
            MissionType.BRIDGE -> bridgeCells(tier)
            MissionType.PATH -> pathCells(tier)
            MissionType.SHELTER -> shelterCells(tier)
            MissionType.RESCUE -> rescueCells(tier)
        }

        val title = when (type) {
            MissionType.BRIDGE -> "Bridge Rush"
            MissionType.PATH -> "Glow Path"
            MissionType.SHELTER -> "Buddy Shelter"
            MissionType.RESCUE -> "Rescue Run"
        }

        val icon = when (type) {
            MissionType.BRIDGE -> "🌉"
            MissionType.PATH -> "✨"
            MissionType.SHELTER -> "🏠"
            MissionType.RESCUE -> "🚩"
        }

        return Mission(
            index = index,
            type = type,
            title = title,
            icon = icon,
            objectiveCells = cells
        )
    }

    private fun bridgeCells(tier: Int): Set<Cell> {
        val length = (4 + tier).coerceAtMost(6)
        val row = 4
        return buildSet {
            for (col in 1..length) add(Cell(row, col))
        }
    }

    private fun pathCells(tier: Int): Set<Cell> {
        val extra = tier.coerceAtMost(3)
        val base = mutableListOf(
            Cell(6, 1), Cell(5, 1), Cell(5, 2), Cell(4, 2),
            Cell(4, 3), Cell(3, 3), Cell(3, 4)
        )
        repeat(extra) { step ->
            base += Cell((3 - step).coerceAtLeast(1), (4 + step).coerceAtMost(6))
        }
        return base.toSet()
    }

    private fun shelterCells(tier: Int): Set<Cell> {
        val size = (3 + tier).coerceAtMost(5)
        val startRow = 2
        val startCol = 2
        val endRow = (startRow + size - 1).coerceAtMost(6)
        val endCol = (startCol + size - 1).coerceAtMost(6)

        return buildSet {
            for (r in startRow..endRow) {
                for (c in startCol..endCol) {
                    val edge = r == startRow || r == endRow || c == startCol || c == endCol
                    if (edge) add(Cell(r, c))
                }
            }
        }
    }

    private fun rescueCells(tier: Int): Set<Cell> {
        val width = (3 + tier).coerceAtMost(5)
        val startCol = 1
        val topRow = 1
        return buildSet {
            for (r in 6 downTo topRow + 1) {
                add(Cell(r, startCol))
            }
            for (c in startCol..(startCol + width)) {
                add(Cell(topRow + 1, c.coerceAtMost(6)))
            }
            add(Cell(topRow, (startCol + width).coerceAtMost(6)))
        }
    }

    private fun Cell.isInGrid(): Boolean = row in 0 until gridSize && col in 0 until gridSize

    companion object {
        private const val STARS_PER_LEVEL = 8
    }
}
