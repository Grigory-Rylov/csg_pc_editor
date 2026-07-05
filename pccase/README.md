# PC Case Generator

Генератор 3D-моделей корпуса ПК из алюминиевого профиля и комплектующих.

## Модули

| Модуль | Описание |
|--------|----------|
| `pccase` | Основной модуль — генерация STL и PNG |
| `javascad` | 3D-примитивы и библиотека CSG |
| `cad3d` | STL-экспортёр |

## Сборка

```bash
./gradlew pccase:build
```

## Запуск

### Экспорт STL (по умолчанию)

Генерирует 4 STL-файла в папку `pccase/stl_pccase/`:

```bash
./gradlew pccase:run
```

Выходные файлы:
- `frame.stl` — рама из алюминиевого профиля 20×20 мм (530×350×330 мм)
- `motherboard.stl` — Supermicro H12SSL-i (ATX, SP3, 8× DDR4)
- `gpu_rtx3090.stl` — NVIDIA RTX 3090 (3-слот, 313×138×61 мм)
- `psu.stl` — ATX блок питания (150×86×140 мм)

### Рендер сцены в PNG

Рендерит всю сцену в PNG-файл (1920×1080) без GPU/дисплея:

```bash
./gradlew pccase:run --args="--render"
```

Выходные файлы:
- `scene.png` — рендер всей сборки с освещением

## Параметры корпуса

Редактируются в `PcCaseApp.kt`:

```kotlin
val frame = PcFrame(
    width = 530.0,    // мм, ширина
    height = 350.0,   // мм, высота
    depth = 330.0,    // мм, глубина
    profileSize = 20.0 // мм, сечение профиля
).build()
```

## Параметры рендеринга

В `SceneRenderer.kt`:

```kotlin
SceneRenderer(
    width = 1920,         // ширина изображения
    height = 1080,        // высота изображения
    fov = 45.0,           // угол обзора
    cameraDistance = 1400.0, // расстояние камеры
    cameraAngleX = -25.0,   // наклон камеры (вертикаль)
    cameraAngleY = 35.0     // поворот камеры (горизонталь)
)
```

## Структура проекта

```
pccase/
├── build.gradle.kts
└── src/main/java/com/github/grishberg/cad3d/pccase/
    ├── PcCaseApp.kt       # Точка входа CLI
    ├── PcFrame.kt          # Рама из алюминиевого профиля
    ├── Motherboard.kt      # Supermicro H12SSL-i
    ├── Gpu.kt              # RTX 3090
    ├── Psu.kt              # ATX блок питания
    └── SceneRenderer.kt    # Софтверный рендерер (Java2D)
```

## Добавление новых комплектующих

1. Создайте файл `NewComponent.kt` в `pccase/src/main/java/.../pccase/`
2. Реализуйте метод `build(): Abstract3dModel`
3. Добавьте вызов в `PcCaseApp.kt`

Пример:

```kotlin
class NewComponent {
    fun build(): Abstract3dModel {
        val parts = mutableListOf<Abstract3dModel>()
        parts.add(Cube(100.0, 50.0, 20.0).move(0.0, 25.0, 0.0))
        return Union(parts)
    }
}
```
