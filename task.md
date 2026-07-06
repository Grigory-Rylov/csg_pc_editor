# PC Case DSL Editor — Task

## Goal
Implement a DSL (Domain-Specific Language) script editor that lets the user configure the PC case scene and re-render 3D models on the fly.

## DSL Grammar

Each command is one line:

```
frame <width> <depth> <height>          # Set frame dimensions in mm
component <type> <x> <y> <z>            # Place a component at coordinates
component <type> <x> <y> <z> count <N>  # Place N copies (only gpu supports count)
# comments
```

Supported component types:
- `motherboard` — Tyan S8030 (single instance, no count)
- `gpu` — Gigabyte RTX 3090 Turbo (supports count)
- `psu` — ATX power supply
- `cooler` — CPU cooler

Lines starting with `#` are comments. Empty lines are ignored.

### Default Script
```
frame 530 330 350
component motherboard 90 0 20.8
component gpu 0 0 100
component gpu 0 0 200 count 4
component psu -240 95 0
component psu -240 -95 0
component cooler 65 -20 7
```

## Architecture Changes

### 1. SceneConfig (new data class)
Located in `pccase/src/main/java/.../pccase/SceneConfig.kt`
- Holds: frame dimensions, list of ComponentPlacements
- `ComponentPlacement { type: String, x: Float, y: Float, z: Float, count: Int, rotation: Float }`

### 2. SceneConfigParser (new class)
Located in `pccase/src/main/java/.../pccase/SceneConfigParser.kt`
- `parse(script: String): Result<SceneConfig>`
- Line-by-line parser with error reporting
- Returns default config for empty script

### 3. Modified PcCaseModelFactory
- Change `buildAll()` → `buildAll(config: SceneConfig): Map<String, CSG>`
- Uses config values instead of hardcoded dimensions/positions

### 4. Modified PcCaseSceneBuilder
- Add `updateConfig(config: SceneConfig)` method
- Calls `requestBuffers()` internally to re-render
- Stores current config

### 5. ScriptEditorActivity
- Show default script in editor on open
- On OK: parse script, validate, return result via `setResult()`
- Show error toast on parse failure

### 6. Cad3dActivity
- `startActivityForResult` for script editor
- On `onActivityResult`: pass config to scene builder, trigger re-render

## Files To Create/Modify

**New files:**
- `pccase/src/main/java/.../pccase/SceneConfig.kt`
- `pccase/src/main/java/.../pccase/SceneConfigParser.kt`

**Modified files:**
- `pccase/src/main/java/.../pccase/PcCaseModelFactory.kt` — accept SceneConfig
- `android-viewer/app/src/main/java/.../util/PcCaseSceneBuilder.kt` — accept and apply config
- `android-viewer/app/src/main/java/.../viewer/ScriptEditorActivity.kt` — parse and return
- `android-viewer/app/src/main/java/.../viewer/Cad3dActivity.kt` — handle result and re-render

## Implementation Order
1. [ ] Create SceneConfig data class
2. [ ] Create SceneConfigParser with tests
3. [ ] Modify PcCaseModelFactory to accept SceneConfig
4. [ ] Modify PcCaseSceneBuilder with updateConfig
5. [ ] Wire ScriptEditorActivity ↔ Cad3dActivity (result passing)
6. [ ] Build, test on emulator, verify re-rendering
