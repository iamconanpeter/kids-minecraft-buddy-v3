# Technical Plan — Kids Minecraft Buddy v3

## 1. Implementation approach
- Keep codebase simple and testable:
  - `MainActivity` hosts app view.
  - `KidsMinecraftBuddyView` handles rendering + touch interaction.
  - `GameEngine` encapsulates mission/progression logic.
  - `ProgressManager` handles local persistence.

## 2. Module/file plan

### App runtime
- `MainActivity.kt`
- `KidsMinecraftBuddyView.kt`
- `KidsMinecraftBuddyEngine.kt`
- `KidsMinecraftBuddyProgressManager.kt`

### Tests
- `KidsMinecraftBuddyEngineTest.kt`

### Docs
- planning + release docs under `docs/`

## 3. Data flow
1. Activity creates `ProgressManager`.
2. `ProgressManager` loads saved `ProgressSnapshot`.
3. Engine initialized with snapshot.
4. View reads engine state each frame and on touch.
5. On mission completion, view asks engine to apply rewards.
6. Engine emits updated snapshot to progress manager for save.

## 4. Rendering and interaction details
- Grid:
  - fixed-size mission board (e.g., 8x8 cells),
  - objective cells highlighted.
- Touch:
  - tap = place selected block,
  - long-press = remove block.
- HUD:
  - stars, level, mission icon, buddy mood icon.

## 5. Difficulty scaling
- Increase target cell count every mission cycle.
- Rotate mission types to avoid monotony.
- Keep first 3 missions intentionally easy.

## 6. Guardrails implementation
- Add soft “break” hint after N consecutive missions.
- Deterministic rewards only.
- No dark pattern timers.

## 7. Quality gates
- Unit tests for:
  - mission objective counting,
  - reward progression,
  - level threshold,
  - state serialization stability.
- Build commands:
  - `./gradlew test`
  - `./gradlew assembleDebug`

## 8. Risks and mitigations
- **Risk:** Build environment lacks Android SDK.
  - **Mitigation:** ensure `local.properties` and SDK path from template, keep dependencies minimal.
- **Risk:** Child UX still too text-heavy.
  - **Mitigation:** icon-led overlays and short labels only.
- **Risk:** Legal confusion with inspiration sources.
  - **Mitigation:** explicit original art/name/lore guidelines in docs and code comments.

## 9. Done definition
- Android app builds debug APK.
- Unit tests pass.
- Mission + buddy + progression + save loop functional.
- Documentation complete and explicit on legal-safe inspiration approach.
