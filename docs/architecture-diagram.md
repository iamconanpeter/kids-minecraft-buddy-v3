# Architecture Diagram — Kids Minecraft Buddy v3

```text
+-------------------+
|   MainActivity    |
|-------------------|
| creates View      |
| wires ProgressMgr |
+---------+---------+
          |
          v
+----------------------------+
|   KidsMinecraftBuddyView   |
|----------------------------|
| draw HUD + Grid + Buddy    |
| handle tap / long-press    |
| call engine mutations      |
+-------------+--------------+
              |
              v
+----------------------------+
|  KidsMinecraftBuddyEngine  |
|----------------------------|
| mission generation         |
| block placement/removal    |
| objective checks           |
| reward + progression       |
| exposes immutable state    |
+-------------+--------------+
              |
              v
+----------------------------+
| KidsMinecraftBuddyProgress |
| Manager (SharedPrefs)      |
|----------------------------|
| load snapshot at launch    |
| save snapshot on updates   |
+----------------------------+
```

## Runtime state model

```text
EngineState
├── MissionState
│   ├── type
│   ├── targetCells
│   ├── placedCells
│   └── completion
├── BuddyState
│   ├── name
│   ├── mood
│   └── hint
└── PlayerProgress
    ├── totalStars
    ├── level
    ├── missionsCompleted
    └── unlocks
```

## Notes
- Engine is pure Kotlin-friendly to maximize unit test coverage.
- View layer performs no business logic beyond input mapping and rendering.
- Persistence remains local-only for child safety and offline reliability.
