package com.iamconanpeter.kidsminecraftbuddyv3

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.floor
import kotlin.math.min

class KidsMinecraftBuddyView(
    context: Context,
    private val engine: KidsMinecraftBuddyEngine,
    private val onProgressChanged: (KidsMinecraftBuddyEngine.ProgressSnapshot) -> Unit
) : View(context) {

    private enum class Screen {
        HOME,
        PLAY,
        COMPLETE
    }

    private var screen: Screen = Screen.HOME

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 66f
        textAlign = Paint.Align.CENTER
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
    }
    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
    }

    private val playButton = RectF()
    private val resetButton = RectF()
    private val homeButton = RectF()
    private val nextButton = RectF()
    private val gridRect = RectF()

    private val paletteRects = linkedMapOf<KidsMinecraftBuddyEngine.BlockType, RectF>()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            if (screen != Screen.PLAY) return
            val cell = cellFromPoint(e.x, e.y) ?: return
            if (engine.longPressCell(cell.first, cell.second)) {
                invalidate()
            }
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            handleTap(e.x, e.y)
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)

        when (screen) {
            Screen.HOME -> drawHome(canvas)
            Screen.PLAY -> drawPlay(canvas)
            Screen.COMPLETE -> {
                drawPlay(canvas)
                drawCompleteOverlay(canvas)
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        paint.color = Color.parseColor("#2D3B8F")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawHome(canvas: Canvas) {
        titlePaint.textSize = 70f
        canvas.drawText("🧱 Buddy Block Quest", width / 2f, height * 0.22f, titlePaint)

        bodyPaint.textSize = 40f
        canvas.drawText("Build + Adventure", width * 0.30f, height * 0.34f, bodyPaint)

        playButton.set(width * 0.18f, height * 0.44f, width * 0.82f, height * 0.60f)
        paint.color = Color.parseColor("#59D66D")
        canvas.drawRoundRect(playButton, 48f, 48f, paint)
        titlePaint.textSize = 64f
        canvas.drawText("▶ PLAY", playButton.centerX(), playButton.centerY() + 22f, titlePaint)

        smallPaint.color = Color.parseColor("#E8EEFF")
        canvas.drawText("Tap green squares • Hold to erase", width * 0.2f, height * 0.72f, smallPaint)
    }

    private fun drawPlay(canvas: Canvas) {
        val state = engine.uiState()

        drawHud(canvas, state)
        drawGrid(canvas, state)
        drawPalette(canvas, state)
        drawActionButtons(canvas)
    }

    private fun drawHud(canvas: Canvas, state: KidsMinecraftBuddyEngine.EngineViewState) {
        paint.color = Color.parseColor("#1E285F")
        canvas.drawRoundRect(24f, 24f, width - 24f, 210f, 28f, 28f, paint)

        titlePaint.textSize = 50f
        canvas.drawText("${state.mission.icon} ${state.mission.title}", width * 0.5f, 95f, titlePaint)

        bodyPaint.textSize = 34f
        canvas.drawText("⭐ ${state.totalStars}   Lv ${state.level}", 52f, 150f, bodyPaint)
        canvas.drawText("🎯 ${state.objectiveProgress}/${state.objectiveTotal}", width * 0.56f, 150f, bodyPaint)

        smallPaint.textSize = 28f
        canvas.drawText("${state.buddy.mood.emoji} ${state.buddy.name}: ${state.buddy.shortHint}", 52f, 192f, smallPaint)
    }

    private fun drawGrid(canvas: Canvas, state: KidsMinecraftBuddyEngine.EngineViewState) {
        val top = 240f
        val side = 38f
        val size = min(width - side * 2, height * 0.46f)
        gridRect.set(side, top, side + size, top + size)

        paint.color = Color.parseColor("#1B2454")
        canvas.drawRoundRect(gridRect, 24f, 24f, paint)

        val cellSize = size / state.gridSize

        for (row in 0 until state.gridSize) {
            for (col in 0 until state.gridSize) {
                val left = gridRect.left + col * cellSize
                val topCell = gridRect.top + row * cellSize
                val rect = RectF(left + 3f, topCell + 3f, left + cellSize - 3f, topCell + cellSize - 3f)
                val cell = KidsMinecraftBuddyEngine.Cell(row, col)

                val isObjective = state.mission.objectiveCells.contains(cell)
                paint.style = Paint.Style.FILL
                paint.color = if (isObjective) Color.parseColor("#3950B7") else Color.parseColor("#2A377F")
                canvas.drawRect(rect, paint)

                val block = state.placedBlocks[cell]
                if (block != null) {
                    paint.color = block.colorHex
                    canvas.drawRect(rect.left + 5f, rect.top + 5f, rect.right - 5f, rect.bottom - 5f, paint)
                }

                paint.style = Paint.Style.STROKE
                paint.color = if (isObjective) Color.parseColor("#95A9FF") else Color.parseColor("#4657AA")
                paint.strokeWidth = 2f
                canvas.drawRect(rect, paint)
            }
        }

        paint.style = Paint.Style.FILL
    }

    private fun drawPalette(canvas: Canvas, state: KidsMinecraftBuddyEngine.EngineViewState) {
        val top = gridRect.bottom + 28f
        val paletteHeight = 120f
        paint.color = Color.parseColor("#1E285F")
        canvas.drawRoundRect(24f, top, width - 24f, top + paletteHeight, 28f, 28f, paint)

        paletteRects.clear()
        val items = KidsMinecraftBuddyEngine.BlockType.entries
        val widthPer = (width - 80f) / items.size

        items.forEachIndexed { index, blockType ->
            val left = 40f + index * widthPer
            val rect = RectF(left, top + 16f, left + widthPer - 12f, top + paletteHeight - 16f)
            paletteRects[blockType] = rect

            val unlocked = state.unlockedBlocks.contains(blockType)
            val selected = state.selectedBlock == blockType

            paint.color = if (selected) Color.WHITE else Color.parseColor("#2C3A82")
            canvas.drawRoundRect(rect, 22f, 22f, paint)

            paint.color = if (unlocked) blockType.colorHex else Color.parseColor("#7A7F8F")
            canvas.drawRoundRect(rect.left + 16f, rect.top + 16f, rect.right - 16f, rect.bottom - 16f, 18f, 18f, paint)

            if (!unlocked) {
                smallPaint.textSize = 36f
                canvas.drawText("🔒", rect.centerX() - 16f, rect.centerY() + 12f, smallPaint)
            }
        }
    }

    private fun drawActionButtons(canvas: Canvas) {
        val top = gridRect.bottom + 160f

        homeButton.set(24f, top, width * 0.45f, top + 110f)
        paint.color = Color.parseColor("#4859B8")
        canvas.drawRoundRect(homeButton, 24f, 24f, paint)
        bodyPaint.textSize = 40f
        canvas.drawText("🏠 Home", homeButton.left + 30f, homeButton.centerY() + 14f, bodyPaint)

        resetButton.set(width * 0.55f, top, width - 24f, top + 110f)
        paint.color = Color.parseColor("#F39B4A")
        canvas.drawRoundRect(resetButton, 24f, 24f, paint)
        canvas.drawText("🔄 Reset", resetButton.left + 30f, resetButton.centerY() + 14f, bodyPaint)
    }

    private fun drawCompleteOverlay(canvas: Canvas) {
        val state = engine.uiState()

        paint.color = Color.parseColor("#B3000000")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val panel = RectF(width * 0.12f, height * 0.24f, width * 0.88f, height * 0.72f)
        paint.color = Color.parseColor("#1D255A")
        canvas.drawRoundRect(panel, 36f, 36f, paint)

        titlePaint.textSize = 68f
        canvas.drawText("🎉 Mission Clear!", panel.centerX(), panel.top + 100f, titlePaint)

        bodyPaint.textSize = 48f
        canvas.drawText("+${state.lastMissionStars} ⭐", panel.left + 80f, panel.top + 190f, bodyPaint)
        canvas.drawText("Total ${state.totalStars} ⭐", panel.left + 80f, panel.top + 250f, bodyPaint)

        if (state.showBreakHint) {
            smallPaint.textSize = 36f
            canvas.drawText("💤 Break time soon!", panel.left + 80f, panel.top + 315f, smallPaint)
        }

        nextButton.set(panel.left + 60f, panel.bottom - 130f, panel.right - 60f, panel.bottom - 36f)
        paint.color = Color.parseColor("#59D66D")
        canvas.drawRoundRect(nextButton, 28f, 28f, paint)
        titlePaint.textSize = 52f
        canvas.drawText("➡ Next Mission", nextButton.centerX(), nextButton.centerY() + 18f, titlePaint)
    }

    private fun handleTap(x: Float, y: Float) {
        when (screen) {
            Screen.HOME -> {
                if (playButton.contains(x, y)) {
                    screen = Screen.PLAY
                    invalidate()
                }
            }

            Screen.PLAY -> {
                val state = engine.uiState()

                if (homeButton.contains(x, y)) {
                    screen = Screen.HOME
                    invalidate()
                    return
                }

                if (resetButton.contains(x, y)) {
                    engine.resetMission()
                    invalidate()
                    return
                }

                paletteRects.entries.firstOrNull { it.value.contains(x, y) }?.let { entry ->
                    if (state.unlockedBlocks.contains(entry.key)) {
                        engine.selectBlock(entry.key)
                        invalidate()
                    }
                    return
                }

                val cell = cellFromPoint(x, y)
                if (cell != null) {
                    val result = engine.tapCell(cell.first, cell.second)
                    if (result == KidsMinecraftBuddyEngine.TapResult.PLACED) {
                        if (engine.uiState().missionComplete) {
                            onProgressChanged(engine.progressSnapshot())
                            screen = Screen.COMPLETE
                        }
                        invalidate()
                    }
                }
            }

            Screen.COMPLETE -> {
                if (nextButton.contains(x, y)) {
                    engine.advanceMission()
                    onProgressChanged(engine.progressSnapshot())
                    screen = Screen.PLAY
                    invalidate()
                }
            }
        }
    }

    private fun cellFromPoint(x: Float, y: Float): Pair<Int, Int>? {
        if (!gridRect.contains(x, y)) return null
        val state = engine.uiState()
        val cellSize = gridRect.width() / state.gridSize

        val col = floor((x - gridRect.left) / cellSize).toInt()
        val row = floor((y - gridRect.top) / cellSize).toInt()

        return if (row in 0 until state.gridSize && col in 0 until state.gridSize) {
            row to col
        } else {
            null
        }
    }
}
