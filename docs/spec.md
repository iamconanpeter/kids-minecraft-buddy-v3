# Functional Spec — Kids Minecraft Buddy v3

## 1. Platform and stack
- Platform: Android native app (Kotlin, Gradle, Android SDK)
- Min SDK: 24
- Target SDK: 34
- Rendering: Android custom `View` + Canvas (2D block grid)
- Storage: `SharedPreferences` (local save)

## 2. Screens

### 2.1 Home Screen
- Shows:
  - game title,
  - buddy avatar card,
  - play/start mission button,
  - stars, level, and mission count summary.

### 2.2 Mission Screen
- Components:
  - top HUD (mission icon, objective progress, stars),
  - block grid playfield,
  - block palette buttons,
  - action buttons (mission reset, back to home).

### 2.3 Mission Complete Overlay
- Displays completion icon, earned stars, next button.

## 3. Game entities

### 3.1 BlockType
- `GRASS`, `STONE`, `WOOD`, `LIGHT`

### 3.2 Buddy
- `id`, `name`, `mood`, `assistHint`
- v1 buddy: `pip` (cheerful helper)

### 3.3 MissionType
- `BRIDGE`, `PATH`, `SHELTER`, `RESCUE`

### 3.4 MissionState
- `missionId`, `type`, `targetCells`, `placedCells`, `isComplete`

### 3.5 PlayerProgress
- `totalStars`, `level`, `missionsCompleted`, `unlockedPalette`, `lastBuddyMood`

## 4. Mission logic rules
1. Mission initializes with a target pattern/zone.
2. Player taps cells to place currently selected block.
3. If placed cell belongs to target objective set and block is valid, objective progress increments.
4. Mission complete when all objective cells are filled correctly.
5. On completion:
   - stars awarded (base + bonus for minimal removals),
   - progress updated,
   - next mission generated.

## 5. Progression rules
- Level formula: `level = 1 + totalStars / 12` (integer floor).
- Unlock rules:
  - Level 2: unlock `WOOD`
  - Level 3: unlock `LIGHT`
- Mission sequence cycles through the four mission archetypes with increasing objective size.

## 6. Save/load
- Save trigger points:
  - on mission completion,
  - on app background,
  - on pause/stop.
- Save payload:
  - `totalStars`, `missionsCompleted`, `level`, `selectedBlock`, `currentMissionIndex`.
- If save absent: initialize defaults.

## 7. Child-friendly UX requirements
- Touch target minimum 64dp for primary actions.
- Labels max 1–3 words where possible.
- Visual cues for objective cells (outline + glow).
- Avoid nested menus.

## 8. Fairness & safety requirements
- Deterministic reward table.
- No ads in gameplay flow.
- No open chat.
- No external links from child path.
- No negative progression penalties.

## 9. Testable acceptance criteria
1. Mission completion triggers star increment and completion overlay.
2. Save/load restores progress after app restart.
3. Leveling thresholds work as defined.
4. Invalid taps outside grid do not crash or alter progress.
5. Block placement/removal updates objective count accurately.

## 10. Explicit requirement answers

### Where is Minecraft feel in this game?
- Grid-aligned blocks,
- place/remove construction actions,
- build-to-solve objective design,
- biome-like mission themes.

### How is it inspired by JJ & Mikey style without infringement?
- Uses original buddy duo adventure structure and pacing.
- Uses no copied character identity, names, art, slogans, or plot replicas.
