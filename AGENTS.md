# AGENTS.md — PC Case Generator

## Overview

This project generates 3D models of a PC case built from aluminum extrusion profiles
(20×20mm cross-section) along with component models: motherboard, GPU, and PSU.
All models are exported as STL files. A headless software renderer can produce
preview PNG images without a display or GPU.

## Project Structure

```
javascad/       — Core 3D modeling library (CSG, primitives, STL I/O)
cad3d/          — STL exporter (StlExporter, PolygonValidator, Triangulator)
plugin/         — Plugin interfaces (SettingsContainer, StlExportListener)
pccase/         — PC case generator module (main entry point)
```

## Build

```bash
./gradlew pccase:build
```

## Run Interactive Viewer (GUI)

Opens a Java2D window with mouse controls (rotate/pan/zoom):

```bash
./gradlew pccase:run
```

## Generate Preview Render (PNG, headless)

```bash
./gradlew pccase:run --args="--render"
```

Output: `pccase/stl_pccase/scene.png` (1920×1080, headless Java2D renderer)

## Aluminum Frame Profiles

The frame is built from 20×20mm aluminum extrusion profiles. **Always use
`AluminumProfile` factory functions when adding new frame beams** — never create
`Cube` directly for frame members.

### Available profile functions

| Function | Orientation | Length axis |
|----------|-------------|-------------|
| `AluminumProfile.vertical(length)` | Along Y | Y |
| `AluminumProfile.horizontalX(length)` | Along X | X |
| `AluminumProfile.horizontalZ(length)` | Along Z | Z |

These functions create a `Cube` with 20×20mm cross-section and track each cut
for the bill of materials report.

### Before building any frame, reset tracking:

```kotlin
AluminumProfile.reset()
```

### After building, get the report:

```kotlin
val report = AluminumProfile.generateReport()
println(report)
File("stl_pccase/profile_report.txt").writeText(report)
```

The report is automatically generated in `PcCaseApp.kt` and saved to
`pccase/stl_pccase/profile_report.txt` on every run.

### Example usage in PcFrame.kt

```kotlin
// Vertical corner post
AluminumProfile.vertical(height).move(-hw + p / 2, hh, -hd + p / 2)

// Horizontal beam along X (e.g. front/back at some Y level)
AluminumProfile.horizontalX(beamW).move(0.0, midY, -hd + p / 2)

// Horizontal beam along Z (e.g. left/right at some Y level)
AluminumProfile.horizontalZ(beamD).move(-hw + p / 2, midY, 0.0)
```

### Bill of Materials report

Every `--render` run produces a `profile_report.txt` listing each cut length,
orientation, quantity, and total length needed. This is used to order the
correct amount of 20×20mm aluminum extrusion.

## 3D Model Development Workflow

### 1. Edit models

Model source files are in:
```
pccase/src/main/java/com/github/grishberg/cad3d/pccase/
├── AluminumProfile.kt — 20×20mm profile builder + BOM tracker
├── PcFrame.kt         — Aluminum profile frame
├── Motherboard.kt     — Tyan S8030
├── Gpu.kt             — Gigabyte RTX 3090 Turbo
├── Psu.kt             — ATX power supply
├── SceneRenderer.kt   — Headless PNG renderer
└── PcCaseViewer.kt    — Interactive Java2D viewer (GUI)
```

Each model class has a `build(): Abstract3dModel` method that returns a CSG tree
built from primitives (`Cube`, `Cylinder`, etc.) combined with `addModel`/`subtractModel`.

### 2. Common 3D operations

```kotlin
// Create primitives
Cube(width, height, depth)
Cylinder(height, Radius.fromRadius(r))

// For frame profiles (ALWAYS use these instead of Cube):
AluminumProfile.vertical(length)
AluminumProfile.horizontalX(length)
AluminumProfile.horizontalZ(length)

// Transform (immutable — returns new instance)
model.move(x, y, z)
model.moveX(dx)
model.rotate(Angles3d.xyzOnly(rx, ry, rz))  // angles in degrees

// Boolean operations
model.addModel(other)       // Union
model.subtractModel(other)  // Difference

// Combine multiple models
Union(listOf(model1, model2, model3))
```

### 3. Export STL

The export pipeline: `Abstract3dModel` → `toCSG(context)` → `StlExporter.saveStl(polygons, path)`

```kotlin
val context = ColorFacetGenerationContext(Color.GRAY)
context.setFn(8)  // surface resolution
val csg = model.toCSG(context)
StlExporter.saveStl(csg.polygons, "output.stl")
```

### 4. Render preview

```kotlin
val renderer = SceneRenderer(
    width = 1920, height = 1080,
    cameraAngleX = -25.0, cameraAngleY = 35.0
)
renderer.renderScene(listOf("name" to csg), File("output.png"))
```

The renderer uses painter's algorithm with flat shading — no GPU required.

## Post-Modification Requirement

**After every model change**, run the render and send the resulting PNG
to the user via the MCP `vk-files` tool:

```bash
# 1. Build
./gradlew pccase:build

# 2. Generate render
./gradlew pccase:run --args="--render"

# 3. Send pccase/stl_pccase/scene.png to user via MCP vk-files
```

The `--render` run also produces `pccase/stl_pccase/profile_report.txt`
with the aluminum profile bill of materials. Share this info if the user
asks about quantities or lengths.

This ensures the user can visually verify each change without running the build locally.

## Adding a New Component

1. Create `NewComponent.kt` in the `pccase` package
2. Implement `fun build(): Abstract3dModel`
3. Register in `PcCaseApp.kt`:
   ```kotlin
   val newPart = NewComponent().build()
   exportModel(newPart, context, outDir, "new_part.stl")
   ```
4. Build, render, and send the PNG via MCP `vk-files`
