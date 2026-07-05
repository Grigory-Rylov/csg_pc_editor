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

## Generate STL Files

```bash
./gradlew pccase:run
```

Output: `pccase/stl_pccase/*.stl`

## Generate Preview Render (PNG)

```bash
./gradlew pccase:run --args="--render"
```

Output: `pccase/stl_pccase/scene.png` (1920×1080, headless Java2D renderer)

## 3D Model Development Workflow

### 1. Edit models

Model source files are in:
```
pccase/src/main/java/com/github/grishberg/cad3d/pccase/
├── PcFrame.kt        — Aluminum profile frame
├── Motherboard.kt    — Supermicro H12SSL-i
├── Gpu.kt            — RTX 3090
├── Psu.kt            — ATX power supply
└── SceneRenderer.kt  — Headless PNG renderer
```

Each model class has a `build(): Abstract3dModel` method that returns a CSG tree
built from primitives (`Cube`, `Cylinder`, etc.) combined with `addModel`/`subtractModel`.

### 2. Common 3D operations

```kotlin
// Create primitives
Cube(width, height, depth)
Cylinder(height, Radius.fromRadius(r))

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
